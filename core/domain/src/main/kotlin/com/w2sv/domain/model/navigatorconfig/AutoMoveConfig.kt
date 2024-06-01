package com.w2sv.domain.model.navigatorconfig

import android.net.Uri

data class AutoMoveConfig(val enabled: Boolean = false, val destination: Uri? = null)