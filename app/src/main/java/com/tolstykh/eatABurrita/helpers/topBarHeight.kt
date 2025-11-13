package com.tolstykh.eatABurrita.helpers

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

@Composable
fun statusBarHeight(): Dp {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return statusBarHeight
}
