package com.w2sv.filenavigator.ui.utils

import androidx.compose.ui.Modifier

fun Modifier.conditional(
    condition: Boolean,
    onTrue: Modifier.() -> Modifier,
    onFalse: (Modifier.() -> Modifier)? = null,
): Modifier {
    return if (condition) {
        then(onTrue(Modifier))
    } else {
        onFalse?.invoke(Modifier) ?: this
    }
}