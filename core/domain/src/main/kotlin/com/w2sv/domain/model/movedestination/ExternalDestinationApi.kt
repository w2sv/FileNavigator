package com.w2sv.domain.model.movedestination

interface ExternalDestinationApi : MoveDestinationApi {
    val providerAppLabel: String?
    val providerPackageName: String?
}