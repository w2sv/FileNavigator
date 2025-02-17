package com.w2sv.navigator.shared

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import com.w2sv.core.navigator.R
import com.w2sv.core.navigator.databinding.DialogHeaderBinding

internal fun AlertDialog.Builder.setIconHeader(@DrawableRes iconRes: Int): AlertDialog.Builder =
    apply {
        setCustomTitle(
            DialogHeaderBinding
                .inflate(LayoutInflater.from(context))
                .apply { icon.setImageResource(iconRes) }
                .root
        )
    }

internal fun roundedCornersAlertDialogBuilder(context: Context): AlertDialog.Builder =
    AlertDialog
        .Builder(context, R.style.RoundedCornersAlertDialog)
