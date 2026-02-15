package com.w2sv.navigator.shared.createdfiles

internal fun interface EmitSelfCreatedFile {
    suspend operator fun invoke(value: SelfCreatedFileIdentifiers)
}
