package com.w2sv.datastorage

import com.w2sv.domain.model.FileTypeKind
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KindTest {

    @Test
    fun testParceling() {
        FileTypeKind.Image.testParceling()
        FileTypeKind.PDF.testParceling()
    }
}