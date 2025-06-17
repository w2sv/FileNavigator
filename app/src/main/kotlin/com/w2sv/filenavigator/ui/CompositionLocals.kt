package com.w2sv.filenavigator.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.google.accompanist.permissions.PermissionState
import com.w2sv.domain.usecase.MoveDestinationPathConverter

val LocalMoveDestinationPathConverter =
    staticCompositionLocalOf<MoveDestinationPathConverter> {
        noCompositionLocalProvidedFor("LocalMoveDestinationPathConverter")
    }

val LocalUseDarkTheme =
    compositionLocalOf { false }

val LocalPostNotificationsPermissionState =
    staticCompositionLocalOf<PermissionState> {
        noCompositionLocalProvidedFor("LocalPostNotificationsPermissionState")
    }

fun noCompositionLocalProvidedFor(name: String): Nothing {
    error("$name not provided")
}
