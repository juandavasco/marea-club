package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BottomNavTab
import com.example.ui.theme.MareaDeepBlue
import com.example.ui.theme.MareaTealBar
import com.example.ui.theme.QuicksandFontFamily

@Composable
fun MareaBottomBar(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MareaTealBar)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Inicio
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Inicio",
                isSelected = currentTab == BottomNavTab.HOME,
                tag = "tab_home",
                onClick = { onTabSelected(BottomNavTab.HOME) }
            )

            // 2. Marea
            BottomNavItem(
                icon = Icons.Default.Waves,
                label = "Mareas",
                isSelected = currentTab == BottomNavTab.MAREA,
                tag = "tab_marea",
                onClick = { onTabSelected(BottomNavTab.MAREA) }
            )

            // 3. Mi isla
            BottomNavItem(
                icon = Icons.Default.Landscape,
                label = "Mi isla",
                isSelected = currentTab == BottomNavTab.ISLAND,
                tag = "tab_island",
                onClick = { onTabSelected(BottomNavTab.ISLAND) }
            )

            // 4. Mi progreso
            BottomNavItem(
                icon = Icons.Default.BarChart,
                label = "Mi progreso",
                isSelected = currentTab == BottomNavTab.PROGRESS,
                tag = "tab_progress",
                onClick = { onTabSelected(BottomNavTab.PROGRESS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val pillColor by animateColorAsState(
        targetValue = if (isSelected) MareaDeepBlue.copy(alpha = 0.35f) else Color.Transparent,
        label = "pill_color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
        label = "content_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "nav_scale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(pillColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontFamily = QuicksandFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = contentColor
        )
    }
}
