package com.w2sv.filenavigator.ui.navigation.main

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.w2sv.composed.navigation3.NestedNavHost
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavRoute
import kotlinx.serialization.Serializable

@Serializable
sealed interface MainNavRoute : NavKey {

    @Serializable
    data object Permissions : MainNavRoute

    @Serializable
    data class BottomNavHost(override val backStack: NavBackStack<BottomNavRoute> = NavBackStack(BottomNavRoute.Home)) :
        MainNavRoute,
        NestedNavHost<BottomNavRoute>
}
