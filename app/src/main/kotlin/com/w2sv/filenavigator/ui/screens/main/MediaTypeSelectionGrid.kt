package com.w2sv.filenavigator.ui.screens.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.mediastore.MediaType
import com.w2sv.filenavigator.ui.animateGridItemSpawnOnScrollDown
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
import com.w2sv.filenavigator.ui.theme.RailwayText
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
    val nColumns = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> 3
        else -> 2
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(nColumns),
        modifier = modifier.height(240.dp)
    ) {
        items(MediaType.values().size) {
            MediaTypeCard(
                mediaType = MediaType.values()[it],
                modifier = Modifier
                    .padding(8.dp)
                    .animateGridItemSpawnOnScrollDown(it, nColumns, state)
            )
        }
    }
}

@Composable
private fun MediaTypeCard(
    mediaType: MediaType,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSection(mediaType = mediaType)
            Divider()
            OriginsSection(mediaType = mediaType)
        }
    }
}

@Composable
private fun HeaderSection(
    mediaType: MediaType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    Column(modifier = modifier) {
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.weight(0.2f))
            Box(Modifier.weight(0.6f), contentAlignment = Alignment.Center){
                RailwayText(
                    text = stringResource(id = mediaType.labelRes),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.CenterEnd) {
                Checkbox(
                    checked = mainScreenViewModel.accountForMediaType.getValue(mediaType),
                    onCheckedChange = { mainScreenViewModel.accountForMediaType.toggle(mediaType) },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.tertiary)
                )
            }
        }
    }
}

@Composable
private fun OriginsSection(
    mediaType: MediaType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    Column(modifier = modifier) {
        mediaType.origins.forEach { origin ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.CenterStart){
                    RailwayText(
                        text = stringResource(id = origin.kind.labelRes),
                        fontSize = 13.sp
                    )
                }
                Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.CenterEnd) {
                    Checkbox(
                        checked = mainScreenViewModel.accountForMediaTypeOrigin.getValue(
                            origin
                        ),
                        onCheckedChange = {
                            mainScreenViewModel.accountForMediaTypeOrigin.toggle(origin)
                        },
                        enabled = mainScreenViewModel.accountForMediaType.getValue(mediaType)
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
            mediaType = MediaType.Image,
            modifier = Modifier.size(160.dp)
        )
    }
}