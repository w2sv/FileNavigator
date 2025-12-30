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
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.NavigatorConfigActions
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.PreviewNavigatorConfigActions
import com.w2sv.filenavigator.ui.util.PreviewOf

object NavigatorSettingsListDefaults {
    val itemSpacing: Dp = 6.dp
    val sectionHeaderPadding = PaddingValues(vertical = 16.dp)
    val fabBottomPadding: Dp
        @Composable
        get() = if (isPortraitModeActive) 140.dp else 90.dp
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
        contentPadding = PaddingValues(bottom = NavigatorSettingsListDefaults.fabBottomPadding)
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
    padding: PaddingValues = NavigatorSettingsListDefaults.sectionHeaderPadding,
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
                actions = PreviewNavigatorConfigActions,
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
