package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SingleChoiceSegmentedButtonRowScope.TweakedSegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SegmentedButtonColors = SegmentedButtonDefaults.colors(),
    border: BorderStroke = SegmentedButtonDefaults.borderStroke(
        colors.borderColor(enabled, selected)
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    icon: @Composable () -> Unit = { SegmentedButtonDefaults.Icon(selected) },
    label: @Composable () -> Unit
) {
    val containerColor = colors.containerColor(enabled, selected)
    val contentColor = colors.contentColor(enabled, selected)
    val interactionCount = interactionSource.interactionCountAsState()

    Surface(
        modifier = modifier
            .weight(1f)
            .interactionZIndex(selected, interactionCount)
            .defaultMinSize(
                minWidth = ButtonDefaults.MinWidth,
                minHeight = ButtonDefaults.MinHeight
            )
            .semantics { role = Role.RadioButton },
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        interactionSource = interactionSource
    ) {
        SegmentedButtonContent(icon, label)
    }
}

private fun Modifier.interactionZIndex(checked: Boolean, interactionCount: State<Int>) =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            val zIndex = interactionCount.value + if (checked) CheckedZIndexFactor else 0f
            placeable.place(0, 0, zIndex)
        }
    }

@Suppress("ktlint:standard:property-naming")
private const val CheckedZIndexFactor = 5f
private val IconSpacing = 8.dp

/**
 * Represents the color used for the SegmentedButton's border,
 * depending on [enabled] and [active].
 *
 * @param enabled whether the [SegmentedButton] is enabled or not
 * @param active whether the [SegmentedButton] item is checked or not
 */
@Stable
internal fun SegmentedButtonColors.borderColor(enabled: Boolean, active: Boolean): Color {
    return when {
        enabled && active -> activeBorderColor
        enabled && !active -> inactiveBorderColor
        !enabled && active -> disabledActiveBorderColor
        else -> disabledInactiveBorderColor
    }
}

/**
 * Represents the content color passed to the items
 *
 * @param enabled whether the [SegmentedButton] is enabled or not
 * @param checked whether the [SegmentedButton] item is checked or not
 */
@Stable
internal fun SegmentedButtonColors.contentColor(enabled: Boolean, checked: Boolean): Color {
    return when {
        enabled && checked -> activeContentColor
        enabled && !checked -> inactiveContentColor
        !enabled && checked -> disabledActiveContentColor
        else -> disabledInactiveContentColor
    }
}

/**
 * Represents the container color passed to the items
 *
 * @param enabled whether the [SegmentedButton] is enabled or not
 * @param active whether the [SegmentedButton] item is active or not
 */
@Stable
internal fun SegmentedButtonColors.containerColor(enabled: Boolean, active: Boolean): Color {
    return when {
        enabled && active -> activeContainerColor
        enabled && !active -> inactiveContainerColor
        !enabled && active -> disabledActiveContainerColor
        else -> disabledInactiveContainerColor
    }
}

@Composable
private fun SegmentedButtonContent(icon: @Composable () -> Unit, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center
//        modifier = Modifier.padding(ButtonDefaults.TextButtonContentPadding)
    ) {
        val typography = MaterialTheme.typography.labelLarge
        ProvideTextStyle(typography) {
            val scope = rememberCoroutineScope()
            val measurePolicy = remember { SegmentedButtonContentMeasurePolicy(scope) }

            Layout(
                modifier = Modifier.height(IntrinsicSize.Min),
                contents = listOf(icon, content),
                measurePolicy = measurePolicy
            )
        }
    }
}

internal class SegmentedButtonContentMeasurePolicy(
    val scope: CoroutineScope
) : MultiContentMeasurePolicy {
    var animatable: Animatable<Int, AnimationVector1D>? = null
    private var initialOffset: Int? = null

    override fun MeasureScope.measure(measurables: List<List<Measurable>>, constraints: Constraints): MeasureResult {
        val (iconMeasurables, contentMeasurables) = measurables
        val iconPlaceables = iconMeasurables.fastMap { it.measure(constraints) }
        val iconWidth = iconPlaceables.fastMaxBy { it.width }?.width ?: 0
        val contentPlaceables = contentMeasurables.fastMap { it.measure(constraints) }
        val contentWidth = contentPlaceables.fastMaxBy { it.width }?.width
        val height = contentPlaceables.fastMaxBy { it.height }?.height ?: 0
        val width = maxOf(SegmentedButtonDefaults.IconSize.roundToPx(), iconWidth) +
            IconSpacing.roundToPx() +
            (contentWidth ?: 0)
        val offsetX = if (iconWidth == 0) {
            -(SegmentedButtonDefaults.IconSize.roundToPx() + IconSpacing.roundToPx()) / 2
        } else {
            0
        }

        if (initialOffset == null) {
            initialOffset = offsetX
        } else {
            val anim = animatable ?: Animatable(initialOffset!!, Int.VectorConverter)
                .also { animatable = it }
            if (anim.targetValue != offsetX) {
                scope.launch {
                    anim.animateTo(offsetX, tween(MotionTokens.DurationMedium3.toInt()))
                }
            }
        }

        return layout(width, height) {
            iconPlaceables.fastForEach {
                it.place(0, (height - it.height) / 2)
            }

            val contentOffsetX = SegmentedButtonDefaults.IconSize.roundToPx() +
                IconSpacing.roundToPx() + (animatable?.value ?: offsetX)

            contentPlaceables.fastForEach {
                it.place(
                    contentOffsetX,
                    (height - it.height) / 2
                )
            }
        }
    }
}

private object MotionTokens {
    @Suppress("ktlint:standard:property-naming")
    const val DurationMedium3 = 350.0
}

@Composable
private fun InteractionSource.interactionCountAsState(): State<Int> {
    val interactionCount = remember { mutableIntStateOf(0) }
    LaunchedEffect(this) {
        this@interactionCountAsState.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press,
                is FocusInteraction.Focus -> {
                    interactionCount.intValue++
                }

                is PressInteraction.Release,
                is FocusInteraction.Unfocus,
                is PressInteraction.Cancel -> {
                    interactionCount.intValue--
                }
            }
        }
    }

    return interactionCount
}
