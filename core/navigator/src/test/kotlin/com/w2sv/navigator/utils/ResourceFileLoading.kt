package com.w2sv.navigator.utils

import java.io.File

internal fun getResourceFile(fileName: String): File =
    File("src/test/resources/$fileName")

internal val File.sizeInMb: Double
    get() = length().toDouble() / 1e6