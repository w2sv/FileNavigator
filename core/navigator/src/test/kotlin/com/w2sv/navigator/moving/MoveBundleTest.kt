package com.w2sv.navigator.moving

import com.w2sv.common.utils.DocumentUri
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import utils.TestInstancesProvider

@RunWith(RobolectricTestRunner::class)
internal class MoveBundleTest {

    @Test
    fun testParceling() {
        MoveBundle(
            mediaStoreFile = TestInstancesProvider.getMediaStoreFile(),
            fileAndSourceType = FileAndSourceType(FileType.Image, SourceType.Camera),
            moveMode = MoveMode.Auto(DocumentUri.parse("gamba"))
        )
            .testParceling()
    }
}