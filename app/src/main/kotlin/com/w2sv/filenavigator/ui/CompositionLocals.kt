package com.w2sv.filenavigator.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.filenavigator.ui.navigation.Navigator

val LocalMoveDestinationPathConverter = staticCompositionLocalOf<MoveDestinationPathConverter> {
    noCompositionLocalProvidedFor("LocalMoveDestinationPathConverter")
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    noCompositionLocalProvidedFor("LocalNavigator")
}

private fun noCompositionLocalProvidedFor(name: String): Nothing {
    error("$name not provided")
}
