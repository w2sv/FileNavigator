package com.w2sv.filenavigator.navigator

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavBackStack
import com.w2sv.filenavigator.ui.navigation.NavigatorImpl
import com.w2sv.filenavigator.ui.navigation.Screen
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test

class NavigatorImplTest {

    private val backStack: NavBackStack = mutableStateListOf(Screen.Home)
    private val navigator = NavigatorImpl(backStack)

    @Test
    fun `toAppSettings adds screen if not already top`() {
        navigator.toAppSettings()
        assertEquals(Screen.AppSettings, backStack.last())
        assertEquals(2, backStack.size)
    }

    @Test
    fun `toAppSettings does not add duplicate if already on top`() {
        backStack.add(Screen.AppSettings)
        navigator.toAppSettings()
        assertEquals(2, backStack.size) // no new item
    }

    @Test
    fun `leaveRequiredPermissions pops and adds Home if backStack is empty`() {
        backStack.clear()
        navigator.leaveRequiredPermissions()
        assertEquals(1, backStack.size)
        assertEquals(Screen.Home, backStack.last())
    }

    @Test
    fun `leaveRequiredPermissions pops only if backStack not empty`() {
        backStack.add(Screen.RequiredPermissions)
        navigator.leaveRequiredPermissions()
        assertFalse(backStack.contains(Screen.RequiredPermissions))
    }

    @Test
    fun `popBackStack removes top item`() {
        backStack.add(Screen.AppSettings)
        navigator.popBackStack()
        assertEquals(Screen.Home, backStack.last())
    }

    @Test
    fun `currentScreen returns top of stack`() {
        backStack.add(Screen.AppSettings)
        assertEquals(Screen.AppSettings, navigator.currentScreen)
    }
}
