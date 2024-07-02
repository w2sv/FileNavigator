package com.w2sv.navigator.mediastore

import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import utils.TestInstancesProvider

@RunWith(RobolectricTestRunner::class)
internal class MoveFileTest {

    @Test
    fun testParceling() {
        TestInstancesProvider
            .getMediaStoreFile()
            .testParceling()
    }
}