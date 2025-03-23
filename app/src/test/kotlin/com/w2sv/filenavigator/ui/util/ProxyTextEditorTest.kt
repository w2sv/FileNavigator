package com.w2sv.filenavigator.ui.util

import com.w2sv.common.util.containsSpecialCharacter
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProxyTextEditorTest {

    private lateinit var proxyEditor: ProxyTextEditor<MockInvalidityReason>
    private var proxyValue = ""

    @Before
    fun setUp() {
        proxyEditor = ProxyTextEditor(
            getValue = { proxyValue },
            setValue = { proxyValue = it },
            processInput = { it.trim() },
            findInvalidityReason = { if (it.containsSpecialCharacter()) MockInvalidityReason.ContainsSpecialCharacters else null }
        )
    }

    @Test
    fun `proxyEditor updates value correctly`() {
        proxyEditor.update(" new proxy input ")
        TestCase.assertEquals("new proxy input", proxyValue)
    }

    @Test
    fun `proxyEditor identifies valid input`() {
        proxyEditor.update("valid input")
        TestCase.assertTrue(proxyEditor.isValid)
    }

    @Test
    fun `proxyEditor identifies invalid input`() {
        proxyEditor.update("sdaf-x")
        TestCase.assertFalse(proxyEditor.isValid)
        TestCase.assertEquals(
            MockInvalidityReason.ContainsSpecialCharacters,
            proxyEditor.invalidityReason
        )
    }

    @Test
    fun `proxyEditor pop resets value`() {
        proxyEditor.update("new proxy value")
        val popped = proxyEditor.pop()
        TestCase.assertEquals("new proxy value", popped)
        TestCase.assertEquals("", proxyValue)
    }
}
