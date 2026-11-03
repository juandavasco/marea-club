package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/** Texto informativo de privacidad (decisión 7). No reemplaza un consentimiento formal revisado jurídicamente. */
object PrivacyText {
    const val TITLE = "Tus datos se quedan contigo"
    val POINTS = listOf(
        "Lo que registras (mareas, notas, diario, reflexiones y consultas) se guarda cifrado solo en este teléfono.",
        "La app no usa internet, no tiene cuentas y no hace copia de seguridad en la nube.",
        "Nadie del equipo, ni tu familia, ni tu colegio puede ver lo que escribes desde otro teléfono.",
        "Puedes borrar un registro o todos tus datos cuando quieras desde el menú.",
        "Marea Club acompaña, pero no diagnostica ni reemplaza a un profesional. Si necesitas ayuda, usa las rutas de apoyo.",
        "Si tienes menos de 18 años, cuéntale a tu acudiente que usas Marea Club."
    )
}

@Composable
fun PrivacyInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("privacy_dialog"),
        title = { Text(PrivacyText.TITLE, fontFamily = SnigletFontFamily, color = MareaTextDark) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                PrivacyText.POINTS.forEach { point ->
                    Text(
                        text = "• $point",
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        color = MareaTextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("privacy_dialog_ok")) {
                Text("Entendido", color = MareaDeepBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/** Reinicio protegido (Documento Técnico §18): hay que marcar la casilla para habilitar el borrado. */
@Composable
fun ResetDataDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var understood by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("reset_dialog"),
        title = { Text("¿Borrar todos tus datos?", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Se borrarán tus mareas, notas, diario, reflexiones, consultas y el progreso de tu isla. La app quedará como recién instalada.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = understood, onValueChange = { understood = it }, role = Role.Checkbox)
                        .testTag("reset_understood_checkbox")
                ) {
                    Checkbox(checked = understood, onCheckedChange = null)
                    Text("Entiendo que no se puede deshacer", fontFamily = NunitoFontFamily, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = understood,
                modifier = Modifier.testTag("reset_confirm_button")
            ) { Text("Borrar todo", color = if (understood) Color(0xFF9A2A10) else MareaTextMuted) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = MareaTextDark) }
        }
    )
}
