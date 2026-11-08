package com.example.ui.screens.family

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.data.family.CompanionRequest
import com.example.data.family.FamilyInfo
import com.example.data.family.RequestStatus
import com.example.data.family.SharedWeek
import com.example.data.family.LinkRole
import com.example.domain.LinkCode
import com.example.domain.PinRules
import com.example.ui.ActiveCode
import com.example.ui.FamilyViewModel
import com.example.ui.PinCheck
import com.example.ui.components.*
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =====================================================================================
// Comunes
// =====================================================================================

@Composable
private fun ScreenColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MareaBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MareaSpace.screen)
            .padding(bottom = MareaSpace.xl),
        content = content
    )
}

@Composable
private fun BodyText(text: String, modifier: Modifier = Modifier, color: Color = MareaTextDark) {
    Text(text, fontFamily = NunitoFontFamily, fontSize = 15.sp, lineHeight = 21.sp, color = color, modifier = modifier)
}

@Composable
private fun ErrorText(message: String?) {
    if (message != null) {
        MareaNotice(text = message, tone = NoticeTone.SUPPORT, tag = "family_message", modifier = Modifier.padding(top = MareaSpace.m))
    }
}

@Composable
private fun CodeField(value: String, onValueChange: (String) -> Unit, tag: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.take(9).uppercase()) },
        label = { Text("Código (8 letras y números)") },
        placeholder = { Text("K7QX-M2PA") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Ascii),
        shape = RoundedCornerShape(16.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    )
}

@Composable
private fun PinField(value: String, onValueChange: (String) -> Unit, label: String, tag: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter { it.isDigit() }.take(PinRules.MAX_LENGTH)) },
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    )
}

/** Lo que el adulto ve y lo que no (Prompt Maestro §7 y §9). Se muestra a los dos. */
@Composable
fun WhatAdultsSeeCard(forChild: Boolean, modifier: Modifier = Modifier) {
    MareaCard(modifier = modifier.testTag("privacy_what_adult_sees")) {
        Text(
            if (forChild) "Lo que tu adulto puede ver" else "Lo que puede ver usted",
            fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MareaTextDark
        )
        BodyText("• Que el perfil está vinculado.\n• Los pedidos de «Mis salvavidas».\n• Un resumen de la semana, solo si ${if (forChild) "tú decides" else "el niño o niña decide"} compartirlo.", Modifier.padding(top = MareaSpace.xs))
        Text(
            if (forChild) "Lo que tu adulto NO puede ver" else "Lo que NO puede ver",
            fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MareaTextDark,
            modifier = Modifier.padding(top = MareaSpace.m)
        )
        BodyText("• Las mareas registradas, las notas ni el diario.\n• Las respuestas de las actividades.\n• La isla ni sus reflexiones.\nTodo eso queda cifrado solo en el teléfono ${if (forChild) "tuyo" else "del niño o niña"}.", Modifier.padding(top = MareaSpace.xs))
    }
}

// =====================================================================================
// Elegir quién usa el teléfono
// =====================================================================================

@Composable
fun RoleChoiceScreen(onChild: () -> Unit, onAdult: () -> Unit, modifier: Modifier = Modifier) {
    ScreenColumn(modifier) {
        MareaHeader(title = "¿Quién va a usar este teléfono?", subtitle = "Marea Club tiene un espacio para chicos y chicas, y otro para los adultos que los acompañan.")
        MareaOption(
            text = "Soy un chico o una chica",
            supporting = "Registra tus mareas, explora actividades y cuida tu isla. No necesitas cuenta.",
            selected = false,
            onClick = onChild,
            tag = "role_child"
        )
        Spacer(Modifier.height(MareaSpace.m))
        MareaOption(
            text = "Soy un adulto de la familia",
            supporting = "Vincule el perfil de su hijo o hija y reciba sus pedidos de acompañamiento. Necesita una cuenta de Google.",
            selected = false,
            onClick = onAdult,
            tag = "role_adult"
        )
    }
}

// =====================================================================================
// Adulto
// =====================================================================================

/** Pide a Android una cuenta de Google y devuelve su token, o un mensaje para la persona. */
suspend fun requestGoogleIdToken(context: Context): Result<String> = try {
    val option = GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id)).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
    val credential = CredentialManager.create(context).getCredential(context, request).credential
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
    } else {
        Result.failure(IllegalStateException("No se recibió una cuenta de Google."))
    }
} catch (e: GetCredentialCancellationException) {
    Result.failure(IllegalStateException("Se canceló el inicio de sesión."))
} catch (e: NoCredentialException) {
    Result.failure(IllegalStateException("No hay una cuenta de Google en este teléfono. Agréguela en Ajustes y vuelva a intentar."))
} catch (e: GetCredentialException) {
    Result.failure(IllegalStateException("No se pudo iniciar sesión con Google. Revise la conexión e intente de nuevo."))
}

/** Toda la experiencia del adulto (entrega 3: cuenta y familia). */
@Composable
fun AdultApp(family: FamilyViewModel, modifier: Modifier = Modifier) {
    val signedIn by family.adultSignedIn.collectAsState()
    val familyId by family.familyId.collectAsState()
    val info by family.family.collectAsState()
    val busy by family.busy.collectAsState()
    val message by family.message.collectAsState()
    val activeCode by family.activeCode.collectAsState()

    when {
        !signedIn -> AdultSignInScreen(family, busy, message, modifier)
        familyId == null -> AdultSetupScreen(family, busy, message, modifier)
        else -> AdultHome(family, info, activeCode, busy, message, modifier)
    }
}

private enum class AdultTab(val label: String, val tag: String) {
    RESUMEN("Resumen", "adult_tab_resumen"),
    FAMILIA("Familia", "adult_tab_familia"),
    BITACORA("Bitácora", "adult_tab_bitacora"),
    CONEXION("Conexión", "adult_tab_conexion"),
    CONFIG("Configuración", "adult_tab_config")
}

/** Perfil del adulto (Prompt Maestro §13): Resumen, Familia, Bitácora, Conexión familiar y Configuración. */
@Composable
private fun AdultHome(
    family: FamilyViewModel,
    info: FamilyInfo?,
    activeCode: ActiveCode?,
    busy: Boolean,
    message: String?,
    modifier: Modifier
) {
    var tab by rememberSaveable { mutableStateOf(AdultTab.RESUMEN) }
    val requests by family.requests.collectAsState()
    val week by family.sharedWeek.collectAsState()
    val pending = requests.count { it.status == RequestStatus.ENVIADA }
    Column(modifier.fillMaxSize().background(MareaBackground)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                AdultTab.RESUMEN -> AdultSummaryScreen(family, info, requests, week, busy, message)
                AdultTab.FAMILIA -> AdultFamilyScreen(family, info, activeCode, busy, message, Modifier)
                AdultTab.BITACORA -> BitacoraScreen()
                AdultTab.CONEXION -> ConnectionListScreen(forChild = false)
                AdultTab.CONFIG -> AdultSettingsScreen(family)
            }
        }
        NavigationBar(containerColor = MareaTealText) {
            AdultTab.entries.forEach { t ->
                NavigationBarItem(
                    selected = tab == t,
                    onClick = { tab = t },
                    icon = {
                        BadgedBox(badge = { if (t == AdultTab.RESUMEN && pending > 0) Badge { Text("$pending") } }) {
                            Icon(
                                when (t) {
                                    AdultTab.RESUMEN -> Icons.Default.Home
                                    AdultTab.FAMILIA -> Icons.Default.People
                                    AdultTab.BITACORA -> Icons.AutoMirrored.Filled.MenuBook
                                    AdultTab.CONEXION -> Icons.Default.Favorite
                                    AdultTab.CONFIG -> Icons.Default.Settings
                                },
                                contentDescription = null
                            )
                        }
                    },
                    label = { Text(t.label, color = Color.White, fontSize = 11.sp, maxLines = 1, softWrap = false) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MareaTealText,
                        unselectedIconColor = Color.White,
                        indicatorColor = Color.White
                    ),
                    modifier = Modifier.testTag(t.tag)
                )
            }
        }
    }
}

/** Resumen: pedidos de Mis salvavidas dirigidos a este adulto y la semana, solo si el niño la comparte. */
@Composable
private fun AdultSummaryScreen(
    family: FamilyViewModel,
    info: FamilyInfo?,
    requests: List<CompanionRequest>,
    week: SharedWeek?,
    busy: Boolean,
    message: String?
) {
    val childName = info?.childName?.ifBlank { null } ?: "Su hijo o hija"
    val myName = info?.adultNames?.get(family.myUid) ?: ""
    val open = requests.filter { it.status.isOpen }
    val past = requests.filterNot { it.status.isOpen }.take(5)
    ScreenColumn {
        MareaHeader(title = if (myName.isNotBlank()) "Hola, $myName" else "Resumen", subtitle = "Lo que $childName decidió compartir con usted.")

        MareaSectionTitle("Mis salvavidas", supporting = "Pedidos de compañía que le envió a usted.")
        if (info?.hasChild != true) {
            BodyText("Aún no hay un perfil vinculado. Vincúlelo desde la pestaña Familia.", color = MareaTextMuted)
        } else if (open.isEmpty()) {
            BodyText("No hay pedidos abiertos.", Modifier.testTag("adult_no_requests"), color = MareaTextMuted)
        }
        open.forEach { r -> AdultRequestCard(r, childName, busy, onRespond = { status -> family.respond(r.id, status) }) }
        ErrorText(message)
        MareaNotice(
            text = "En esta versión los pedidos se ven al abrir Marea Club: aún no llegan notificaciones al teléfono. Abra la app con frecuencia.",
            modifier = Modifier.padding(top = MareaSpace.m),
            tag = "adult_no_push_notice"
        )

        MareaSectionTitle("Su semana", supporting = "Solo aparece si $childName activa «Compartir mi semana». Es su decisión.")
        if (week == null) {
            BodyText("$childName no está compartiendo su semana.", Modifier.testTag("adult_week_not_shared"), color = MareaTextMuted)
        } else {
            SharedWeekCard(week)
        }

        if (past.isNotEmpty()) {
            MareaSectionTitle("Pedidos anteriores")
            past.forEach { r ->
                BodyText("${dateTime(r.createdAtMillis)} · ${r.reason?.label ?: "sin especificar"} · ${statusLabel(r.status)}", Modifier.padding(bottom = MareaSpace.xs), color = MareaTextMuted)
            }
        }
    }
}

private fun dateTime(ms: Long): String = SimpleDateFormat("EEE d MMM, h:mm a", Locale.forLanguageTag("es-CO")).format(Date(ms))

private fun statusLabel(s: RequestStatus): String = when (s) {
    RequestStatus.ENVIADA -> "sin ver"
    RequestStatus.VISTA -> "visto"
    RequestStatus.VOY -> "respondió: voy"
    RequestStatus.NO_PUEDO -> "respondió: no puedo"
    RequestStatus.CANCELADA -> "cancelado"
    RequestStatus.CERRADA -> "ya estaba acompañado"
}

@Composable
private fun AdultRequestCard(r: CompanionRequest, childName: String, busy: Boolean, onRespond: (RequestStatus) -> Unit) {
    MareaCard(modifier = Modifier.padding(bottom = MareaSpace.m).testTag("adult_request_${r.id}"), background = Color(0xFFFFF6E5)) {
        Text("$childName necesita acompañamiento", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MareaTextDark)
        BodyText("«Necesito que estés conmigo. Cuando puedas, ven a acompañarme».", Modifier.padding(top = MareaSpace.xs))
        BodyText("${r.reason?.let { "Necesita: ${it.label}" } ?: "No dijo qué necesita (no es necesario)"} · ${dateTime(r.createdAtMillis)}", Modifier.padding(top = MareaSpace.xs))
        BodyText(
            "No hace falta preguntar qué pasó de inmediato: acompañe primero. Estado: ${statusLabel(r.status)}.",
            Modifier.padding(top = MareaSpace.xs, bottom = MareaSpace.m), color = MareaTextMuted
        )
        MareaPrimaryButton("Voy para allá", onClick = { onRespond(RequestStatus.VOY) }, enabled = !busy, tag = "adult_respond_voy")
        Spacer(Modifier.height(MareaSpace.s))
        if (r.status == RequestStatus.ENVIADA) {
            MareaSecondaryButton("Lo vi", onClick = { onRespond(RequestStatus.VISTA) }, enabled = !busy, tag = "adult_respond_vista")
        }
        MareaTextButton("No puedo ahora", onClick = { onRespond(RequestStatus.NO_PUEDO) }, tag = "adult_respond_no_puedo")
    }
}

@Composable
private fun SharedWeekCard(week: SharedWeek) {
    val order = listOf("TRANQUILA" to "Tranquila", "MOVIDA" to "Movida", "ALTA" to "Alta", "TORMENTA" to "Tormenta", "NO_SE" to "No sé")
    MareaCard(modifier = Modifier.testTag("adult_shared_week")) {
        Text("Semana que empezó el ${week.weekStart.split("-").let { "${it[2].toInt()}/${it[1].toInt()}" }}", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MareaTextDark)
        BodyText("Registró su marea ${week.daysWithRecord} de 7 días.", Modifier.padding(vertical = MareaSpace.xs))
        order.forEach { (key, label) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                BodyText(label)
                BodyText("${week.counts[key] ?: 0}")
            }
        }
        BodyText(
            "Son conteos, no un diagnóstico. Ninguna marea es buena o mala; si quiere conversar, pregunte con curiosidad y sin presionar.",
            Modifier.padding(top = MareaSpace.s), color = MareaTextMuted
        )
    }
}

@Composable
private fun AdultSettingsScreen(family: FamilyViewModel) {
    var confirmLeaveDevice by remember { mutableStateOf(false) }
    ScreenColumn {
        MareaHeader(title = "Configuración", subtitle = "Cuenta, privacidad y este teléfono.")
        WhatAdultsSeeCard(forChild = false)
        Spacer(Modifier.height(MareaSpace.l))
        MareaSecondaryButton("Cerrar sesión", onClick = { family.signOutAdult() }, tag = "adult_sign_out")
        Spacer(Modifier.height(MareaSpace.s))
        MareaTextButton("Usar este teléfono como el de un niño o niña", onClick = { confirmLeaveDevice = true }, tag = "adult_switch_role")
        BodyText(
            "Marea Club acompaña, pero no diagnostica ni reemplaza a un profesional. Si le preocupa la seguridad de su hijo o hija, llame a la Línea 106 o al 123.",
            Modifier.padding(top = MareaSpace.l), color = MareaTextMuted
        )
    }
    if (confirmLeaveDevice) {
        ConfirmDialog(
            title = "¿Cambiar de perfil en este teléfono?",
            text = "Se cerrará su sesión aquí. Su familia y el vínculo siguen guardados: puede volver a entrar con Google cuando quiera.",
            confirm = "Cambiar",
            onConfirm = {
                confirmLeaveDevice = false
                family.signOutAdult()
                family.setRole(null)
            },
            onDismiss = { confirmLeaveDevice = false }
        )
    }
}

@Composable
private fun AdultSignInScreen(family: FamilyViewModel, busy: Boolean, message: String?, modifier: Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    ScreenColumn(modifier) {
        MareaHeader(
            title = "Perfil de adulto",
            subtitle = "Inicie sesión para crear el espacio de su familia y vincular el teléfono de su hijo o hija.",
            onBack = { family.setRole(null) }
        )
        WhatAdultsSeeCard(forChild = false)
        Spacer(Modifier.height(MareaSpace.l))
        MareaPrimaryButton(
            text = if (busy) "Conectando…" else "Continuar con Google",
            enabled = !busy,
            onClick = {
                scope.launch {
                    requestGoogleIdToken(context)
                        .onSuccess { family.onGoogleIdToken(it) }
                        .onFailure { family.onGoogleError(it.message ?: "No se pudo iniciar sesión.") }
                }
            },
            tag = "adult_google_button"
        )
        ErrorText(message)
        BodyText(
            "Solo se guarda su nombre y el vínculo con la familia. Puede cerrar sesión cuando quiera.",
            Modifier.padding(top = MareaSpace.m), color = MareaTextMuted
        )
    }
}

@Composable
private fun AdultSetupScreen(family: FamilyViewModel, busy: Boolean, message: String?, modifier: Modifier) {
    var alias by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    ScreenColumn(modifier) {
        MareaHeader(title = "El espacio de su familia", subtitle = "Elija cómo empezar.")
        MareaCard {
            Text("Crear el espacio de mi familia", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MareaTextDark)
            BodyText("¿Cómo quiere llamar el perfil del niño o niña? Puede ser un apodo; es opcional.", Modifier.padding(vertical = MareaSpace.s))
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it.take(30) },
                label = { Text("Apodo (opcional)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("adult_child_alias")
            )
            Spacer(Modifier.height(MareaSpace.m))
            MareaPrimaryButton("Crear el espacio", onClick = { family.createFamily(alias) }, enabled = !busy, tag = "adult_create_family")
        }
        MareaSectionTitle("¿Otro adulto ya creó el espacio?", supporting = "Pídale un código de adulto y escríbalo aquí.")
        CodeField(code, { code = it }, tag = "adult_join_code")
        Spacer(Modifier.height(MareaSpace.m))
        MareaSecondaryButton("Unirme con el código", onClick = { family.joinAsSecondAdult(code) }, enabled = !busy && code.isNotBlank(), tag = "adult_join_button")
        ErrorText(message)
        Spacer(Modifier.height(MareaSpace.l))
        MareaTextButton("Cerrar sesión", onClick = { family.signOutAdult() }, tag = "adult_sign_out")
    }
}

@Composable
private fun AdultFamilyScreen(
    family: FamilyViewModel,
    info: FamilyInfo?,
    activeCode: ActiveCode?,
    busy: Boolean,
    message: String?,
    modifier: Modifier
) {
    var confirmUnlink by remember { mutableStateOf(false) }
    var confirmRemoveAdult by remember { mutableStateOf(false) }
    val myUid = family.myUid
    val isOwner = info != null && info.ownerUid == myUid

    ScreenColumn(modifier) {
        MareaHeader(title = "Familia", subtitle = info?.let { "Adultos: ${it.adultNameList().joinToString(" y ")}" } ?: "Cargando…")

        // Niño o niña
        MareaCard(modifier = Modifier.testTag("adult_child_card")) {
            Text(info?.childName?.ifBlank { "Perfil del niño o niña" } ?: "Perfil del niño o niña", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MareaTextDark)
            if (info?.hasChild == true) {
                BodyText("✓ Vinculado. Recibirá aquí sus pedidos de «Mis salvavidas».", Modifier.padding(top = MareaSpace.xs).testTag("adult_child_linked"))
                Spacer(Modifier.height(MareaSpace.s))
                MareaTextButton("Desvincular este perfil", onClick = { confirmUnlink = true }, tag = "adult_unlink_child")
            } else {
                BodyText("Aún no está vinculado. Genere un código y escríbalo en el teléfono del niño o niña, en el menú ⋯ › Mi familia.", Modifier.padding(top = MareaSpace.xs).testTag("adult_child_not_linked"))
                Spacer(Modifier.height(MareaSpace.m))
                MareaPrimaryButton("Generar código para el niño o niña", onClick = { family.generateCode(LinkRole.CHILD) }, enabled = !busy, tag = "adult_generate_child_code")
            }
            BodyText(
                "Si olvidó su PIN, genere un código nuevo: con él podrá crear otro PIN sin perder nada.",
                Modifier.padding(top = MareaSpace.s), color = MareaTextMuted
            )
            if (info?.hasChild == true) {
                MareaSecondaryButton("Generar código para recuperar el PIN", onClick = { family.generateCode(LinkRole.CHILD) }, enabled = !busy, tag = "adult_generate_pin_code")
            }
        }

        activeCode?.let { CodeCard(it, onDone = { family.discardCode() }) }
        ErrorText(message)

        // Segundo adulto
        MareaSectionTitle("Otro adulto", supporting = "Puede vincular a un segundo adulto de confianza (máximo dos).")
        if (info != null && info.adultUids.size >= 2) {
            val other = info.adultUids.firstOrNull { it != info.ownerUid }
            MareaCard {
                BodyText("Vinculado: ${other?.let { info.adultNames[it] } ?: "Adulto"}", Modifier.testTag("adult_second_linked"))
                if (isOwner) MareaTextButton("Quitar a este adulto", onClick = { confirmRemoveAdult = true }, tag = "adult_remove_second")
            }
        } else if (isOwner) {
            MareaSecondaryButton("Generar código para otro adulto", onClick = { family.generateCode(LinkRole.ADULT) }, enabled = !busy, tag = "adult_generate_adult_code")
        } else {
            BodyText("Solo quien creó el espacio puede invitar a otro adulto.", color = MareaTextMuted)
        }

    }

    if (confirmUnlink) {
        ConfirmDialog(
            title = "¿Desvincular el perfil?",
            text = "Dejará de recibir sus pedidos de «Mis salvavidas». Los datos del niño o niña siguen en su teléfono. Podrá vincularlo de nuevo con otro código.",
            confirm = "Desvincular",
            onConfirm = { confirmUnlink = false; family.unlinkChild() },
            onDismiss = { confirmUnlink = false }
        )
    }
    if (confirmRemoveAdult) {
        ConfirmDialog(
            title = "¿Quitar al otro adulto?",
            text = "Ya no recibirá los pedidos de «Mis salvavidas».",
            confirm = "Quitar",
            onConfirm = { confirmRemoveAdult = false; family.removeSecondAdult() },
            onDismiss = { confirmRemoveAdult = false }
        )
    }
}

@Composable
private fun CodeCard(code: ActiveCode, onDone: () -> Unit) {
    val until = remember(code) {
        SimpleDateFormat("h:mm a", Locale.forLanguageTag("es-CO")).format(Date(code.createdAtMillis + LinkCode.VALID_MINUTES * 60_000L))
    }
    MareaCard(modifier = Modifier.padding(top = MareaSpace.m).testTag("active_code_card"), background = Color(0xFFE3F2F8)) {
        Text(
            if (code.role == LinkRole.CHILD) "Código para el niño o niña" else "Código para el otro adulto",
            fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MareaTextDark
        )
        Text(
            LinkCode.format(code.code),
            fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = 3.sp,
            color = MareaDeepBlue, textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MareaSpace.m)
                .testTag("active_code_text")
        )
        BodyText(
            "Sirve una sola vez y vence a las $until (10 minutos). " +
                if (code.role == LinkRole.CHILD) "En su teléfono: menú ⋯ › Mi familia › escribir el código."
                else "El otro adulto debe instalar Marea Club, elegir «Soy un adulto», iniciar sesión y escribirlo.",
            color = MareaTextDark
        )
        MareaTextButton("Listo, ocultar el código", onClick = onDone, tag = "active_code_done")
    }
}

@Composable
private fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm, modifier = Modifier.testTag("confirm_button")) { Text(confirm) } },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_button")) { Text("Cancelar") } }
    )
}

// =====================================================================================
// Niño o niña
// =====================================================================================

/** Menú ⋯ › Mi familia: vincularse con un código o ver el vínculo. */
@Composable
fun ChildFamilyScreen(
    family: FamilyViewModel,
    userName: String,
    /** Lo que se compartiría de esta semana (calculado de los registros locales). */
    currentWeek: SharedWeek,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info by family.family.collectAsState()
    val busy by family.busy.collectAsState()
    val message by family.message.collectAsState()
    val hasPin by family.hasPin.collectAsState()
    val sharing by family.shareWeek.collectAsState()
    var code by remember { mutableStateOf("") }
    var confirmLeave by remember { mutableStateOf(false) }
    val linked = family.isChildLinked(info)

    ScreenColumn(modifier) {
        MareaHeader(
            title = "Mi familia",
            subtitle = if (linked) "Tu perfil está vinculado." else "Vincula tu perfil con un adulto de confianza para poder pedirle compañía.",
            onBack = onBack
        )
        if (linked) {
            MareaCard(modifier = Modifier.testTag("child_linked_card")) {
                Text("Vinculado con ${info?.adultNameList()?.joinToString(" y ").orEmpty()}", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MareaTextDark)
                BodyText("Desde «Mis salvavidas» puedes pedirle compañía cuando lo necesites.", Modifier.padding(top = MareaSpace.xs))
            }
            Spacer(Modifier.height(MareaSpace.m))
            MareaCard(modifier = Modifier.testTag("child_share_week_card")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Compartir mi semana", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MareaTextDark)
                        BodyText("Solo cuántas mareas de cada tipo registraste y en cuántos días. Nunca tus notas, tu diario ni por qué te sentiste así.")
                    }
                    Switch(
                        checked = sharing,
                        onCheckedChange = { family.setShareWeek(it, currentWeek) },
                        enabled = !busy,
                        modifier = Modifier.testTag("child_share_week_switch")
                    )
                }
                if (sharing) {
                    BodyText(
                        "Ahora se ve: " + currentWeek.counts.entries.joinToString(", ") { "${it.key.lowercase().replace("_", " ")} ${it.value}" }
                            .ifBlank { "ningún registro esta semana" } + " · ${currentWeek.daysWithRecord} de 7 días.",
                        Modifier.padding(top = MareaSpace.s).testTag("child_share_week_preview"),
                        color = MareaTextMuted
                    )
                }
            }
            ErrorText(message)
            Spacer(Modifier.height(MareaSpace.m))
            WhatAdultsSeeCard(forChild = true)
            if (!hasPin) {
                Spacer(Modifier.height(MareaSpace.m))
                MareaPrimaryButton("Crear mi PIN", onClick = { family.requestPinSetup() }, tag = "child_create_pin")
            }
            Spacer(Modifier.height(MareaSpace.l))
            MareaTextButton("Salir de la familia", onClick = { confirmLeave = true }, tag = "child_leave_family")
        } else {
            MareaCard {
                Text("¿Tienes un código?", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MareaTextDark)
                BodyText("Tu adulto lo genera en su teléfono, en Marea Club › Familia. Dura 10 minutos.", Modifier.padding(vertical = MareaSpace.s))
                CodeField(code, { code = it }, tag = "child_code_input")
                Spacer(Modifier.height(MareaSpace.m))
                MareaPrimaryButton(
                    text = if (busy) "Vinculando…" else "Vincular",
                    onClick = { family.childRedeem(code, userName) },
                    enabled = !busy && code.isNotBlank(),
                    tag = "child_link_button"
                )
                ErrorText(message)
            }
            Spacer(Modifier.height(MareaSpace.m))
            WhatAdultsSeeCard(forChild = true)
            BodyText("Vincularte necesita internet. Tus mareas, tu diario y tu isla siguen solo en este teléfono.", Modifier.padding(top = MareaSpace.m), color = MareaTextMuted)
        }
    }

    if (confirmLeave) {
        ConfirmDialog(
            title = "¿Salir de la familia?",
            text = "Ya no podrás enviar pedidos de «Mis salvavidas» hasta que te vincules de nuevo. Tus datos siguen en este teléfono.",
            confirm = "Salir",
            onConfirm = { confirmLeave = false; family.childLeave() },
            onDismiss = { confirmLeave = false }
        )
    }
}

/** El niño crea su PIN (después de vincularse o al recuperarlo). */
@Composable
fun PinSetupScreen(family: FamilyViewModel, modifier: Modifier = Modifier) {
    var pin by remember { mutableStateOf("") }
    var again by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf<String?>(null) }
    ScreenColumn(modifier) {
        MareaHeader(title = "Crea tu PIN", subtitle = "Lo pediremos al abrir Marea Club para que nadie más vea tu espacio. Usa de 4 a 6 números que recuerdes.")
        PinField(pin, { pin = it; problem = null }, "PIN", "pin_new")
        Spacer(Modifier.height(MareaSpace.m))
        PinField(again, { again = it; problem = null }, "Escríbelo otra vez", "pin_confirm")
        problem?.let { MareaNotice(text = it, tone = NoticeTone.SUPPORT, tag = "pin_problem", modifier = Modifier.padding(top = MareaSpace.m)) }
        Spacer(Modifier.height(MareaSpace.l))
        MareaPrimaryButton("Guardar mi PIN", onClick = {
            problem = family.savePin(pin, again)
        }, enabled = pin.length >= PinRules.MIN_LENGTH, tag = "pin_save")
        BodyText("Si lo olvidas, tu adulto puede darte un código nuevo para crear otro. No perderás nada.", Modifier.padding(top = MareaSpace.m), color = MareaTextMuted)
    }
}

/** Pantalla de PIN al abrir la app. */
@Composable
fun PinEntryScreen(family: FamilyViewModel, userName: String, modifier: Modifier = Modifier) {
    var pin by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var forgot by remember { mutableStateOf(false) }

    if (forgot) {
        ForgotPinScreen(family, userName, onBack = { forgot = false }, modifier = modifier)
        return
    }
    ScreenColumn(modifier) {
        Spacer(Modifier.height(MareaSpace.xxl))
        MareaHeader(title = "Hola, $userName", subtitle = "Escribe tu PIN para entrar a tu espacio.")
        PinField(pin, { pin = it; feedback = null }, "PIN", "pin_entry")
        feedback?.let { MareaNotice(text = it, tone = NoticeTone.SUPPORT, tag = "pin_feedback", modifier = Modifier.padding(top = MareaSpace.m)) }
        Spacer(Modifier.height(MareaSpace.l))
        MareaPrimaryButton("Entrar", onClick = {
            feedback = when (val r = family.checkPin(pin)) {
                PinCheck.Ok -> null
                is PinCheck.Wrong -> "PIN incorrecto. " + if (r.attemptsBeforeWait > 0) "Te quedan ${r.attemptsBeforeWait} intentos antes de una pausa." else ""
                is PinCheck.Locked -> "Demasiados intentos. Podrás intentar de nuevo a las " +
                    SimpleDateFormat("h:mm:ss a", Locale.forLanguageTag("es-CO")).format(Date(r.untilMillis)) + "."
            }
            pin = ""
        }, enabled = pin.length >= PinRules.MIN_LENGTH, tag = "pin_enter")
        MareaTextButton("Olvidé mi PIN", onClick = { forgot = true }, tag = "pin_forgot")
    }
}

@Composable
private fun ForgotPinScreen(family: FamilyViewModel, userName: String, onBack: () -> Unit, modifier: Modifier) {
    val busy by family.busy.collectAsState()
    val message by family.message.collectAsState()
    var code by remember { mutableStateOf("") }
    ScreenColumn(modifier) {
        MareaHeader(
            title = "¿Olvidaste tu PIN?",
            subtitle = "Pide a tu adulto que genere un código en su teléfono (Marea Club › Familia). Con él crearás un PIN nuevo sin perder nada.",
            onBack = onBack
        )
        CodeField(code, { code = it }, tag = "forgot_code_input")
        Spacer(Modifier.height(MareaSpace.m))
        MareaPrimaryButton(
            text = if (busy) "Comprobando…" else "Usar el código",
            onClick = { family.childRedeem(code, userName, resettingPin = true) },
            enabled = !busy && code.isNotBlank(),
            tag = "forgot_code_button"
        )
        ErrorText(message)
    }
}
