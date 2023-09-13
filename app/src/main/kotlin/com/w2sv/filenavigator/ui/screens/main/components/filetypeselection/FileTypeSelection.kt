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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.data.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import kotlinx.coroutines.CoroutineScope
import slimber.log.i

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTypeSelectionColumn(
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
    ) {
        AppFontText(
            text = stringResource(id = R.string.navigated_file_types),
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        val animatedFileTypes = remember {
            mutableSetOf<FileType>()
        }
        var nRunningAnimations = remember {
            0
        }

        LazyColumn(state = rememberLazyListState()) {
            items(mainScreenViewModel.sortedFileTypes, key = { it }) { fileType ->
                i { "Laying out ${fileType.identifier}" }

                FileTypeAccordion(
                    fileType = fileType,
                    isEnabled = mainScreenViewModel.unconfirmedFileTypeStatus.getValue(fileType.status).isEnabled,
                    animate = !animatedFileTypes.contains(fileType),
                    nRunningAnimations = nRunningAnimations,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItemPlacement(
                            tween(durationMillis = DefaultAnimationDuration)
                        )
                )
                if (animatedFileTypes.add(fileType)) {
                    nRunningAnimations += 1
                    scope.launchDelayed(250L) {
                        nRunningAnimations -= 1
                    }
                }
            }
        }
    }
}