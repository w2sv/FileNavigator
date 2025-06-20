package com.w2sv.filenavigator.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen : NavKey {
    @Serializable
    object Home : Screen()

    @Serializable
    object AppSettings : Screen()

    @Serializable
    object RequiredPermissions : Screen()

    @Serializable
    object NavigatorSettings : Screen()

    companion object {
        fun initial(anyPermissionMissing: Boolean): Screen =
            if (anyPermissionMissing) RequiredPermissions else Home
    }
}
