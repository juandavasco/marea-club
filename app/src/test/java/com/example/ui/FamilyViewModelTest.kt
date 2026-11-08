package com.example.ui

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MareaRepository
import com.example.data.family.DeviceRole
import com.example.data.family.FakeFamilyBackend
import com.example.data.family.FakeServer
import com.example.data.family.LinkRole
import com.example.domain.LinkCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import android.os.Looper

/**
 * Vínculo familiar de punta a punta con un servidor en memoria (entrega 3, QA del Prompt Maestro §8).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FamilyViewModelTest {

    private lateinit var app: Application
    private var now = 1_000_000L
    private val server = FakeServer { now }

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    /** Todos los perfiles de la prueba comparten las preferencias: cada "teléfono" empieza sin vínculo. */
    private fun freshDevice() {
        app.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).edit()
            .remove("device_role").remove("family_id").commit()
    }

    private fun adult(name: String = "Juan", token: String = "juan"): FamilyViewModel {
        freshDevice()
        val vm = FamilyViewModel(app, { now }, { FakeFamilyBackend(server, name) })
        vm.setRole(DeviceRole.ADULT)
        vm.onGoogleIdToken(token); idle()
        return vm
    }

    private fun child(): FamilyViewModel {
        freshDevice()
        val vm = FamilyViewModel(app, { now }, { FakeFamilyBackend(server) })
        vm.setRole(DeviceRole.CHILD)
        return vm
    }

    private fun adultWithFamily(): FamilyViewModel = adult().also { it.createFamily("Navegante"); idle() }

    private fun FamilyViewModel.newCode(role: LinkRole = LinkRole.CHILD): String {
        generateCode(role); idle()
        return activeCode.value!!.code
    }

    @Test
    fun `el adulto crea la familia y genera un codigo de 8 caracteres`() {
        val a = adultWithFamily()
        assertNotNull(a.familyId.value)
        assertEquals("Juan", a.family.value!!.adultNames.values.single())
        val code = a.newCode()
        assertEquals(8, code.length)
        // En el servidor solo queda el hash
        assertTrue(server.codes.keys.single() == LinkCode.hash(code))
        assertFalse(server.codes.keys.any { it.contains(code) })
    }

    @Test
    fun `el nino se vincula, el adulto lo ve vinculado y se pide crear el PIN`() {
        val a = adultWithFamily()
        val code = a.newCode()
        val c = child()
        c.childRedeem(LinkCode.format(code).lowercase(), "Navegante"); idle()
        assertTrue(c.isChildLinked(c.family.value))
        assertTrue(a.family.value!!.hasChild)
        assertTrue(c.needsPinSetup.value)
        assertEquals(listOf("Juan"), c.family.value!!.adultNameList())
    }

    @Test
    fun `un codigo sirve una sola vez`() {
        val a = adultWithFamily()
        val code = a.newCode()
        child().apply { childRedeem(code, "N"); idle() }
        freshDevice()
        val other = FamilyViewModel(app, { now }, { FakeFamilyBackend(server) })
        other.childRedeem(code, "Otro"); idle()
        assertFalse(other.isChildLinked(other.family.value))
        assertTrue(other.message.value!!.contains("no sirve"))
    }

    @Test
    fun `un codigo vence a los 10 minutos`() {
        val a = adultWithFamily()
        val code = a.newCode()
        now += 10 * 60_000L
        val c = child()
        c.childRedeem(code, "N"); idle()
        assertFalse(c.isChildLinked(c.family.value))
        assertNotNull(c.message.value)
    }

    @Test
    fun `un codigo mal escrito se explica sin llamar al servidor`() {
        val c = child()
        c.childRedeem("K7Q", "N"); idle()
        assertTrue(c.message.value!!.contains("8 letras"))
    }

    @Test
    fun `sin conexion se avisa y no se vincula`() {
        val a = adultWithFamily()
        val code = a.newCode()
        server.online = false
        val c = child()
        c.childRedeem(code, "N"); idle()
        assertEquals(FamilyViewModel.NO_CONNECTION, c.message.value)
        assertFalse(server.codes.values.single().used)
    }

    @Test
    fun `segundo adulto con codigo de adulto, maximo dos`() {
        val a = adultWithFamily()
        val code = a.newCode(LinkRole.ADULT)
        val b = adult("Ana", "ana")
        b.joinAsSecondAdult(code); idle()
        assertEquals(a.familyId.value, b.familyId.value)
        assertEquals(listOf("Juan", "Ana"), a.family.value!!.adultNameList())
        // Ya hay dos: no se pueden generar más códigos de adulto
        a.generateCode(LinkRole.ADULT); idle()
        assertNotNull(a.message.value)
    }

    @Test
    fun `un codigo de nino no sirve para entrar como adulto`() {
        val a = adultWithFamily()
        val code = a.newCode(LinkRole.CHILD)
        val b = adult("Ana", "ana")
        b.joinAsSecondAdult(code); idle()
        assertNull(b.familyId.value)
    }

    @Test
    fun `el adulto desvincula al nino y el nino deja de verse vinculado`() {
        val a = adultWithFamily()
        val code = a.newCode()
        val c = child().apply { childRedeem(code, "N"); idle() }
        a.unlinkChild(); idle()
        assertFalse(a.family.value!!.hasChild)
        assertFalse(c.isChildLinked(c.family.value))
    }

    @Test
    fun `PIN - se crea, se verifica y se bloquea tras 5 intentos`() {
        val c = child()
        assertEquals("Ese PIN es fácil de adivinar. Prueba con otros números.", c.savePin("1234", "1234"))
        assertNull(c.savePin("2580", "2580"))
        val reopened = child()
        assertFalse("pide el PIN al abrir", reopened.unlocked.value)
        repeat(4) { assertTrue(reopened.checkPin("0000") is PinCheck.Wrong) }
        assertTrue(reopened.checkPin("0000") is PinCheck.Locked)
        assertTrue("bloqueado aunque sea correcto", reopened.checkPin("2580") is PinCheck.Locked)
        now += 31_000
        assertEquals(PinCheck.Ok, reopened.checkPin("2580"))
        assertTrue(reopened.unlocked.value)
    }

    @Test
    fun `PIN olvidado - un codigo nuevo del adulto permite crear otro sin perder el vinculo`() {
        val a = adultWithFamily()
        val c = child().apply { childRedeem(a.newCode(), "N"); idle(); savePin("2580", "2580") }
        val reopened = FamilyViewModel(app, { now }, { FakeFamilyBackend(server) })
        assertFalse(reopened.unlocked.value)
        // El teléfono es el mismo, pero aquí el doble del servidor crea otra sesión anónima:
        reopened.childRedeem(a.newCode(), "N", resettingPin = true); idle()
        assertTrue(reopened.unlocked.value)
        assertTrue(reopened.needsPinSetup.value)
        assertFalse(reopened.hasPin.value)
        assertTrue(a.family.value!!.hasChild)
        assertTrue(c.hasPin.value)
    }

    @Test
    fun `quien ya usaba la app antes de la entrega 3 queda como nino sin preguntar`() {
        app.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean("is_onboarded", true).commit()
        val vm = FamilyViewModel(app, { now }, { FakeFamilyBackend(server) })
        assertEquals(DeviceRole.CHILD, vm.role.value)
    }

    @Test
    fun `borrar los datos saca al nino de la familia`() {
        val a = adultWithFamily()
        val c = child().apply { childRedeem(a.newCode(), "N"); idle() }
        c.onLocalReset(); idle()
        assertFalse(a.family.value!!.hasChild)
        assertNull(c.role.value)
    }

    @Test
    fun `tras 5 codigos invalidos hay que esperar antes de probar otro`() {
        val a = adultWithFamily()
        val c = child()
        repeat(5) { c.childRedeem("K7QX-M2P${"ABCDE"[it]}", "N"); idle() }
        val good = a.newCode()
        c.childRedeem(good, "N"); idle()
        assertTrue(c.message.value!!.contains("Espera"))
        assertFalse(c.isChildLinked(c.family.value))
        now += 31_000
        c.childRedeem(good, "N"); idle()
        assertTrue(c.isChildLinked(c.family.value))
    }
}
