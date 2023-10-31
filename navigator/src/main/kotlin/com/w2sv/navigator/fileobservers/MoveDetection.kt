package com.w2sv.navigator.fileobservers

import android.net.Uri
import com.w2sv.common.utils.milliSecondsTo
import java.util.Date

data class CutCandidate(val uri: Uri, val changeObservationTime: Date) {

    fun matches(pasteCandidate: PasteCandidate, timeThreshold: Int): Boolean =
        uri != pasteCandidate.uri && changeObservationTime.milliSecondsTo(pasteCandidate.changeObservationTime) < timeThreshold
}

data class PasteCandidate(val uri: Uri, val changeObservationTime: Date)