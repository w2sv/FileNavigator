package com.w2sv.filenavigator.ui.util

import com.w2sv.common.util.containsSpecialCharacter
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StatefulTextEditorTest {

    private lateinit var statefulEditor: StatefulTextEditor<MockInvalidityReason>

    @Before
    fun setUp() {
        statefulEditor = StatefulTextEditor(
            initialText = "hello",
            processInput = { it.trim() },
            findInvalidityReason = { if (it.containsSpecialCharacter()) MockInvalidityReason.ContainsSpecialCharacters else null }
        )
    }

    @Test
    fun `statefulEditor updates value correctly`() {
        statefulEditor.update(" new input ")
        assertEquals("new input", statefulEditor.getValue())
    }

    @Test
    fun `statefulEditor identifies valid input`() {
        statefulEditor.update("valid input")
        assertTrue(statefulEditor.isValid)
    }

    @Test
    fun `statefulEditor identifies invalid input`() {
        statefulEditor.update(".asdfa")
        assertFalse(statefulEditor.isValid)
        assertEquals(MockInvalidityReason.ContainsSpecialCharacters, statefulEditor.invalidityReason)
    }

    @Test
    fun `statefulEditor pop resets value`() {
        statefulEditor.update("new value")
        val popped = statefulEditor.pop()
        assertEquals("new value", popped)
        assertEquals("hello", statefulEditor.getValue())
    }
}
