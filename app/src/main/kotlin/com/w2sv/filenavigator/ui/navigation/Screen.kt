package com.w2sv.filenavigator.ui.navigation

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.w2sv.modules.resources.R
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(@StringRes val titleRes: Int) : NavKey {

    @Serializable
    object Home : Screen(R.string.app_name)

    @Serializable
    object AppSettings : Screen(R.string.app_settings)

    @Serializable
    object Permissions : Screen(R.string.required_permissions)

    @Serializable
    object NavigatorSettings : Screen(R.string.navigator_settings)

    val isBottomNavDestination get() = this != Permissions
    val isPermissions get() = this == Permissions
    val isHome get() = this == Home
    val isNavigatorSettings get() = this == NavigatorSettings
}
