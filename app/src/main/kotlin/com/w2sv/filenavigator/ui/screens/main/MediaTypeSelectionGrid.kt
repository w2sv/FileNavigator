package com.w2sv.filenavigator.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.mediastore.MediaType
import com.w2sv.filenavigator.ui.animateGridItemSpawn
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
import com.w2sv.filenavigator.utils.toggle

@Preview
@Composable
private fun MediaTypeSelectionGridPrev() {
    FileNavigatorTheme {
        MediaTypeSelectionGrid()
    }
}

@Composable
internal fun MediaTypeSelectionGrid(modifier: Modifier = Modifier) {
    val state = rememberLazyListState()
    val nColumns = 2

    LazyVerticalGrid(
        columns = GridCells.Fixed(nColumns),
        modifier = modifier.height(240.dp)
    ) {
        items(MediaType.values().size) {
            MediaTypeCard(
                mediaType = MediaType.values()[it],
                modifier = Modifier
                    .padding(8.dp)
                    .animateGridItemSpawn(it, nColumns, state)
            )
        }
    }
}

@Composable
private fun MediaTypeCard(
    mediaType: MediaType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = mediaType.iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = mediaType.labelRes), fontSize = 18.sp)
                Checkbox(
                    checked = mainScreenViewModel.listenToMediaType.getValue(mediaType),
                    onCheckedChange = { mainScreenViewModel.listenToMediaType.toggle(mediaType) }
                )
            }
            Divider()
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                mediaType.origins.forEachIndexed { i, origin ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = origin.labelRes),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.fillMaxWidth())

                        val originIdentifier = mediaType.originIdentifiers[i]
                        Checkbox(
                            checked = mainScreenViewModel.includeMediaTypeOrigin.getValue(
                                originIdentifier
                            ),
                            onCheckedChange = {
                                mainScreenViewModel.includeMediaTypeOrigin.toggle(originIdentifier)
                            },
                            enabled = mainScreenViewModel.listenToMediaType.getValue(mediaType),
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                        )
                    }
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
            mediaType = MediaType.Image,
            modifier = Modifier.size(160.dp)
        )
    }
}