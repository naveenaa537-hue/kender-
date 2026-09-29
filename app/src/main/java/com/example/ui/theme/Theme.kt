package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeSetting(val title: String, val previewColor: Color) {
    LAVENDER_DUSK("Lavender Dusk", LavenderPrimary),
    SUNSET_GLOW("Sunset Glow", SunsetPrimaryLight),
    EMERALD_FOREST("Emerald Forest", EmeraldPrimaryLight),
    OCEAN_BREEZE("Ocean Breeze", OceanPrimaryLight),
    ROSE_ROMANCE("Rose Romance", RosePrimaryLight),
    MIDNIGHT_NEON("Midnight Neon", NeonPrimary)
}

fun getCustomColorScheme(
    themeName: String,
    darkTheme: Boolean
): ColorScheme {
    return when (themeName) {
        "SUNSET_GLOW" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFF8A65),
                onPrimary = Color(0xFF3E0A00),
                primaryContainer = SunsetPrimary,
                secondary = Color(0xFFFFB74D),
                background = SunsetBackgroundDark,
                surface = Color(0xFF261814)
            )
        } else {
            lightColorScheme(
                primary = SunsetPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = SunsetPrimaryContainer,
                secondary = SunsetSecondary,
                background = SunsetBackgroundLight,
                surface = Color.White
            )
        }

        "EMERALD_FOREST" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF80CBC4),
                onPrimary = Color(0xFF003730),
                primaryContainer = EmeraldPrimary,
                secondary = Color(0xFFA5D6A7),
                background = EmeraldBackgroundDark,
                surface = Color(0xFF14241E)
            )
        } else {
            lightColorScheme(
                primary = EmeraldPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = EmeraldPrimaryContainer,
                secondary = EmeraldSecondary,
                background = EmeraldBackgroundLight,
                surface = Color.White
            )
        }

        "OCEAN_BREEZE" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF81D4FA),
                onPrimary = Color(0xFF00344F),
                primaryContainer = OceanPrimary,
                secondary = Color(0xFF80DEEA),
                background = OceanBackgroundDark,
                surface = Color(0xFF132431)
            )
        } else {
            lightColorScheme(
                primary = OceanPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = OceanPrimaryContainer,
                secondary = OceanSecondary,
                background = OceanBackgroundLight,
                surface = Color.White
            )
        }

        "ROSE_ROMANCE" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFF48FB1),
                onPrimary = Color(0xFF4C0020),
                primaryContainer = RosePrimary,
                secondary = Color(0xFFFF80AB),
                background = RoseBackgroundDark,
                surface = Color(0xFF28131C)
            )
        } else {
            lightColorScheme(
                primary = RosePrimaryLight,
                onPrimary = Color.White,
                primaryContainer = RosePrimaryContainer,
                secondary = RoseSecondary,
                background = RoseBackgroundLight,
                surface = Color.White
            )
        }

        "MIDNIGHT_NEON" -> darkColorScheme(
            primary = NeonPrimary,
            onPrimary = Color.Black,
            secondary = NeonSecondary,
            onSecondary = Color.Black,
            tertiary = NeonTertiary,
            background = NeonBackgroundDark,
            surface = NeonSurfaceDark
        )

        else -> if (darkTheme) { // LAVENDER_DUSK default
            darkColorScheme(
                primary = LavenderPrimaryContainer,
                onPrimary = LavenderOnPrimaryContainer,
                secondary = LavenderSecondaryContainer,
                background = LavenderBackgroundDark,
                surface = LavenderSurfaceDark
            )
        } else {
            lightColorScheme(
                primary = LavenderPrimary,
                onPrimary = LavenderOnPrimary,
                primaryContainer = LavenderPrimaryContainer,
                onPrimaryContainer = LavenderOnPrimaryContainer,
                secondary = LavenderSecondary,
                secondaryContainer = LavenderSecondaryContainer,
                background = LavenderBackgroundLight,
                surface = LavenderSurfaceLight
            )
        }
    }
}

@Composable
fun KindredTheme(
    themeName: String = "LAVENDER_DUSK",
    darkTheme: Boolean = isSystemInDarkTheme(),
    isSystemTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val effectiveDark = if (isSystemTheme) isSystemInDarkTheme() else darkTheme

    val colorScheme = if (themeName == "SYSTEM_DYNAMIC" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        getCustomColorScheme(themeName, effectiveDark)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
