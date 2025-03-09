package com.w2sv.filenavigator.ui.util

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

interface InputInvalidityReason {
    val errorMessage: String
}

abstract class TextEditor<T : InputInvalidityReason>(
    private val initialValue: String,
    private val processInput: (String) -> String,
    private val findInvalidityReason: (String) -> T?,
    val getValue: () -> String,
    private val setValue: (String) -> Unit,
) {
    fun update(input: String) {
        setValue(processInput(input))
    }

    val invalidityReason by derivedStateOf { findInvalidityReason(getValue()) }
    val isValid by derivedStateOf { invalidityReason == null && getValue().isNotBlank() }

    fun pop(): String = getValue().also { setValue(initialValue) }
}

@Stable
class StatefulTextEditor<T : InputInvalidityReason> private constructor(
    private val mutableState: MutableState<String>,
    initialText: String,
    processInput: (String) -> String,
    findInvalidityReason: (String) -> T?
) :
    State<String> by mutableState, TextEditor<T>(
    initialValue = initialText,
    processInput = processInput,
    findInvalidityReason = findInvalidityReason,
    getValue = { mutableState.value },
    setValue = { mutableState.value = it }) {

    constructor(initialText: String = "", processInput: (String) -> String = { it }, findInvalidityReason: (String) -> T? = { null })
        : this(
        initialText = initialText,
        mutableState = mutableStateOf(initialText),
        processInput = processInput,
        findInvalidityReason = findInvalidityReason
    )
}

@Stable
class ProxyTextEditor<T : InputInvalidityReason>(
    getValue: () -> String,
    setValue: (String) -> Unit,
    processInput: (String) -> String,
    findInvalidityReason: (String) -> T?
) : TextEditor<T>(getValue(), processInput, findInvalidityReason, getValue, setValue)
