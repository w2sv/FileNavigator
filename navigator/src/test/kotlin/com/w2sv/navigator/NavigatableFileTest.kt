package com.w2sv.navigator

import com.w2sv.data.model.FileType
import com.w2sv.navigator.model.NavigatableFile
import com.w2sv.navigator.utils.TestInstancesProvider
import com.w2sv.navigator.utils.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NavigatableFileTest {

    @Test
    fun testParceling() {
        NavigatableFile(
            mediaStoreFile = TestInstancesProvider.getMediaStoreFile(),
            type = FileType.Media.Image,
            sourceKind = FileType.Source.Kind.Screenshot
        )
            .testParceling()
    }
}