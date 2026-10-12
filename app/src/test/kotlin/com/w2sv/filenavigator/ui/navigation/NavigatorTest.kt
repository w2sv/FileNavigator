package com.w2sv.filenavigator.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavRoute
import com.w2sv.filenavigator.ui.navigation.main.MainNavRoute
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertSame
import org.junit.Test

class NavigatorTest {

    private lateinit var backStack: NavBackStack<MainNavRoute>
    private lateinit var navigator: Navigator

    private fun initializeBackStack(vararg routes: MainNavRoute) {
        backStack = NavBackStack(*routes)
        navigator = Navigator(backStack)
    }

    private fun currentBottomNavStack(): List<BottomNavRoute> =
        (navigator.currentRoute as MainNavRoute.BottomNavHost).backStack.toList()

    @Test
    fun `toAppSettings adds app settings after home in the nested stack`() {
        initializeBackStack(MainNavRoute.BottomNavHost())

        navigator.toAppSettings()

        assertEquals(listOf(BottomNavRoute.Home, BottomNavRoute.AppSettings), currentBottomNavStack())
    }

    @Test
    fun `bottom navigation keeps the same main host and replaces the selected destination`() {
        val host = MainNavRoute.BottomNavHost(NavBackStack(BottomNavRoute.Home, BottomNavRoute.NavigatorSettings))
        initializeBackStack(host)

        navigator.toAppSettings()

        assertSame(host, backStack.single())
        assertEquals(listOf(BottomNavRoute.Home, BottomNavRoute.AppSettings), currentBottomNavStack())
    }

    @Test
    fun `selecting the current destination leaves its nested stack unchanged`() {
        val host = MainNavRoute.BottomNavHost(NavBackStack(BottomNavRoute.Home, BottomNavRoute.AppSettings))
        initializeBackStack(host)

        navigator.toAppSettings()

        assertSame(host, backStack.single())
        assertEquals(listOf(BottomNavRoute.Home, BottomNavRoute.AppSettings), currentBottomNavStack())
    }

    @Test
    fun `permissions to home replaces the main route with a bottom navigation host`() {
        initializeBackStack(MainNavRoute.Permissions)

        navigator.toHome()

        assertEquals(1, backStack.size)
        assertEquals(listOf(BottomNavRoute.Home), currentBottomNavStack())
    }

    @Test
    fun `bottom navigation to permissions replaces the main route`() {
        initializeBackStack(MainNavRoute.BottomNavHost())

        navigator.toPermissions()

        assertEquals(listOf(MainNavRoute.Permissions), backStack.toList())
    }

    @Test
    fun `toHome from app settings resets the nested stack`() {
        initializeBackStack(MainNavRoute.BottomNavHost(NavBackStack(BottomNavRoute.Home, BottomNavRoute.AppSettings)))

        navigator.toHome()

        assertEquals(listOf(BottomNavRoute.Home), currentBottomNavStack())
    }

    @Test
    fun `popBackStack from app settings returns to home without removing the main host`() {
        val host = MainNavRoute.BottomNavHost(NavBackStack(BottomNavRoute.Home, BottomNavRoute.AppSettings))
        initializeBackStack(host)

        navigator.popBackStack()

        assertSame(host, backStack.single())
        assertEquals(listOf(BottomNavRoute.Home), currentBottomNavStack())
    }

    @Test
    fun `popBackStack from home keeps the main host`() {
        initializeBackStack(MainNavRoute.BottomNavHost())

        navigator.popBackStack()

        assertEquals(1, backStack.size)
        assertEquals(BottomNavRoute.Home, navigator.currentBottomNavRoute)
    }
}
