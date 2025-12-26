package com.w2sv.filenavigator.ui.sharedstate

import androidx.compose.runtime.Stable
import com.w2sv.domain.model.Theme

@Stable
data class ThemeSettings(val theme: Theme, val useAmoledBlackTheme: Boolean, val useDynamicColors: Boolean)
