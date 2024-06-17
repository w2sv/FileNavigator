package com.w2sv.filenavigator.ui.screens.home.components.movehistory

import android.content.Context
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.w2sv.composed.OnLifecycleEvent
import com.w2sv.composed.extensions.thenIf
import com.w2sv.domain.model.MoveEntry
import com.w2sv.domain.usecase.DocumentUriToPathConverter
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.WeightedBox
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.model.movedFileExists
import com.w2sv.filenavigator.ui.screens.home.components.movehistory.model.rememberFirstDateRepresentations
import com.w2sv.filenavigator.ui.theme.onSurfaceDisabled
import com.w2sv.filenavigator.ui.utils.LocalDocumentUriToPathConverter
import eu.wewox.textflow.TextFlow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MoveHistory(
    history: ImmutableList<MoveEntry>,
    onRowClick: suspend (MoveEntry, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateRepresentationList = rememberFirstDateRepresentations(history)

    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(history, key = { _, moveEntry -> moveEntry.dateTime }) { i, moveEntry ->
            dateRepresentationList[i]?.let { dateRepresentation ->
                Text(
                    text = dateRepresentation,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            MoveEntryView(
                moveEntry = moveEntry,
                onClick = onRowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun MoveEntryView(
    moveEntry: MoveEntry,
    onClick: suspend (MoveEntry, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    documentUriToPathConverter: DocumentUriToPathConverter = LocalDocumentUriToPathConverter.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    var movedFileExists by remember(moveEntry) {
        mutableStateOf(moveEntry.movedFileExists(context))
    }
    OnLifecycleEvent(lifecycleEvent = Lifecycle.Event.ON_START, key1 = moveEntry) {
        if (movedFileExists) {
            movedFileExists = moveEntry.movedFileExists(context)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { scope.launch { onClick(moveEntry, movedFileExists) } }
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
            )
            .thenIf(!movedFileExists) {
                alpha(0.32f)
            }
            .padding(8.dp)
    ) {
        TextFlow(
            text = moveEntry.fileName,
            modifier = Modifier.weight(0.5f),
            color = LocalContentColor.current,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            maxLines = 5,
        ) {
            Icon(
                painter = painterResource(id = moveEntry.combinedFileAndSourceTypeIconRes),
                contentDescription = null,
                tint = moveEntry.fileType.color,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        WeightedBox(weight = 0.2f) {
            CompositionLocalProvider(value = LocalContentColor provides MaterialTheme.colorScheme.onSurfaceDisabled) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceDisabled
                    )
                    if (moveEntry.autoMoved) {
                        Text(
                            text = stringResource(id = R.string.auto),
                            fontSize = 13.sp,
                            lineHeight = 2.sp
                        )
                    }
                }
            }
        }
        WeightedBox(weight = 0.5f) {
            MoveEntryViewText(
                text = remember(moveEntry.destinationDocumentUri) {
                    documentUriToPathConverter.invoke(
                        moveEntry.destinationDocumentUri,
                        context
                    )!!
                },
            )
        }
    }
}

@Composable
private fun MoveEntryViewText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        overflow = TextOverflow.Ellipsis,
        fontSize = 14.sp,
        maxLines = 5,
        modifier = modifier
    )
}