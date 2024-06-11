package com.w2sv.domain.model.navigatorconfig

import android.os.Parcelable
import com.w2sv.common.utils.DocumentUri
import kotlinx.parcelize.Parcelize

@Parcelize
data class AutoMoveConfig(
    val enabled: Boolean = false,
    val destination: DocumentUri? = null
) :
    Parcelable