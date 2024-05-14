package com.w2sv.filenavigator.ui.screens.home.components.movehistory.model

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.platform.LocalContext
import com.w2sv.filenavigator.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun rememberIndexToDateRepresentationMap(
    key1: Any? = null,
    key2: Any? = null,
    context: Context = LocalContext.current
): IndexToDateRepresentationMap =
    remember(key1, key2) {
        IndexToDateRepresentationMap(getString = { context.getString(it) })
    }

@Stable
class IndexToDateRepresentationMap private constructor(
    private val mutableStateMap: SnapshotStateMap<Int, String?>,
    private val now: LocalDateTime,
    private val getString: (Int) -> String
) :
    Map<Int, String?> by mutableStateMap {

    constructor(now: LocalDateTime = LocalDateTime.now(), getString: (Int) -> String) : this(
        mutableStateMap = mutableStateMapOf(),
        now = now,
        getString = getString
    )

    /**
     * Keep track of max index to prevent recomputation of lower index periods.
     */
    private var maxIndex: Int by mutableIntStateOf(-1)

    /**
     * @return A date representation if [dateTime] the first one to correspond to one, otherwise null.
     */
    fun getDateRepresentation(i: Int, dateTime: LocalDateTime): String? =
        mutableStateMap.getOrPut(i) {
            when {
                i > maxIndex -> {
                    val dateRepresentation: String = getDateRepresentation(
                        dateTime = dateTime,
                        now = now,
                        getString = getString
                    )
                    if (dateRepresentation != this[maxIndex]) {
                        maxIndex = i
                        dateRepresentation
                    } else {
                        null
                    }
                }

                else -> null
            }
        }

    fun removeIndex(index: Int) {
        if (index == maxIndex) {
            removeMaxIndex()
        } else {
            val correspondingValue = getValue(index)
            if (correspondingValue != null && getValue(index + 1) == null) {
                mutableStateMap[index + 1] = correspondingValue
            }
            rotateIndicesLeftAndRemoveMaxIndex(index + 1)
        }
    }

    private fun rotateIndicesLeftAndRemoveMaxIndex(startIndex: Int) {
        for (i in (startIndex..maxIndex)) {
            mutableStateMap[i - 1] = getValue(i)
        }
        removeMaxIndex()
    }

    private fun removeMaxIndex() {
        mutableStateMap.remove(maxIndex)
        maxIndex -= 1
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun getDateRepresentation(
    dateTime: LocalDateTime,
    now: LocalDateTime,
    getString: (Int) -> String
): String = when {
    dateTime.toLocalDate() == now.toLocalDate() -> getString(R.string.today)
    dateTime.toLocalDate() == now.minusDays(1).toLocalDate() -> getString(R.string.yesterday)
    else -> dateTime.format(formatter.withZone(ZoneId.systemDefault()))
}

private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")