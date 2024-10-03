package com.w2sv.common.util

import com.w2sv.test.testParceling
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class MediaUriTest {

    private val mediaUri = MediaUri.parse("content://media/external/images/media/1000012597")

    @Test
    fun testParceling() {
        mediaUri.testParceling()
    }

    @Test
    fun testId() {
        assertEquals(MediaId(value = 1000012597), mediaUri.id)
    }
}