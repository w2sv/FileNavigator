package com.w2sv.filenavigator.ui.screenshot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.w2sv.filenavigator.ui.screenshot.util.enableTransparentEdgeToEdge

class InAppScreenshotActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableTransparentEdgeToEdge()

        val screenshot = StoreScreenshot.valueOf(
            intent.getStringExtra(EXTRA_SCREEN) ?: StoreScreenshot.HOME.name
        )
        val fixture = StoreScreenshotFixture(this)

        setContent {
            StoreScreenshotTheme(fixture) {
                StoreScreenshotContent(
                    screenshot = screenshot,
                    fixture = fixture
                )
            }
        }
    }

    companion object {
        const val EXTRA_SCREEN = "storeScreenshotScreen"
    }
}
