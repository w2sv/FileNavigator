package com.w2sv.filenavigator.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.w2sv.filenavigator.ui.theme.Epsilon

sealed interface AnimatedRowElement {
    sealed interface ContentHolder: AnimatedRowElement {
        val content: @Composable () -> Unit
    }

    data class Static(override val content: @Composable () -> Unit) : ContentHolder
    data class Conditional(override val content: @Composable () -> Unit) : ContentHolder
    data object CounterWeight: AnimatedRowElement
}

@Composable
fun RowScope.AnimatedElements(
    elementWeight: Float,
    showConditionalElement: Boolean,
    elements: List<AnimatedRowElement>
) {
    val conditionalElementWeight by animateFloatAsState(
        targetValue = if (showConditionalElement) elementWeight else Epsilon,
        label = ""
    )
    val counterElementWeight by remember {
        derivedStateOf { elementWeight - conditionalElementWeight + Epsilon }
    }

    elements.forEach { element ->
        when (element) {
            is AnimatedRowElement.CounterWeight -> Spacer(modifier = Modifier.weight(counterElementWeight))
            is AnimatedRowElement.ContentHolder -> Box(
                modifier = Modifier.weight(if (element is AnimatedRowElement.Static) elementWeight else conditionalElementWeight),
                contentAlignment = Alignment.Center
            ) {
                element.content()
            }
        }
    }
}