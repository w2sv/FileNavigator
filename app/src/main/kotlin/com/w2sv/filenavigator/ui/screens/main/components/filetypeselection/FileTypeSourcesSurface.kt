package com.w2sv.filenavigator.ui.screens.main.components.filetypeselection

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.data.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AnimatedElements
import com.w2sv.filenavigator.ui.components.AnimatedRowElement
import com.w2sv.filenavigator.ui.components.AppCheckbox
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.components.SnackbarKind
import com.w2sv.filenavigator.ui.components.showSnackbarAndDismissCurrent
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.states.FileTypesState
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.DefaultIconDp
import com.w2sv.filenavigator.ui.utils.InBetweenSpaced
import com.w2sv.filenavigator.ui.utils.extensions.allFalseAfterEnteringValue
import com.w2sv.filenavigator.ui.utils.extensions.orDisabledIf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FileTypeSourcesSurface(
    fileType: FileType,
    fileTypesState: FileTypesState,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            InBetweenSpaced(
                elements = fileType.sources,
                makeElement = {
                    SourceColumn(
                        source = it,
                        fileTypesState = fileTypesState
                    )
                }
            )
        }
    }
}

@Composable
private fun SourceColumn(
    source: FileType.Source,
    fileTypesState: FileTypesState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val isEnabled =
            fileTypesState.mediaFileSourceEnabledMap.getOrDefault(
                key = source.isEnabled,
                defaultValue = true
            )
        val defaultDestinationPath by fileTypesState.defaultMoveDestinationState.pathMap.getValue(
            source.defaultDestination
        )
            .collectAsState()

        SourceRow(
            source = source,
            isEnabled = isEnabled,
            fileTypesState = fileTypesState,
            modifier = Modifier.height(44.dp)
        )

        AnimatedVisibility(visible = isEnabled && defaultDestinationPath != null) {
            DefaultMoveDestinationRow(
                path = remember(this) {  // Remedies NullPointerException
                    defaultDestinationPath!!
                },
                onDeleteButtonClick = {
                    fileTypesState.defaultMoveDestinationState.saveDestination(source, null)
                },
                modifier = Modifier
                    .height(36.dp)
                    .padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun SourceRow(
    source: FileType.Source,
    isEnabled: Boolean,
    fileTypesState: FileTypesState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        // Icon
        Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = source.kind.iconRes),
                contentDescription = null,
                tint = source.fileType.color.copy(alpha = 0.75f)
                    .orDisabledIf(condition = !isEnabled)
            )
        }
        // Label
        Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.CenterStart) {
            AppFontText(
                text = stringResource(id = source.kind.labelRes),
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    .orDisabledIf(condition = !isEnabled)
            )
        }

        AnimatedElements(
            elementWeight = 0.1f,
            showConditionalElement = isEnabled,
            elements = listOf(
                AnimatedRowElement.CounterWeight,
                AnimatedRowElement.Static {
                    if (source.fileType.isMediaType) {
                        AppCheckbox(
                            checked = isEnabled,
                            onCheckedChange = { checkedNew ->
                                if (!source.fileType.sources.map {
                                        fileTypesState.mediaFileSourceEnabledMap.getValue(
                                            it.isEnabled
                                        )
                                    }
                                        .allFalseAfterEnteringValue(checkedNew)
                                ) {
                                    fileTypesState.mediaFileSourceEnabledMap[source.isEnabled] =
                                        checkedNew
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbarAndDismissCurrent(
                                            AppSnackbarVisuals(
                                                message = context.getString(R.string.leave_at_least_one_file_source_selected_or_disable_the_entire_file_type),
                                                kind = SnackbarKind.Error
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                },
                AnimatedRowElement.Conditional {
                    SetDefaultMoveDestinationButton(
                        onClick = {
                            fileTypesState.defaultMoveDestinationState.launchPickerFor(source)
                        }
                    )
                }
            )
        )
    }
}

@Composable
private fun SetDefaultMoveDestinationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(
                id = com.w2sv.navigator.R.drawable.ic_file_move_24
            ),
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = stringResource(
                R.string.open_target_directory_settings
            ),
            modifier = Modifier.size(DefaultIconDp)
        )
    }
}

@Composable
private fun DefaultMoveDestinationRow(
    path: String,
    onDeleteButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.fillMaxWidth(0.22f))
        AppFontText(
            text = path,
            color = AppColor.disabled,
            fontSize = 14.sp,
            modifier = Modifier.weight(0.7f)
        )
        IconButton(
            onClick = onDeleteButtonClick,
            modifier = Modifier.weight(0.1f)
        ) {
            Icon(
                painter = painterResource(id = com.w2sv.navigator.R.drawable.ic_delete_24),
                contentDescription = stringResource(R.string.delete_default_move_destination),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}