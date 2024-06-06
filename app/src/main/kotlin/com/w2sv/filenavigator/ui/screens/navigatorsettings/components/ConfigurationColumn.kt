package com.w2sv.filenavigator.ui.screens.navigatorsettings.components

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.common.utils.getDocumentUriPath
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.designsystem.emptyWindowInsets
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection.FileTypeAccordion
import com.w2sv.filenavigator.ui.states.ReversibleNavigatorConfig
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import slimber.log.i

private val verticalPadding = 16.dp

@Immutable
@Parcelize
data class SourceAutoMoveBottomSheetParameters(
    val fileType: FileType,
    val sourceType: SourceType,
    val sourceAutoMoveConfig: AutoMoveConfig
) : Parcelable

@Composable
fun SubDirectoryIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_subdirectory_arrow_right_24),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun rememberAutoMoveDestinationPath(
    destination: Uri?,
    context: Context = LocalContext.current
): State<String?> =
    remember(destination) {
        mutableStateOf(
            destination?.let { getDocumentUriPath(it, context) }
        )
    }

@Composable
fun AutoMoveRow(
    destinationPath: String,
    changeDestination: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    CompositionLocalProvider(value = LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .padding(start = 10.dp, bottom = 4.dp)
        ) {
            SubDirectoryIcon(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(20.dp)
            )
            Text(destinationPath, modifier = Modifier.weight(1f), fontSize = 14.sp)
            IconButton(
                onClick = { changeDestination() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_folder_edit_24),
                    contentDescription = stringResource(R.string.select_the_auto_move_destination),
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            trailing?.invoke(this)
        }
    }
}

@Composable
fun rememberSelectAutoMoveDestination(onDestinationSelected: (Uri) -> Unit): ManagedActivityResultLauncher<Uri?, Uri?> {
    val context: Context = LocalContext.current
    return rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { optionalTreeUri ->
        optionalTreeUri?.let {
            onDestinationSelected(
                DocumentFile.fromTreeUri(context, it)!!.uri
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigatorConfigurationColumn(
    reversibleConfig: ReversibleNavigatorConfig,
    showAddFileTypesBottomSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config by reversibleConfig.collectAsStateWithLifecycle()
    var sourceAutoMoveBottomSheetParameters by rememberSaveable {
        mutableStateOf<SourceAutoMoveBottomSheetParameters?>(null)
    }

    LazyColumn(
        modifier = modifier
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionHeader(
                    text = stringResource(id = R.string.file_types),
                )
                AnimatedVisibility(visible = config.disabledFileTypes.isNotEmpty()) {
                    FilledTonalIconButton(onClick = showAddFileTypesBottomSheet) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_a_file_type)
                        )
                    }
                }
            }
        }
        items(config.enabledFileTypes, key = { it }) { fileType ->
            i { "Laying out ${fileType.logIdentifier}" }

            FileTypeAccordion(
                fileType = fileType,
                excludeFileType = remember(fileType) {
                    {
                        reversibleConfig.onFileTypeCheckedChange(
                            fileType = fileType,
                            checkedNew = false
                        )
                    }
                },
                autoMoveConfig = config.fileTypeConfig(fileType).autoMoveConfig,
                setAutoMoveConfig = { autoMoveConfig ->
                    reversibleConfig.update {
                        it.copyWithAlteredFileAutoMoveConfig(
                            fileType = fileType,
                            autoMoveConfig = autoMoveConfig
                        )
                    }
                },
                sourceTypeConfigMap = config.fileTypeConfig(fileType).sourceTypeConfigMap.toImmutableMap(),
                onSourceCheckedChange = remember(fileType) {
                    { source, checked ->
                        reversibleConfig.onFileSourceCheckedChange(
                            fileType = fileType,
                            sourceType = source,
                            checkedNew = checked
                        )
                    }
                },
                deleteSourceAutoMoveDestination = { sourceType ->
                    reversibleConfig.update {
                        it.copyWithAlteredSourceAutoMoveConfig(
                            fileType,
                            sourceType
                        ) { autoMoveConfig ->
                            autoMoveConfig.copy(
                                enabled = it.fileTypeConfig(fileType).autoMoveConfig.enabled,
                                destination = null
                            )
                        }
                    }
                },
                setSourceAutoMoveBottomSheetParameters = {
                    sourceAutoMoveBottomSheetParameters = it
                },
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .animateItem()
            )
        }
        item {
            SectionHeader(
                text = stringResource(id = R.string.more),
            )
            MoreColumnItems(
                disableOnLowBattery = config.disableOnLowBattery,
                setDisableOnLowBattery = { checked ->
                    reversibleConfig.update { it.copy(disableOnLowBattery = checked) }
                },
                modifier = Modifier.padding(bottom = if (isPortraitModeActive) 132.dp else 92.dp)
            )
        }
    }

    sourceAutoMoveBottomSheetParameters?.let { params ->
        SourceAutoMoveConfigBottomSheet(
            config = params.sourceAutoMoveConfig,
            setConfig = { autoMoveConfig ->
                reversibleConfig.update { config ->
                    config.copyWithAlteredSourceConfig(
                        fileType = params.fileType,
                        sourceType = params.sourceType,
                    ) {
                        it.copy(autoMoveConfig = autoMoveConfig)
                    }
                }
                sourceAutoMoveBottomSheetParameters =
                    params.copy(sourceAutoMoveConfig = autoMoveConfig)
            },
            onDismissRequest = { sourceAutoMoveBottomSheetParameters = null })
    }
}

private val iconModifier = Modifier.padding(end = 8.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SourceAutoMoveConfigBottomSheet(
    config: AutoMoveConfig,
    setConfig: (AutoMoveConfig) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
) {
    val selectAutoMoveDestination = rememberSelectAutoMoveDestination {
        setConfig(config.copy(destination = it))
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        windowInsets = emptyWindowInsets,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 32.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Auto Move")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { setConfig(config.copy(enabled = !config.enabled)) }
                )
            }
            AnimatedVisibility(
                visible = config.enabled,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectAutoMoveDestination.launch(config.destination) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubDirectoryIcon(modifier = iconModifier)
                    Text(text = "Set specific destination")
                }
            }
        }
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBarsIgnoringVisibility))
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = defaultSectionHeaderModifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
    )
}

private val defaultSectionHeaderModifier = Modifier.padding(vertical = verticalPadding)

@Composable
private fun MoreColumnItems(
    disableOnLowBattery: Boolean,
    setDisableOnLowBattery: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SwitchItemRow(
            iconRes = R.drawable.ic_battery_low_24,
            textRes = R.string.disable_on_low_battery,
            checked = disableOnLowBattery,
            onCheckedChange = setDisableOnLowBattery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun SwitchItemRow(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = stringResource(id = textRes))
        RightAligned {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}