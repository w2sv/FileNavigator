package com.w2sv.domain.model.filetype

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.w2sv.core.common.R

enum class SourceType(@StringRes val labelRes: Int, @DrawableRes val iconRes: Int) {
    Camera(
        R.string.camera,
        R.drawable.ic_camera_24
    ),
    Screenshot(
        R.string.screenshot,
        R.drawable.ic_screenshot_24
    ),
    Recording(
        R.string.recording,
        R.drawable.ic_mic_24
    ),
    Download(
        R.string.download,
        R.drawable.ic_file_download_24
    ),
    OtherApp(
        R.string.other_app,
        R.drawable.ic_apps_24
    )
}
