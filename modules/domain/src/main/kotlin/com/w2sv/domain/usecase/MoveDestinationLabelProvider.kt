package com.w2sv.domain.usecase

import android.content.Context
import com.w2sv.domain.model.movedestination.MoveDestinationApi

/**
 * Converts a move destination into the path text shown to users.
 *
 * Local destination output respects the user's storage volume name preference.
 */
interface MoveDestinationLabelProvider {
    operator fun invoke(moveDestination: MoveDestinationApi, context: Context): String
}
