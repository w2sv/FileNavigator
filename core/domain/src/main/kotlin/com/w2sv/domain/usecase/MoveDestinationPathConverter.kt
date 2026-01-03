package com.w2sv.domain.usecase

import android.content.Context
import com.w2sv.domain.model.movedestination.MoveDestinationApi

interface MoveDestinationPathConverter {
    operator fun invoke(moveDestination: MoveDestinationApi, context: Context): String
}
