package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.states.NavigatorConfiguration
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import slimber.log.i

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTypeSelectionColumn(
    modifier: Modifier = Modifier,
    navigatorConfiguration: NavigatorConfiguration,
) {
    Column(
        modifier = modifier
    ) {
        AppFontText(
            text = stringResource(id = R.string.file_types),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        val firstDisabledFileType by navigatorConfiguration.firstDisabledFileType.collectAsState()

        LazyColumn(state = rememberLazyListState()) {
            items(
                items = navigatorConfiguration.sortedFileTypes,
                key = { it }
            ) { fileType ->
                i { "Laying out ${fileType.name}" }

                FileTypeAccordion(
                    fileType = fileType,
                    isFirstDisabled = { fileType == firstDisabledFileType },
                    navigatorConfiguration = navigatorConfiguration,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItemPlacement(tween(DefaultAnimationDuration))  // Animate upon reordering
                )
            }
        }
    }
}