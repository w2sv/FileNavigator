package com.w2sv.filenavigator.ui.screens.home.components.movehistory

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.utils.getDocumentUriPath
import com.w2sv.domain.model.MoveEntry
import com.w2sv.filenavigator.ui.designsystem.WeightedBox
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.screens.home.components.movehistory.model.DateState
import com.w2sv.filenavigator.ui.theme.AppColor
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoveEntryColumn(
    history: ImmutableList<MoveEntry>,
    onRowClick: (MoveEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateState: DateState = remember(history.size) {
        DateState()
    }

    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(history, key = { i, _ -> i }) { i, moveEntry ->
            Column {
                dateState.getScopeTitle(i, moveEntry)?.let { scopeTitle ->
                    Text(
                        text = scopeTitle,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                MoveEntryRow(
                    moveEntry = moveEntry,
                    onClick = { onRowClick(moveEntry) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MoveEntryRow(
    moveEntry: MoveEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
            )
            .padding(8.dp)
    ) {
        WeightedBox(weight = 0.15f) {
            Icon(
                painter = painterResource(id = moveEntry.source.getIconRes()),
                contentDescription = null,
                tint = moveEntry.fileType.color
            )
        }
        WeightedBox(weight = 0.5f) {
            MoveEntryRowText(text = moveEntry.fileName)
        }
        WeightedBox(weight = 0.1f) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppColor.disabled
            )
        }
        WeightedBox(weight = 0.5f) {
            MoveEntryRowText(
                text = remember(moveEntry.destinationDocumentUri) {
                    getDocumentUriPath(
                        moveEntry.destinationDocumentUri,
                        context
                    )!!
                },
            )
        }
    }
}

@Composable
private fun MoveEntryRowText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        overflow = TextOverflow.Ellipsis,
        fontSize = 14.sp,
        maxLines = 5,
        modifier = modifier
    )
}