package com.w2sv.navigator.mediastore

import androidx.annotation.VisibleForTesting
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
@VisibleForTesting
internal fun File.contentHash(
    messageDigest: MessageDigest,
    bufferSize: Int = 8192,
): String {
    FileInputStream(this)
        .use { inputStream ->
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                messageDigest.update(buffer, 0, bytesRead)
            }
        }

    return messageDigest.digest().toHexString()
}