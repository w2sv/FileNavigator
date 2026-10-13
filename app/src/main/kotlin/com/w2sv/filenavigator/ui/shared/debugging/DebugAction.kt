package com.w2sv.filenavigator.ui.shared.debugging

/**
 * Debug-only intent actions.
 * Must never be handled in release builds.
 */
enum class DebugAction(val intentAction: String) {
    LaunchNavigatorSettings("com.w2sv.filenavigator.LAUNCH_NAVIGATOR_SETTINGS_SCREEN"),
    StartNavigator("com.w2sv.filenavigator.START_NAVIGATOR");

    companion object {
        fun fromIntentAction(intentAction: String?): DebugAction? =
            entries.firstOrNull { it.intentAction == intentAction }
    }
}
