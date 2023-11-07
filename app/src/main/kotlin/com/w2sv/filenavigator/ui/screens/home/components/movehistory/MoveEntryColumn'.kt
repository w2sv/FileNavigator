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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import com.w2sv.data.model.MoveEntry
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.components.SnackbarKind
import com.w2sv.filenavigator.ui.components.WeightedBox
import com.w2sv.filenavigator.ui.components.showSnackbarAndDismissCurrent
import com.w2sv.filenavigator.ui.model.MovedFileMediaUriRetrievalResult
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.model.getMovedFileMediaUri
import com.w2sv.filenavigator.ui.screens.home.components.movehistory.model.DateState
import com.w2sv.filenavigator.ui.theme.AppColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoveEntryColumn(
    history: List<MoveEntry>,
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
                    AppFontText(
                        text = scopeTitle,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                MoveEntryRow(
                    moveEntry = moveEntry,
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
private fun MoveEntryRowText(text: String, modifier: Modifier = Modifier) {
    AppFontText(
        text = text,
        overflow = TextOverflow.Ellipsis,
        fontSize = 14.sp,
        maxLines = 5,
        modifier = modifier
    )
}