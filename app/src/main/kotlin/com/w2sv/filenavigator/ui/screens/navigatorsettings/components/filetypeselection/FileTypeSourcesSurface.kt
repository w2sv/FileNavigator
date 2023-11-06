package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.data.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppCheckbox
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.components.SnackbarKind
import com.w2sv.filenavigator.ui.components.showSnackbarAndDismissCurrent
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.states.FileTypesState
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
                    SourceRow(
                        source = it,
                        isEnabled = fileTypesState.mediaFileSourceEnabledMap.getOrDefault(
                            key = it.isEnabledDSE,
                            defaultValue = true
                        ),
                        fileTypesState = fileTypesState,
                        modifier = Modifier.height(44.dp)
                    )
                }
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
                tint = source.fileType.color
                    .orDisabledIf(condition = !isEnabled)
            )
        }
        // Label
        Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.CenterStart) {
            AppFontText(
                text = stringResource(id = source.kind.labelRes),
                color = MaterialTheme.colorScheme.onSurface
                    .orDisabledIf(condition = !isEnabled)
            )
        }

        Box(modifier = Modifier.weight(0.15f), contentAlignment = Alignment.Center) {
            if (source.fileType.isMediaType) {
                AppCheckbox(
                    checked = isEnabled,
                    onCheckedChange = { checkedNew ->
                        if (!source.fileType.sources.map {
                                fileTypesState.mediaFileSourceEnabledMap.getValue(
                                    it.isEnabledDSE
                                )
                            }
                                .allFalseAfterEnteringValue(checkedNew)
                        ) {
                            fileTypesState.mediaFileSourceEnabledMap[source.isEnabledDSE] =
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
        }
    }
}