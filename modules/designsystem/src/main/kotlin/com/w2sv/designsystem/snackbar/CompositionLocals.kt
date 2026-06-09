package com.w2sv.designsystem.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSnackbarVisibility = staticCompositionLocalOf { SnackbarVisibility() }

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }
