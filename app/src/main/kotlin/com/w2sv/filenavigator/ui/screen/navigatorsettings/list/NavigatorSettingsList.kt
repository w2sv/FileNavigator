package com.w2sv.filenavigator.ui.screen.navigatorsettings.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.util.PreviewOf

object NavigatorSettingsListDefaults {
    val itemSpacing: Dp = 6.dp
    val sectionHeaderPaddingVertical: Dp = 12.dp
}

@Composable
fun NavigatorSettingsList(
    config: NavigatorConfig,
    actions: NavigatorConfigActions,
    showFileTypesBottomSheet: () -> Unit,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        verticalArrangement = Arrangement.spacedBy(NavigatorSettingsListDefaults.itemSpacing),
        contentPadding = PaddingValues(top = NavigatorSettingsListDefaults.itemSpacing, bottom = Padding.fabButtonBottomPadding)
    ) {
        fileTypeSettingsList(
            config = config,
            fileTypeConfigActions = actions,
            showFileTypesBottomSheet = showFileTypesBottomSheet,
            showFileTypeConfigurationDialog = showFileTypeConfigurationDialog
        )
        moreItemList(config, actions)
    }
}

@Composable
fun NavigatorSettingsListSectionHeader(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    padding: PaddingValues = PaddingValues(vertical = NavigatorSettingsListDefaults.sectionHeaderPaddingVertical),
    endContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier.padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium
        )
        endContent()
    }
}

@Preview
@Composable
private fun PrevTop() {
    Prev(0)
}

@Preview
@Composable
private fun PrevBottom() {
    Prev(6)
}

@Composable
private fun Prev(firstVisibleItemIndex: Int) {
    PreviewOf {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavigatorSettingsList(
                config = NavigatorConfig.default,
                actions = object : NavigatorConfigActions {
                    override fun toggleSource(
                        fileType: FileType,
                        sourceType: SourceType,
                        enabled: Boolean
                    ) =
                        Unit

                    override fun setAutoMoveConfig(
                        fileType: FileType,
                        sourceType: SourceType,
                        config: AutoMoveConfig
                    ) =
                        Unit

                    override fun setAutoMoveConfigs(fileType: FileType, config: AutoMoveConfig) =
                        Unit

                    override fun update(function: (NavigatorConfig) -> NavigatorConfig) =
                        Unit
                },
                showFileTypesBottomSheet = {},
                showFileTypeConfigurationDialog = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                state = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleItemIndex)
            )
        }
    }
}
