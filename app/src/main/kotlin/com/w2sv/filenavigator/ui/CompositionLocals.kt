package com.w2sv.filenavigator.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.util.snackbar.SnackbarVisibility

val LocalMoveDestinationLabelProvider = staticCompositionLocalOf<MoveDestinationLabelProvider> {
    noCompositionLocalProvidedFor("LocalMoveDestinationLabelProvider")
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    noCompositionLocalProvidedFor("LocalNavigator")
}

val LocalSnackbarVisibility = staticCompositionLocalOf { SnackbarVisibility() }

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

private fun noCompositionLocalProvidedFor(name: String): Nothing {
    error("$name not provided")
}
