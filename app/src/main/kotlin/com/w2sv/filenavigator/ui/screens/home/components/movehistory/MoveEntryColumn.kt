package com.w2sv.filenavigator.ui.screens.home.components.movehistory

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.utils.getDocumentUriPath
import com.w2sv.domain.model.MoveEntry
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.components.SnackbarAction
import com.w2sv.filenavigator.ui.components.SnackbarKind
import com.w2sv.filenavigator.ui.components.WeightedBox
import com.w2sv.filenavigator.ui.components.showSnackbarAndDismissCurrent
import com.w2sv.filenavigator.ui.model.MovedFileMediaUriRetrievalResult
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.model.getMovedFileMediaUri
import com.w2sv.filenavigator.ui.screens.home.components.movehistory.model.DateState
import com.w2sv.filenavigator.ui.theme.AppColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoveEntryColumn(
    history: ImmutableList<MoveEntry>,
    launchEntryDeletion: (MoveEntry) -> Job,
    modifier: Modifier = Modifier
) {
    val dateState: DateState = remember(history.size) {
        DateState()
    }

    val onRowClick: (MoveEntry) -> Unit =
        rememberOnRowClick(launchEntryDeletion = launchEntryDeletion)

    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(history, key = { i, _ -> i }) { i, moveEntry ->
            Column {
                dateState.getScopeTitle(i, moveEntry)?.let { scopeTitle ->
                    Text(
                        text = scopeTitle,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                MoveEntryRow(
                    moveEntry = moveEntry,
                    onClick = remember(moveEntry) { { onRowClick(moveEntry) } },
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
private fun rememberOnRowClick(
    launchEntryDeletion: (MoveEntry) -> Job,
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope()
): (MoveEntry) -> Unit {
    return remember {
        {
            scope.launch {
                when (val result = it.getMovedFileMediaUri(context)) {
                    is MovedFileMediaUriRetrievalResult.CouldntFindFile -> {
                        snackbarHostState.showSnackbarAndDismissCurrent(
                            AppSnackbarVisuals(
                                context.getString(R.string.couldn_t_find_file),
                                kind = SnackbarKind.Error,
                                action = SnackbarAction(
                                    context.getString(R.string.delete_entry)
                                ) {
                                    launchEntryDeletion(it)
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                }
                            )
                        )
                    }

                    is MovedFileMediaUriRetrievalResult.Success -> {
                        context.startActivity(
                            Intent()
                                .setAction(Intent.ACTION_VIEW)
                                .setDataAndType(
                                    result.mediaUri,
                                    it.fileType.simpleStorageMediaType.mimeType
                                )
                        )
                    }
                }
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
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
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