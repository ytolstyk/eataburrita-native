package com.tolstykh.eatABurrita.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ColorFamily(
    val iconTint: Color,
    val iconBackground: Color,
    val iconOutline: Color,
)

data class ExtendedColorScheme(
    val extra: ColorFamily = extendedLight.extra,
)

val extendedLight = ExtendedColorScheme(
    extra = ColorFamily(
        iconTint = BlackText,
        iconBackground = WhiteText,
        iconOutline = DarkIconOutline,
    ),
)
val extendedDark = ExtendedColorScheme(
    extra = ColorFamily(
        iconTint = WhiteText,
        iconBackground = BlackText,
        iconOutline = DarkIconOutline,
    ),
)
val LocalExColorScheme = staticCompositionLocalOf { ExtendedColorScheme() }