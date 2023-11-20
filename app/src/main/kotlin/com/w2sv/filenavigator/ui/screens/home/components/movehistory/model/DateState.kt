package com.w2sv.filenavigator.ui.screens.home.components.movehistory.model

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.w2sv.domain.model.MoveEntry
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Stable
class DateState {
    private val now = LocalDateTime.now()
    private val indexToDate = mutableStateMapOf<Int, String?>()
    private var lastComputedIndexToDate: Pair<Int, String>? by mutableStateOf(null)

    fun getScopeTitle(i: Int, moveEntry: com.w2sv.domain.model.MoveEntry): String? =
        indexToDate.getOrPut(i) {
            if (lastComputedIndexToDate == null || i > lastComputedIndexToDate!!.first) {
                getDateRepresentation(
                    moveEntry.dateTime,
                    now
                )
                    .let { scopeTitle ->
                        if (lastComputedIndexToDate == null || scopeTitle != lastComputedIndexToDate!!.second) scopeTitle.also {
                            lastComputedIndexToDate = i to scopeTitle

                            slimber.log.i { "Put ${moveEntry.hashCode()}=$this" }
                        } else null
                    }
            } else null
        }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun getDateRepresentation(
    dateTime: LocalDateTime,
    now: LocalDateTime
): String = when {
    dateTime.toLocalDate() == now.toLocalDate() -> "Today"
    dateTime.toLocalDate() == now.minusDays(1).toLocalDate() -> "Yesterday"
    else -> dateTime.format(formatter.withZone(ZoneId.systemDefault()))
}

private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")