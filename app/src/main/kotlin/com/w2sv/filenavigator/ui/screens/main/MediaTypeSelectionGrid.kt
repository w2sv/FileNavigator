package com.w2sv.filenavigator.ui.screens.main

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.mediastore.FileType
import com.w2sv.filenavigator.ui.ExtendedSnackbarVisuals
import com.w2sv.filenavigator.ui.SnackbarKind
import com.w2sv.filenavigator.ui.animateGridItemSpawn
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
import com.w2sv.filenavigator.ui.theme.RailwayText
import com.w2sv.filenavigator.utils.goToManageExternalStorageSettings
import com.w2sv.filenavigator.utils.toggle
import kotlinx.coroutines.launch

@Preview
@Composable
private fun MediaTypeSelectionGridPrev() {
    FileNavigatorTheme {
        MediaTypeSelectionGrid()
    }
}

@Composable
internal fun MediaTypeSelectionGrid(
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState()

    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            val nColumns = 2

            LazyVerticalGrid(
                columns = GridCells.Fixed(nColumns),
                modifier = modifier
            ) {
                content(nColumns = nColumns, state = state, makeCardsVerticallyScrollable = false)
            }
        }

        else -> {
            val nColumns = 1

            LazyHorizontalGrid(
                rows = GridCells.Fixed(nColumns),
                modifier = modifier
            ) {
                content(
                    nColumns = nColumns,
                    state = state,
                    cardModifier = Modifier
                        .width(180.dp),
                    makeCardsVerticallyScrollable = true
                )
            }
        }
    }
}

private fun LazyGridScope.content(
    nColumns: Int,
    state: LazyListState,
    makeCardsVerticallyScrollable: Boolean,
    cardModifier: Modifier = Modifier
) {
    items(FileType.all.size) {
        MediaTypeCard(
            fileType = FileType.all[it],
            modifier = cardModifier
                .padding(8.dp)
                .animateGridItemSpawn(it, nColumns, state),
            columnModifier = if (makeCardsVerticallyScrollable) {
                Modifier.verticalScroll(rememberScrollState())
            } else
                Modifier
        )
    }
}

enum class CardState {
    Enabled,
    FileManagerPermissionMissing,
    MediaTypeDisabled,
    AllOriginsDisabled
}

@Composable
private fun MediaTypeCard(
    fileType: FileType,
    modifier: Modifier = Modifier,
    columnModifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val cardState: CardState = when {
        fileType is FileType.NonMedia && !mainScreenViewModel.manageExternalStoragePermissionGranted.collectAsState().value -> CardState.FileManagerPermissionMissing
        !mainScreenViewModel.accountForFileType.getValue(fileType) -> CardState.MediaTypeDisabled
        fileType.origins.none { mainScreenViewModel.accountForFileTypeOrigin.getValue(it) } -> CardState.AllOriginsDisabled
        else -> CardState.Enabled
    }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = columnModifier
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSection(
                fileType = fileType,
                cardState = cardState
            )
            Divider()
            OriginsSection(
                fileType = fileType,
                cardState = cardState
            )
        }
    }
}

@Composable
fun disabledColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

@Composable
fun checkMarkColorOnCard(): Color =
    MaterialTheme.colorScheme.surface

@Composable
private fun HeaderSection(
    fileType: FileType,
    cardState: CardState,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mainColor = if (cardState == CardState.Enabled)
        MaterialTheme.colorScheme.tertiary
    else
        disabledColor()

    Column(modifier = modifier) {
        Icon(
            painter = painterResource(id = fileType.iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.CenterHorizontally),
            tint = mainColor
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.weight(0.2f))
            Box(Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
                RailwayText(
                    text = stringResource(id = fileType.titleRes),
                    fontSize = 18.sp,
                    color = mainColor
                )
            }
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.CenterEnd) {
                Checkbox(
                    checked = cardState == CardState.Enabled,
                    enabled = cardState != CardState.AllOriginsDisabled,
                    onCheckedChange = {
                        when (cardState) {
                            CardState.FileManagerPermissionMissing -> scope.launch {
                                with(mainScreenViewModel.snackbarHostState) {
                                    currentSnackbarData?.dismiss()
                                    showSnackbar(
                                        ExtendedSnackbarVisuals(
                                            message = context.getString(
                                                R.string.snackbar_message,
                                                context.getString(fileType.titleRes)
                                            ),
                                            kind = SnackbarKind.Error,
                                            action = {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                    goToManageExternalStorageSettings(context)
                                                }
                                            },
                                            actionLabel = context.getString(R.string.grant)
                                        )
                                    )
                                }
                            }

                            else -> mainScreenViewModel.accountForFileType.toggle(fileType)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = mainColor,
                        checkmarkColor = checkMarkColorOnCard()
                    )
                )
            }
        }
    }
}

@Composable
private fun OriginsSection(
    fileType: FileType,
    cardState: CardState,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    Column(modifier = modifier) {
        fileType.origins.forEach { origin ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.CenterStart) {
                    RailwayText(
                        text = stringResource(id = origin.kind.labelRes),
                        fontSize = 13.sp,
                        color = if (cardState == CardState.Enabled) Color.Unspecified else disabledColor()
                    )
                }
                Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.CenterEnd) {
                    Checkbox(
                        checked = mainScreenViewModel.accountForFileTypeOrigin.getValue(
                            origin
                        ),
                        onCheckedChange = {
                            mainScreenViewModel.accountForFileTypeOrigin.toggle(origin)
                        },
                        enabled = setOf(CardState.Enabled, CardState.AllOriginsDisabled).contains(
                            cardState
                        ),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.secondary,
                            checkmarkColor = checkMarkColorOnCard()
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MediaTypeCardPreview() {
    FileNavigatorTheme {
        MediaTypeCard(
            fileType = FileType.Image,
            modifier = Modifier.size(160.dp)
        )
    }
}