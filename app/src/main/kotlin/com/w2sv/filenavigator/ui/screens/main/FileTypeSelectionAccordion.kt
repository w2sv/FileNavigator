package com.w2sv.filenavigator.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.mediastore.FileType
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
import com.w2sv.filenavigator.ui.theme.RailwayText
import com.w2sv.filenavigator.utils.toggle

@Composable
fun FileTypeAccordionColumn(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.padding(horizontal = 10.dp)) {
        items(FileType.all) {
            this@LazyColumn.FileTypeAccordion(fileType = it)
        }
    }
}

@Composable
private fun LazyListScope.FileTypeAccordion(
    fileType: FileType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    AccordionHeader(fileType = fileType)
    AnimatedVisibility(visible = mainScreenViewModel.accountForFileType.getValue(fileType)) {
        AccordionCorpus(fileType = fileType)
    }
}

@Composable
private fun AccordionHeader(
    fileType: FileType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = fileType.iconRes),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = fileType.color
        )
        Spacer(modifier = Modifier.width(14.dp))
        RailwayText(
            text = stringResource(id = fileType.titleRes),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.weight(0.8f))
        Switch(
            checked = mainScreenViewModel.accountForFileType.getValue(fileType),
            onCheckedChange = {
                mainScreenViewModel.accountForFileType.toggle(
                    fileType
                )
            }
        )
    }
}

@Composable
private fun LazyListScope.AccordionCorpus(
    fileType: FileType,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.padding(top = 8.dp)
    ) {
        items(fileType.origins) { origin ->
            FileTypeOriginRow(origin = origin)
        }
    }
}

@Composable
private fun FileTypeOriginRow(
    origin: FileType.Origin,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        RailwayText(text = stringResource(id = origin.kind.labelRes))
        Checkbox(
            checked = mainScreenViewModel.accountForFileTypeOrigin.getValue(origin),
            onCheckedChange = {
                mainScreenViewModel.accountForFileTypeOrigin.toggle(origin)
            }
        )
    }
}

@Preview
@Composable
private fun HeaderPrev() {
    FileNavigatorTheme {
        AccordionHeader(fileType = FileType.Image)
    }
}