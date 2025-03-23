package com.w2sv.filenavigator.ui.util

enum class MockInvalidityReason(override val errorMessageRes: Int) : InputInvalidityReason {
    ContainsSpecialCharacters(0)
}
