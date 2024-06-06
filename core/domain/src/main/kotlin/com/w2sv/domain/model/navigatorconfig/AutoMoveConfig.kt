package com.w2sv.domain.model.navigatorconfig

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AutoMoveConfig(val enabled: Boolean = false, val destination: Uri? = null) : Parcelable