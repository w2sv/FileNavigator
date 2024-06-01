package com.w2sv.navigator

import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.moving.MoveFile
import com.w2sv.navigator.utils.TestInstancesProvider
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class MoveFileTest {

    @Test
    fun testParceling() {
        MoveFile(
            mediaStoreFile = TestInstancesProvider.getMediaStoreFile(),
            fileAndSourceType = FileAndSourceType(FileType.Image, SourceType.Camera)
        )
            .testParceling()
    }
}