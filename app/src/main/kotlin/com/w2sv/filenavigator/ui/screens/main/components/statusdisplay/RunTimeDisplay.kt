package com.w2sv.filenavigator.ui.screens.main.components.statusdisplay

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.w2sv.androidutils.generic.timeDeltaFromNow
import com.w2sv.androidutils.generic.toSecondsCompat
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppFontText(
            text = stringResource(R.string.running_for),
            fontSize = fontSize,
        )
        AppFontText(
            text = runTime.asFormattedString(),
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun Duration.asFormattedString(): String =
    String.format(
        "${toDays().let { if (it != 0L) "${it}d " else "" }}%02d:%02d:%02d",
        toHours(),
        toMinutes(),
        toSecondsCompat()
    )