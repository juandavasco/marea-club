package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.MareaTurquoise

@Composable
fun MareaTopBar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 3 cyan dots as seen in all mockups
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onMenuClick() }
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .testTag("menu_dots_button"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color = MareaTurquoise, shape = CircleShape)
                )
            }
        }

        // Official Marea Club Logo
        Image(
            painter = painterResource(id = R.drawable.marea_logo),
            contentDescription = "Marea Club Logo",
            modifier = Modifier
                .height(34.dp)
                .testTag("marea_club_logo")
        )
    }
}
