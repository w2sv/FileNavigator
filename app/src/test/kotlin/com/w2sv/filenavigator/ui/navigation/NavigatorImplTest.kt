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
    fun `toAppSettings adds screen if not already top`() {
        initializeBackStack(Screen.Home)
        navigator.toAppSettings()
        assertEquals(Screen.AppSettings, backStack.last())
        assertEquals(2, backStack.size)
    }

    @Test
    fun `toAppSettings does not add duplicate if already on top`() {
        initializeBackStack(Screen.Home, Screen.AppSettings)
        navigator.toAppSettings()
        assertEquals(2, backStack.size) // no new item
    }

    @Test
    fun `toHome pops and adds Home screen`() {
        initializeBackStack(Screen.Permissions)
        navigator.toHome()
        assertBackStackEquals(listOf(Screen.Home))
    }

    @Test
    fun `popBackStack removes top item`() {
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
