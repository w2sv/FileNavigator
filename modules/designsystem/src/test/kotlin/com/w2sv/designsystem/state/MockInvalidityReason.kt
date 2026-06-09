package com.w2sv.designsystem.state

enum class MockInvalidityReason(override val errorMessageRes: Int) : InputInvalidityReason {
    ContainsSpecialCharacters(0)
}
