package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Official Marea Club Brand Typography: Sniglet Regular
val SnigletFontFamily = FontFamily(
    Font(R.font.sniglet, FontWeight.Normal)
)

val QuicksandFontFamily = FontFamily(
    Font(R.font.quicksand, FontWeight.Normal),
    Font(R.font.quicksand, FontWeight.Medium),
    Font(R.font.quicksand, FontWeight.SemiBold),
    Font(R.font.quicksand, FontWeight.Bold)
)

val NunitoFontFamily = FontFamily(
    Font(R.font.nunito, FontWeight.Normal),
    Font(R.font.nunito, FontWeight.Medium),
    Font(R.font.nunito, FontWeight.SemiBold),
    Font(R.font.nunito, FontWeight.Bold)
)

val MareaTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SnigletFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        color = MareaTextDark
    ),
    headlineMedium = TextStyle(
        fontFamily = SnigletFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp,
        lineHeight = 31.sp,
        color = MareaTextDark
    ),
    titleLarge = TextStyle(
        fontFamily = SnigletFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        color = MareaTextDark
    ),
    titleMedium = TextStyle(
        fontFamily = SnigletFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = MareaTextDark
    ),
    titleSmall = TextStyle(
        fontFamily = QuicksandFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = MareaTextDark
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = MareaTextDark
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = MareaTextMuted
    ),
    labelLarge = TextStyle(
        fontFamily = SnigletFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 19.sp,
        color = MareaTextDark
    ),
    labelMedium = TextStyle(
        fontFamily = QuicksandFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = MareaTextMuted
    )
)
