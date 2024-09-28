package com.w2sv.domain.model

import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri

typealias AnyMoveDestinationEntry = MoveDestinationEntry<*>

sealed interface MoveDestinationEntry<MD : MoveDestination> {
    val destination: MD
    val movedFileDocumentUri: DocumentUri

    val localOrNull: Local?
        get() = this as? Local

    val externalOrNull: External?
        get() = this as? External

    /**
     * @param movedFileDocumentUri For checking whether file still exists
     * @param movedFileMediaUri For ACTION_VIEW intent
     */
    data class Local(
        override val destination: MoveDestination.Directory,
        override val movedFileDocumentUri: DocumentUri,
        val movedFileMediaUri: MediaUri
    ) : MoveDestinationEntry<MoveDestination.Directory>

    @JvmInline
    value class External(
        override val destination: MoveDestination.File.External,
    ) : MoveDestinationEntry<MoveDestination.File.External> {

        override val movedFileDocumentUri: DocumentUri
            get() = destination.documentUri
    }
}