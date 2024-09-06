package com.w2sv.filenavigator.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.w2sv.filenavigator.MainActivity
import org.junit.Rule
import org.junit.Test

class PermissionScreenTest {

    @get:Rule
    val composeContentTestRule: ComposeContentTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testNavigationDrawerScreen() {
        with(composeContentTestRule) {
            waitForIdle()

            onNodeWithText("Missing Permissions")
                .assertIsDisplayed()
        }
    }
}