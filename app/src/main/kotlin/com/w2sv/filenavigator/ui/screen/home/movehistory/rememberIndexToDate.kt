package com.w2sv.filenavigator.ui.screen.home.movehistory

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.w2sv.core.common.R
import com.w2sv.domain.model.MovedFile
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Returns a mapping from list indices to date labels for a move history.
 *
 * The returned map contains an entry only for the first occurrence of each distinct
 * date representation (e.g. “Today”, “Yesterday”, or a formatted date), assuming
 * the history is ordered chronologically.
 *
 * @param history Chronologically ordered list of moved files.
 */
@Composable
fun rememberIndexToDateLabel(history: ImmutableList<MovedFile>, context: Context = LocalContext.current): Map<Int, String> =
    remember(history.size, context) {
        firstDateLabels(
            dates = history.map { it.moveDateTime.toLocalDate() },
            getString = { context.getString(it) }
        )
    }

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun firstDateLabels(
    dates: List<LocalDate>,
    getString: (Int) -> String,
    today: LocalDate = LocalDate.now()
): Map<Int, String> {
    var lastDateLabel: String? = null
    return buildMap {
        dates.forEachIndexed { index, date ->
            val dateLabel = dateLabel(
                date = date,
                now = today,
                getString = getString
            )
            if (dateLabel != lastDateLabel) {
                put(index, dateLabel)
                lastDateLabel = dateLabel
            }
        }
    }
}

private fun dateLabel(date: LocalDate, now: LocalDate, getString: (Int) -> String): String =
    when (date) {
        now -> getString(R.string.today)
        now.minusDays(1) -> getString(R.string.yesterday)
        else -> date.format(formatter.withZone(ZoneId.systemDefault()))
    }

private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
