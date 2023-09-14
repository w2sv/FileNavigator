package com.w2sv.filenavigator.ui.screens.main.components.filetypeselection

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.data.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import com.w2sv.filenavigator.ui.screens.main.components.filetypeselection.defaultmovedestination.DefaultMoveDestinationDialog
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.CascadeAnimationState
import slimber.log.i

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTypeSelectionColumn(
    modifier: Modifier = Modifier,
    mainScreenVM: MainScreenViewModel = viewModel(),
) {
    var defaultMoveDestinationDialogFileSource by rememberSaveable {
        mutableStateOf<FileType.Source?>(null)
    }
        .apply {
            value?.let {
                DefaultMoveDestinationDialog(
                    fileSource = it,
                    configuration = mainScreenVM.navigatorUIState.getDefaultMoveDestinationConfiguration(
                        it
                    ),
                    closeDialog = {
                        value = null
                    }
                )
            }
        }

    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
    ) {
        AppFontText(
            text = stringResource(id = R.string.file_types),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        val cascadeAnimationState = remember {
            CascadeAnimationState<FileType>()
        }

        LazyColumn(state = rememberLazyListState()) {
            items(mainScreenVM.navigatorUIState.sortedFileTypes, key = { it }) { fileType ->
                i { "Laying out ${fileType.identifier}" }

                FileTypeAccordion(
                    fileType = fileType,
                    fileTypeStatusMap = mainScreenVM.navigatorUIState.fileTypeStatusMap,
                    fileSourceEnabledMap = mainScreenVM.navigatorUIState.mediaFileSourceEnabledMap,
                    configureDefaultMoveDestination = {
                        defaultMoveDestinationDialogFileSource = it
                    },
                    cascadeAnimationState = cascadeAnimationState,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItemPlacement(tween(DefaultAnimationDuration))  // Animation upon reordering
                )
            }
        }
    }
}