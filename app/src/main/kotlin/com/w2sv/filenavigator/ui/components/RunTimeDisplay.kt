package com.w2sv.filenavigator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.w2sv.filenavigator.ui.utils.DoOnStart
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun RunTimeDisplay(
    startDateTime: LocalDateTime,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp
) {
    var runTime by remember(startDateTime) {
        mutableStateOf(
            startDateTime.timeDeltaFromNow()
        )
    }

    LaunchedEffect(key1 = runTime) {
        delay(1000L)
        runTime = runTime.plusSeconds(1L)
    }

    DoOnStart(callback = { runTime = startDateTime.timeDeltaFromNow() })

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppFontText(
            text = "Run time: ",
            fontSize = fontSize,
        )
        AppFontText(
            text = runTime.asFormattedString(),
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}

fun LocalDateTime.timeDeltaFromNow(): Duration =
    durationBetween(LocalDateTime.now())

fun LocalDateTime.durationBetween(other: LocalDateTime): Duration =
    Duration.between(
        this, other
    )

fun Duration.toSecondsCompat(): Long =
    seconds % 60

private fun Duration.asFormattedString(): String =
    String.format(
        "${toDays().let { if (it != 0L) "${it}d " else "" }}%02d:%02d:%02d",
        toHours(),
        toMinutes(),
        toSecondsCompat()
    )