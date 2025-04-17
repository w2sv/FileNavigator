package com.w2sv.flatteningparcelize

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class FlatteningParcelizeProcessorProvider : SymbolProcessorProvider {
    @OptIn(KspExperimental::class)
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        FlatteningParcelizeProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
}
