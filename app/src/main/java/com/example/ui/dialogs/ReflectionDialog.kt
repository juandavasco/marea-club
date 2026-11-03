package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.*

@Composable
fun ReflectionDialog(
    onDismiss: () -> Unit,
    onSaveReflection: (String) -> Unit
) {
    var reflectionText by remember { mutableStateOf("") }

    val reflectionPrompts = listOf(
        "¿Qué marea sentiste con más fuerza esta semana?",
        "¿Hubo algún momento de calma que disfrutaste?",
        "¿Qué descubriste de ti cuando la marea subió?",
        "Una palabra o frase para tu mar interior hoy:"
    )
    var selectedPrompt by remember { mutableStateOf(reflectionPrompts.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MareaCardLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.muni_isotipo),
                                contentDescription = "Muni",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "Un momento para ti",
                            fontFamily = SnigletFontFamily,
                            fontSize = 18.sp,
                            color = MareaTextDark
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = selectedPrompt,
                    fontFamily = SnigletFontFamily,
                    fontSize = 14.sp,
                    color = MareaDeepBlue,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    placeholder = {
                        Text(
                            text = "Escribe libremente lo que sientes o descubriste...",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = MareaTextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("reflection_text_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MareaBackground,
                        unfocusedContainerColor = MareaBackground,
                        focusedBorderColor = MareaTealBar,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Suggest other prompts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextButton(
                        onClick = {
                            val currentIndex = reflectionPrompts.indexOf(selectedPrompt)
                            val nextIndex = (currentIndex + 1) % reflectionPrompts.size
                            selectedPrompt = reflectionPrompts[nextIndex]
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cambiar pregunta 🔄",
                            fontFamily = SnigletFontFamily,
                            fontSize = 11.sp,
                            color = MareaDeepBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (reflectionText.isNotBlank()) {
                            onSaveReflection(reflectionText)
                        }
                    },
                    enabled = reflectionText.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MareaDeepBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_reflection_button")
                ) {
                    Text(
                        text = "Guardar en mi diario 🌊",
                        fontFamily = SnigletFontFamily,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
