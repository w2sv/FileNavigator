package com.w2sv.filenavigator.ui.screen.home.movehistory

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.w2sv.core.common.R
import com.w2sv.domain.model.MovedFile
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.collections.immutable.ImmutableList

/**
 * @param history Chronologically ordered.
 */
@Composable
fun rememberFirstDateRepresentations(
    history: ImmutableList<MovedFile>,
    key1: Any? = null,
    key2: Any? = null,
    context: Context = LocalContext.current
): List<String?> =
    remember(history.size, key1, key2) {
        firstDateRepresentations(
            history = history,
            getString = { context.getString(it) }
        )
    }

private fun firstDateRepresentations(
    history: List<MovedFile>,
    getString: (Int) -> String,
    today: LocalDate = LocalDate.now()
): List<String?> {
    var lastDateRepresentation: String? = null
    return mutableListOf<String?>()
        .apply {
            history.forEach { moveEntry ->
                val representation = getDateRepresentation(
                    date = moveEntry.moveDateTime.toLocalDate(),
                    now = today,
                    getString = getString
                )
                if (representation == lastDateRepresentation) {
                    add(null)
                } else {
                    add(representation)
                    lastDateRepresentation = representation
                }
            }
        }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun getDateRepresentation(
    date: LocalDate,
    now: LocalDate,
    getString: (Int) -> String
): String =
    when (date) {
        now -> getString(R.string.today)
        now.minusDays(1) -> getString(R.string.yesterday)
        else -> date.format(formatter.withZone(ZoneId.systemDefault()))
    }

private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
