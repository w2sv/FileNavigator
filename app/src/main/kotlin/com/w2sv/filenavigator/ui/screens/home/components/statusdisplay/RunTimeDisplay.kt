package com.w2sv.filenavigator.ui.screens.home.components.statusdisplay

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
import androidx.lifecycle.Lifecycle
import com.w2sv.androidutils.generic.timeDeltaFromNow
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.utils.DoOnLifecycleEvent
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun RunTimeDisplay(
    startDateTime: LocalDateTime,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp
) {
    var runDuration by remember(startDateTime) {
        mutableStateOf(
            startDateTime.timeDeltaFromNow()
        )
    }

    LaunchedEffect(key1 = runDuration) {
        delay(1000L)
        runDuration = runDuration.plusSeconds(1L)
    }

    DoOnLifecycleEvent(
        callback = { runDuration = startDateTime.timeDeltaFromNow() },
        lifecycleEvent = Lifecycle.Event.ON_START
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppFontText(
            text = stringResource(R.string.running_for),
            fontSize = fontSize,
        )
        AppFontText(
            text = runDuration.formatted(),
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// TODO: test
fun Duration.formatted(): String =
    String
        .format("%02d:%02d:%02d:%02d", toDays(), toHours() % 24, toMinutes() % 60, seconds % 60)
        .removePrefix("00:")