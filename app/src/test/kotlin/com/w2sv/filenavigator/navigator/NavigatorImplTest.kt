package com.w2sv.filenavigator.navigator

import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import com.w2sv.filenavigator.ui.navigation.NavigatorImpl
import com.w2sv.filenavigator.ui.navigation.Screen
import junit.framework.TestCase.assertEquals
import org.junit.Test

class NavigatorImplTest {

    private lateinit var backStack: NavBackStack
    private val navigator by lazy { NavigatorImpl(backStack) }

    private fun initializeBackStack(vararg screen: Screen) {
        backStack = screen.toList().toMutableStateList()
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
    fun `leaveRequiredPermissions pops and adds Home screen`() {
        initializeBackStack(Screen.RequiredPermissions)
        navigator.leaveRequiredPermissions()
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
