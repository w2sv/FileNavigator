package com.w2sv.filenavigator.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import junit.framework.TestCase.assertEquals
import org.junit.Test

class NavigatorImplTest {

    private lateinit var backStack: NavBackStack<Screen>
    private lateinit var navigator: Navigator

    private fun initializeBackStack(vararg screen: Screen) {
        backStack = NavBackStack(*screen)
        navigator = NavigatorImpl(backStack)
    }

    private fun assertBackStackEquals(expected: List<Screen>) {
        assertEquals(expected, backStack.toList())
    }

    @Test
    fun `toAppSettings sets home followed by app settings`() {
        initializeBackStack(Screen.Home)
        navigator.toAppSettings()
        assertBackStackEquals(listOf(Screen.Home, Screen.AppSettings))
    }

    @Test
    fun `toAppSettings does not add duplicate if already on top`() {
        initializeBackStack(Screen.Home, Screen.AppSettings)
        navigator.toAppSettings()
        assertBackStackEquals(listOf(Screen.Home, Screen.AppSettings))
    }

    @Test
    fun `toAppSettings replaces another primary destination`() {
        initializeBackStack(Screen.Home, Screen.NavigatorSettings)
        navigator.toAppSettings()
        assertBackStackEquals(listOf(Screen.Home, Screen.AppSettings))
    }

    @Test
    fun `toHome clears to home`() {
        initializeBackStack(Screen.Permissions)
        navigator.toHome()
        assertBackStackEquals(listOf(Screen.Home))
    }

    @Test
    fun `popBackStack from bottom navigation destination returns home`() {
        initializeBackStack(Screen.Home, Screen.AppSettings)
        navigator.popBackStack()
        assertEquals(Screen.Home, backStack.last())
    }

    @Test
    fun `currentScreen returns top of stack`() {
        initializeBackStack(Screen.Home, Screen.AppSettings)
        assertEquals(Screen.AppSettings, navigator.currentScreen)
    }
}
