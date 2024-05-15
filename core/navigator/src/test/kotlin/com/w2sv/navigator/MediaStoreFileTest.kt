package com.w2sv.navigator

import com.w2sv.navigator.utils.TestInstancesProvider
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class MediaStoreFileTest {

    @Test
    fun testParceling() {
        TestInstancesProvider
            .getMediaStoreFile()
            .testParceling()
    }
}