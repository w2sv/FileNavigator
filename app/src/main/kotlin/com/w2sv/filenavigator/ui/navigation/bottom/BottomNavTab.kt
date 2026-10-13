package com.w2sv.filenavigator.ui.navigation.bottom

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.w2sv.modules.resources.R

enum class BottomNavTab(val route: BottomNavRoute, @StringRes val labelRes: Int, val icon: ImageVector) {
    Home(
        route = BottomNavRoute.Home,
        labelRes = R.string.home,
        icon = Icons.Outlined.Dashboard
    ),
    NavigatorSettings(
        route = BottomNavRoute.NavigatorSettings,
        labelRes = R.string.navigator_screen_title,
        icon = Icons.Outlined.Tune
    ),
    AppSettings(
        route = BottomNavRoute.AppSettings,
        labelRes = R.string.app_settings,
        icon = Icons.Outlined.Settings
    )
}
