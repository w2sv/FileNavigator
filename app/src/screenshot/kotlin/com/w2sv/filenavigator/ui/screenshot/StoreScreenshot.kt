package com.w2sv.filenavigator.ui.screenshot

import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavRoute

enum class StoreScreenshot(val route: BottomNavRoute) {
    HOME(BottomNavRoute.Home),
    NAVIGATOR_SETTINGS(BottomNavRoute.NavigatorSettings),
    APP_SETTINGS(BottomNavRoute.AppSettings)
}
