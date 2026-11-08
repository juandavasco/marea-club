package com.example.data.family

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementación con Firebase (plan Spark gratuito, sin Cloud Functions).
 * Las reglas de seguridad (firebase/firestore.rules) validan cada escritura en el servidor.
 */
class FirebaseFamilyBackend : FamilyBackend {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override val currentUid: String? get() = auth.currentUser?.uid
    override val isSignedInWithGoogle: Boolean get() = auth.currentUser?.isAnonymous == false
    override val currentDisplayName: String? get() = auth.currentUser?.takeIf { !it.isAnonymous }?.let { firstName(it.displayName) }

    override suspend fun signInWithGoogle(idToken: String): AdultAccount = network {
        val user = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user
            ?: throw BackendUnavailable()
        AdultAccount(user.uid, firstName(user.displayName))
    }

    override suspend fun signInAnonymously(): String = network {
        auth.currentUser?.uid ?: auth.signInAnonymously().await().user?.uid ?: throw BackendUnavailable()
    }

    override fun signOut() = auth.signOut()

    override suspend fun loadProfile(): RemoteProfile? = network {
        val uid = currentUid ?: return@network null
        val snap = db.collection(USERS).document(uid).get(Source.SERVER).await()
        if (!snap.exists()) return@network null
        val role = when (snap.getString("role")) {
            "adult" -> DeviceRole.ADULT
            "child" -> DeviceRole.CHILD
            else -> return@network null
        }
        RemoteProfile(role, snap.getString("familyId") ?: return@network null)
    }

    override suspend fun createFamily(adultName: String, childName: String): String = network {
        val uid = currentUid ?: throw BackendUnavailable()
        val family = db.collection(FAMILIES).document()
        db.batch()
            .set(
                family, mapOf(
                    "ownerUid" to uid,
                    "adultUids" to listOf(uid),
                    "adultNames" to mapOf(uid to adultName.take(MAX_NAME)),
                    "childUid" to null,
                    "childName" to childName.take(MAX_NAME),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "lastLinkCode" to ""
                )
            )
            .set(
                db.collection(USERS).document(uid),
                mapOf("role" to "adult", "familyId" to family.id, "displayName" to adultName.take(MAX_NAME))
            )
            .commit().await()
        family.id
    }

    override suspend fun publishLinkCode(familyId: String, role: LinkRole, codeHash: String) {
        network {
            val uid = currentUid ?: throw BackendUnavailable()
            db.collection(LINK_CODES).document(codeHash).set(
                mapOf(
                    "familyId" to familyId,
                    "role" to role.wire,
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "used" to false,
                    "usedBy" to null
                )
            ).await()
        }
    }

    override suspend fun redeemLinkCode(codeHash: String, role: LinkRole, displayName: String): LinkResult = try {
        network {
            val uid = currentUid ?: throw BackendUnavailable()
            // Leer del servidor (no de la caché): si no hay conexión falla de inmediato.
            val code = db.collection(LINK_CODES).document(codeHash).get(Source.SERVER).await()
            val familyId = code.getString("familyId")
            if (!code.exists() || familyId == null || code.getString("role") != role.wire || code.getBoolean("used") != false) {
                return@network LinkResult.InvalidCode
            }
            val familyRef = db.collection(FAMILIES).document(familyId)
            val batch = db.batch()
            batch.update(code.reference, mapOf("used" to true, "usedBy" to uid))
            when (role) {
                LinkRole.CHILD -> batch.update(familyRef, mapOf("childUid" to uid, "lastLinkCode" to codeHash))
                // El segundo adulto no puede leer la familia antes de entrar: se agrega con arrayUnion.
                LinkRole.ADULT -> batch.update(
                    familyRef, mapOf(
                        "adultUids" to FieldValue.arrayUnion(uid),
                        "adultNames.$uid" to displayName.take(MAX_NAME),
                        "lastLinkCode" to codeHash
                    )
                )
            }
            batch.set(
                db.collection(USERS).document(uid),
                mapOf("role" to if (role == LinkRole.CHILD) "child" else "adult", "familyId" to familyId, "displayName" to displayName.take(MAX_NAME))
            )
            batch.commit().await()
            LinkResult.Linked(familyId)
        }
    } catch (e: BackendUnavailable) {
        LinkResult.NoConnection
    } catch (e: FirebaseFirestoreException) {
        // PERMISSION_DENIED: el código venció, ya se usó o no es de esta familia.
        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) LinkResult.InvalidCode else LinkResult.NoConnection
    }

    override fun observeFamily(familyId: String, onChange: (FamilyInfo?) -> Unit): Subscription {
        val registration = db.collection(FAMILIES).document(familyId).addSnapshotListener { snap, error ->
            if (error != null || snap == null || !snap.exists()) {
                onChange(null)
                return@addSnapshotListener
            }
            @Suppress("UNCHECKED_CAST")
            onChange(
                FamilyInfo(
                    id = snap.id,
                    ownerUid = snap.getString("ownerUid") ?: "",
                    adultUids = (snap.get("adultUids") as? List<String>).orEmpty(),
                    adultNames = (snap.get("adultNames") as? Map<String, String>).orEmpty(),
                    childUid = snap.getString("childUid"),
                    childName = snap.getString("childName") ?: ""
                )
            )
        }
        return Subscription { registration.remove() }
    }

    override suspend fun unlinkChild(familyId: String) {
        network {
            db.batch()
                .update(db.collection(FAMILIES).document(familyId), "childUid", null)
                .delete(weekDoc(familyId))
                .commit().await()
        }
    }

    override suspend fun removeSecondAdult(familyId: String, ownerUid: String) {
        network { db.collection(FAMILIES).document(familyId).update("adultUids", listOf(ownerUid)).await() }
    }

    override suspend fun childLeaves(familyId: String) {
        network {
            // Primero se borra lo compartido (después ya no tendría permiso).
            weekDoc(familyId).delete().await()
            db.collection(FAMILIES).document(familyId).update("childUid", null).await()
        }
    }

    // ------------------------------------------------------------------ Mis salvavidas

    private fun requests(familyId: String) = db.collection(FAMILIES).document(familyId).collection("requests")

    override suspend fun sendCompanionRequest(familyId: String, toUid: String, reason: CompanionReason): String = network {
        val uid = currentUid ?: throw BackendUnavailable()
        // Comprobar conexión primero: sin ella, Firestore guardaría el pedido en cola y lo enviaría
        // más tarde, cuando el niño ya no lo espera. Mejor avisar que no se envió.
        db.collection(FAMILIES).document(familyId).get(Source.SERVER).await()
        val ref = requests(familyId).document()
        ref.set(
            mapOf(
                "childUid" to uid,
                "toUid" to toUid,
                "reason" to reason.wire,
                "createdAt" to FieldValue.serverTimestamp(),
                "status" to RequestStatus.ENVIADA.wire,
                "respondedAt" to null
            )
        ).await()
        ref.id
    }

    override fun observeRequests(familyId: String, asChild: Boolean, onChange: (List<CompanionRequest>) -> Unit): Subscription {
        val uid = currentUid ?: return Subscription { }
        // La consulta debe coincidir con las reglas: el adulto solo puede pedir los suyos.
        val query = requests(familyId).whereEqualTo(if (asChild) "childUid" else "toUid", uid)
        val registration = query.addSnapshotListener { snap, error ->
            if (error != null || snap == null) return@addSnapshotListener
            onChange(
                snap.documents.map { d ->
                    CompanionRequest(
                        id = d.id,
                        childUid = d.getString("childUid") ?: "",
                        toUid = d.getString("toUid") ?: "",
                        reason = CompanionReason.fromWire(d.getString("reason")),
                        // Mientras el servidor no confirma la hora, se usa la del teléfono
                        createdAtMillis = d.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                        status = RequestStatus.fromWire(d.getString("status")),
                        respondedAtMillis = d.getTimestamp("respondedAt")?.toDate()?.time
                    )
                }.sortedByDescending { it.createdAtMillis }
            )
        }
        return Subscription { registration.remove() }
    }

    override suspend fun respondToRequest(familyId: String, requestId: String, status: RequestStatus) {
        network {
            requests(familyId).document(requestId)
                .update(mapOf("status" to status.wire, "respondedAt" to FieldValue.serverTimestamp())).await()
        }
    }

    override suspend fun childUpdateRequest(familyId: String, requestId: String, status: RequestStatus) {
        network { requests(familyId).document(requestId).update("status", status.wire).await() }
    }

    // ------------------------------------------------------------------ Compartir mi semana

    private fun weekDoc(familyId: String) = db.collection(FAMILIES).document(familyId).collection("shared").document("week")

    override suspend fun shareWeek(familyId: String, week: SharedWeek) {
        network {
            weekDoc(familyId).set(
                mapOf(
                    "weekStart" to week.weekStart,
                    "counts" to week.counts,
                    "daysWithRecord" to week.daysWithRecord,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    override suspend fun stopSharingWeek(familyId: String) {
        network { weekDoc(familyId).delete().await() }
    }

    override fun observeSharedWeek(familyId: String, onChange: (SharedWeek?) -> Unit): Subscription {
        val registration = weekDoc(familyId).addSnapshotListener { snap, error ->
            if (error != null || snap == null || !snap.exists()) {
                onChange(null)
                return@addSnapshotListener
            }
            @Suppress("UNCHECKED_CAST")
            val counts = (snap.get("counts") as? Map<String, Number>).orEmpty().mapValues { it.value.toInt() }
            onChange(
                SharedWeek(
                    weekStart = snap.getString("weekStart") ?: "",
                    counts = counts,
                    daysWithRecord = (snap.getLong("daysWithRecord") ?: 0).toInt(),
                    updatedAtMillis = snap.getTimestamp("updatedAt")?.toDate()?.time ?: 0
                )
            )
        }
        return Subscription { registration.remove() }
    }

    /** Las escrituras de Firestore esperan conexión indefinidamente: se corta a los 20 s. */
    private suspend fun <T> network(block: suspend () -> T): T = try {
        withTimeout(TIMEOUT_MS) { block() }
    } catch (e: TimeoutCancellationException) {
        throw BackendUnavailable(e)
    } catch (e: FirebaseFirestoreException) {
        if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) throw BackendUnavailable(e) else throw e
    } catch (e: com.google.firebase.FirebaseNetworkException) {
        throw BackendUnavailable(e)
    }

    private fun firstName(name: String?): String =
        name?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Adulto"

    companion object {
        const val FAMILIES = "families"
        const val USERS = "users"
        const val LINK_CODES = "linkCodes"
        private const val MAX_NAME = 30
        private const val TIMEOUT_MS = 20_000L
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) cont.resume(task.result)
        else cont.resumeWithException(task.exception ?: IllegalStateException("Tarea de Firebase sin resultado"))
    }
}
