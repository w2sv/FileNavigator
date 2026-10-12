package com.w2sv.filenavigator.ui.navigation.bottom

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.w2sv.modules.resources.R
import kotlinx.serialization.Serializable

@Serializable
sealed class BottomNavRoute(@StringRes val titleRes: Int) : NavKey {

    @Serializable
    data object Home : BottomNavRoute(R.string.app_name)

    @Serializable
    data object AppSettings : BottomNavRoute(R.string.app_settings)

    @Serializable
    data object NavigatorSettings : BottomNavRoute(R.string.navigator_settings)
}
