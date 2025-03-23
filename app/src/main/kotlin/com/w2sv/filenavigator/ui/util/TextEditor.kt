package com.w2sv.filenavigator.ui.util

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

interface InputInvalidityReason {
    val errorMessageRes: Int
}

/**
 * @param cleanseInput For doing input cleansing operations the user does not need to be explicitly informed about
 * @param findInvalidityReason For determining an input invalidity reason which the user should be informed about. Will have its result exposed through [invalidityReason].
 */
abstract class TextEditor<T : InputInvalidityReason>(
    private val initialValue: String,
    private val cleanseInput: (String) -> String,
    private val findInvalidityReason: (String) -> T?,
    val getValue: () -> String,
    private val setValue: (String) -> Unit
) {
    fun update(input: String) {
        setValue(cleanseInput(input))
    }

    /** An input invalidity reason the user should be informed about, rather than being silently corrected through [cleanseInput]. */
    val invalidityReason by derivedStateOf { findInvalidityReason(getValue()) }

    /** @return `true` if [invalidityReason] is `null` and the current value is not blank */
    val isValid by derivedStateOf { invalidityReason == null && getValue().isNotBlank() }

    /** Returns the current value before resetting it to [initialValue]. */
    fun pop(): String =
        getValue().also { setValue(initialValue) }
}

@Stable
class StatefulTextEditor<T : InputInvalidityReason> private constructor(
    private val mutableState: MutableState<String>,
    initialText: String,
    processInput: (String) -> String,
    findInvalidityReason: (String) -> T?
) :
    State<String> by mutableState,
    TextEditor<T>(
        initialValue = initialText,
        cleanseInput = processInput,
        findInvalidityReason = findInvalidityReason,
        getValue = { mutableState.value },
        setValue = { mutableState.value = it }
    ) {

    constructor(initialText: String = "", processInput: (String) -> String = { it }, findInvalidityReason: (String) -> T? = { null }) :
        this(
            initialText = initialText,
            mutableState = mutableStateOf(initialText),
            processInput = processInput,
            findInvalidityReason = findInvalidityReason
        )
}

/**
 * A [TextEditor] that does not hold its own text state, but edits one held by some other object, and refers to it via [getValue] and [setValue].
 */
@Stable
class ProxyTextEditor<T : InputInvalidityReason>(
    getValue: () -> String,
    setValue: (String) -> Unit,
    processInput: (String) -> String,
    findInvalidityReason: (String) -> T?
) : TextEditor<T>(getValue(), processInput, findInvalidityReason, getValue, setValue)
