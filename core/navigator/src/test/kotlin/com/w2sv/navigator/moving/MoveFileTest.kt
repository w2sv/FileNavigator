package com.w2sv.navigator.moving

import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import util.TestInstancesProvider

@RunWith(RobolectricTestRunner::class)
internal class MoveFileTest {

    @Test
    fun testParceling() {
        TestInstancesProvider
            .moveFile()
            .testParceling()
    }
}