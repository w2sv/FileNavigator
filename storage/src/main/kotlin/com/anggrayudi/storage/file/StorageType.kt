package com.anggrayudi.storage.file

import android.net.Uri
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.extension.getStorageId

/**
 * Created on 17/08/20
 * @author Anggrayudi H
 */
enum class StorageType {
    /**
     * Equals primary storage.
     * @see [SimpleStorage.externalStoragePath]
     */
    EXTERNAL,
    DATA,
    SD_CARD,
    UNKNOWN;

    val isSdCard: Boolean get() = this == SD_CARD

    fun isExpected(actualStorageType: StorageType) =
        this == UNKNOWN || this == actualStorageType

    companion object {

        /**
         * @param storageId get it from [Uri.getStorageId]
         */
        @JvmStatic
        fun fromStorageId(storageId: String) =
            when {
                storageId == StorageId.PRIMARY -> EXTERNAL
                storageId == StorageId.DATA -> DATA
                storageId.matches(DocumentFileCompat.SD_CARD_STORAGE_ID_REGEX) -> SD_CARD
                else -> UNKNOWN
            }
    }
}
