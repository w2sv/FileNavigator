package com.w2sv.filenavigator.ui.screens.main.components.movehistory

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.utils.getDocumentUriPath
import com.w2sv.data.model.MoveEntry
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.components.SnackbarKind
import com.w2sv.filenavigator.ui.components.showSnackbarAndDismissCurrent
import com.w2sv.filenavigator.ui.model.MovedFileMediaUriRetrievalResult
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.model.getMovedFileMediaUri
import com.w2sv.filenavigator.ui.theme.AppColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun MoveHistory(moveEntryFlow: Flow<List<MoveEntry>>, modifier: Modifier = Modifier) {
    val moveHistory by rememberUpdatedState(newValue = moveEntryFlow.collectAsState(initial = emptyList()))

    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            AppFontText(
                text = "History",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(0.15f)
            )

            AnimatedContent(
                targetState = moveHistory.value.isEmpty(),
                label = "",
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxWidth()
            ) {
                if (it) {
                    NoHistoryPlaceHolder()
                } else {
                    MoveEntryColumn(history = moveHistory.value)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoveEntryColumn(history: List<MoveEntry>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
    ) {
        items(history, key = { it.hashCode() }) {
            MoveEntryRow(
                moveEntry = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun MoveEntryRow(
    moveEntry: MoveEntry,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                when (val result = moveEntry.getMovedFileMediaUri(context)) {
                    is MovedFileMediaUriRetrievalResult.CouldntFindFile -> {
                        scope.launch {
                            snackbarHostState.showSnackbarAndDismissCurrent(
                                AppSnackbarVisuals(
                                    "Couldn't find file.",
                                    kind = SnackbarKind.Error
                                )
                            )
                        }
                    }

                    is MovedFileMediaUriRetrievalResult.Success -> {
                        context.startActivity(
                            Intent()
                                .setAction(Intent.ACTION_VIEW)
                                .setDataAndType(
                                    result.mediaUri,
                                    moveEntry.fileType.simpleStorageMediaType.mimeType
                                )
                        )
                    }
                }
            }
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        WeightedBox(weight = 0.15f) {
            Icon(
                painter = painterResource(id = moveEntry.fileType.iconRes),
                contentDescription = null,
                tint = moveEntry.fileType.color
            )
        }
        WeightedBox(weight = 0.5f) {
            MoveEntryRowText(text = moveEntry.fileName)
        }
        WeightedBox(weight = 0.1f) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppColor.disabled
            )
        }
        WeightedBox(weight = 0.5f) {
            MoveEntryRowText(
                text = remember(moveEntry.destination) {
                    getDocumentUriPath(
                        moveEntry.destination,
                        context
                    )!!
                },
            )
        }
    }
}

@Composable
fun MoveEntryRowText(text: String, modifier: Modifier = Modifier) {
    AppFontText(
        text = text,
        overflow = TextOverflow.Ellipsis,
        fontSize = 14.sp,
        maxLines = 5,
        modifier = modifier
    )
}

@Composable
fun RowScope.WeightedBox(
    weight: Float,
    contentAlignment: Alignment = Alignment.CenterStart,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.weight(weight), contentAlignment = contentAlignment) {
        content()
    }
}

@Composable
fun NoHistoryPlaceHolder(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_history_24),
            contentDescription = null,
            modifier = Modifier.size(92.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppFontText(
            text = "Navigated files will appear here",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}