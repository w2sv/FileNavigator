package com.w2sv.filenavigator.ui.screens

import androidx.annotation.StringRes
import com.w2sv.filenavigator.R

enum class Screen(@StringRes val titleRes: Int) {
    Home(R.string.home),
    NavigatorSettings(R.string.navigator_settings),
    MissingPermissions(R.string.missing_permissions)
}