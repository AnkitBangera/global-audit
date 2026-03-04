package com.landmarkgroup.globalaudit.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Adds safe area padding to respect system bars (status bar and navigation bar).
 * This ensures content is not cut off by system UI elements.
 */
@Composable
fun Modifier.safeAreaPadding(): Modifier {
    return this.padding(WindowInsets.systemBars.asPaddingValues())
}

/**
 * Adds safe area padding only for top (status bar).
 * Useful when you want to handle bottom padding separately.
 */
@Composable
fun Modifier.safeAreaTopPadding(): Modifier {
    return this.padding(WindowInsets.statusBars.asPaddingValues())
}

/**
 * Adds safe area padding only for bottom (navigation bar).
 * Useful when you want to handle top padding separately.
 */
@Composable
fun Modifier.safeAreaBottomPadding(): Modifier {
    return this.padding(WindowInsets.navigationBars.asPaddingValues())
}

/**
 * Adds safe area padding for IME (keyboard) when it's visible.
 * Useful for ensuring content is visible above the keyboard.
 */
@Composable
fun Modifier.safeAreaImePadding(): Modifier {
    return this.padding(WindowInsets.ime.asPaddingValues())
}
