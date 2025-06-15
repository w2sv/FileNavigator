package com.w2sv.filenavigator.ui.screen.home.components.movehistory

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.w2sv.composed.OnLifecycleEvent
import com.w2sv.composed.extensions.thenIf
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.designsystem.WeightedBox
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.modelext.exists
import eu.wewox.textflow.material3.TextFlow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private object MoveHistoryDefaults {
    val FontSize = 14.sp
}

@Composable
fun MoveHistory(
    history: ImmutableList<MovedFile>,
    onRowClick: suspend (MovedFile, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateRepresentationList = rememberFirstDateRepresentations(history)

    LazyColumn(modifier = modifier) {
        itemsIndexed(history, key = { _, moveEntry -> moveEntry.moveDateTime }) { i, moveEntry ->
            dateRepresentationList[i]?.let { dateRepresentation ->
                Text(
                    text = dateRepresentation,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            MoveRecordView(
                movedFile = moveEntry,
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
private fun MoveRecordView(
    movedFile: MovedFile,
    onClick: suspend (MovedFile, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    moveDestinationPathConverter: MoveDestinationPathConverter = LocalMoveDestinationPathConverter.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    var movedFileExists by remember(movedFile) {
        mutableStateOf(movedFile.exists(context))
    }
    OnLifecycleEvent(lifecycleEvent = Lifecycle.Event.ON_START, key1 = movedFile) {
        if (movedFileExists) {
            movedFileExists = movedFile.exists(context)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { scope.launch { onClick(movedFile, movedFileExists) } }
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            .thenIf(!movedFileExists) {
                alpha(0.32f)
            }
            .padding(8.dp)
    ) {
        FileNameWithTypeAndSourceIcon(moveEntry = movedFile, modifier = Modifier.weight(0.7f))
        CompositionLocalProvider(value = LocalContentColor provides MaterialTheme.colorScheme.primary) {
            WeightedBox(weight = 0.2f, contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        WeightedBox(weight = 0.5f) {
            Text(
                text = remember(movedFile.moveDestination) {
                    moveDestinationPathConverter.invoke(
                        movedFile.moveDestination,
                        context
                    )
                },
                fontSize = MoveHistoryDefaults.FontSize
            )
        }
    }
}

@Composable
private fun FileNameWithTypeAndSourceIcon(moveEntry: MovedFile, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        TextFlow(
            text = moveEntry.name,
            fontSize = MoveHistoryDefaults.FontSize
        ) {
            // Placeholder that determines the obstacle size
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(20.dp)
            )
        }
        Icon(
            painter = painterResource(id = moveEntry.fileAndSourceType.iconRes),
            contentDescription = null,
            tint = moveEntry.fileType.color
        )
    }
}
