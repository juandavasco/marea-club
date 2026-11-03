package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.*

@Composable
fun RewardCelebrationDialog(
    rewardTitle: String,
    rewardIcon: String,
    onDismiss: () -> Unit,
    onGoToIsland: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡Nuevo elemento en tu isla!",
                    fontFamily = SnigletFontFamily,
                    fontSize = 21.sp,
                    color = MareaDeepBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = rewardIcon, fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = rewardTitle,
                    fontFamily = SnigletFontFamily,
                    fontSize = 18.sp,
                    color = MareaTextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.muni_waving),
                        contentDescription = "Muni",
                        modifier = Modifier.size(46.dp)
                    )
                    Text(
                        text = "Tu compromiso con el autocuidado florece en tu isla. ¡Muni está feliz de acompañarte!",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        color = MareaTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onGoToIsland()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("go_to_island_reward_button")
                ) {
                    Text("Ver mi isla ahora ➔", fontFamily = SnigletFontFamily, fontSize = 15.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Continuar explorando", fontFamily = SnigletFontFamily, fontSize = 13.sp, color = MareaTextMuted)
                }
            }
        }
    }
}
