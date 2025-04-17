package com.w2sv.flatteningparcelize

import com.w2sv.flatteningparcelize.test.ExtensionSetFileType
import com.w2sv.flatteningparcelize.test.ExtensionSetStaticFileType
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FlatteningParcelizeTest {

    @Test
    fun testParcelingOfDelegatedClass() {
        val original = ExtensionSetFileType(
            delegate = ExtensionSetStaticFileType.Impl(type = "js", name = "JavaScript"),
            colorInt = 0xFF0000
        )

        original.testParceling()
    }
}
