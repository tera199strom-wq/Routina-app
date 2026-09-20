package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Define Fredoka FontFamily with balanced, lighter weights to prevent text from appearing overly thick
val FredokaOneFontFamily = FontFamily(
    Font(R.font.fredoka_light, FontWeight.Thin),
    Font(R.font.fredoka_light, FontWeight.ExtraLight),
    Font(R.font.fredoka_light, FontWeight.Light),
    Font(R.font.fredoka_light, FontWeight.Normal),
    Font(R.font.fredoka_regular, FontWeight.Medium),
    Font(R.font.fredoka_medium, FontWeight.SemiBold),
    Font(R.font.fredoka_semibold, FontWeight.Bold),
    Font(R.font.fredoka_semibold, FontWeight.ExtraBold),
    Font(R.font.fredoka_semibold, FontWeight.Black)
)

private val defaultTypography = Typography()

// Complete Material 3 typography suite using Fredoka One
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = FredokaOneFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = FredokaOneFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = FredokaOneFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = FredokaOneFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = FredokaOneFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = FredokaOneFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = FredokaOneFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = FredokaOneFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = FredokaOneFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = FredokaOneFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = FredokaOneFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = FredokaOneFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = FredokaOneFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = FredokaOneFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = FredokaOneFontFamily)
)
