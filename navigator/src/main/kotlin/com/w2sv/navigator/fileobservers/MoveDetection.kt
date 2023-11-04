package com.w2sv.navigator.fileobservers

import android.net.Uri
import com.w2sv.common.utils.milliSecondsTo
import java.time.LocalDateTime

data class CutCandidate(val uri: Uri, val changeObservationDateTime: LocalDateTime) {

    fun matches(pasteCandidate: PasteCandidate, timeThreshold: Int): Boolean =
        uri != pasteCandidate.uri && changeObservationDateTime.milliSecondsTo(pasteCandidate.changeObservationDateTime) < timeThreshold
}

data class PasteCandidate(val uri: Uri, val changeObservationDateTime: LocalDateTime)