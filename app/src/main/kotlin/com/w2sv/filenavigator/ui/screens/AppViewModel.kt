package com.w2sv.filenavigator.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.data.model.Theme
import com.w2sv.data.storage.repositories.PreferencesRepository
import com.w2sv.filenavigator.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) : ViewModel() {

    val theme = preferencesRepository.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        Theme.DeviceDefault
    )

    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.saveTheme(theme)
        }
    }

    // ==============
    // BackPress Handling
    // ==============

    val exitApplication get() = _exitApplication.asSharedFlow()
    private val _exitApplication = MutableSharedFlow<Unit>()

    fun onBackPress(context: Context) {
        backPressHandler.invoke(
            onFirstPress = {
                context.showToast(context.getString(R.string.tap_again_to_exit))
            },
            onSecondPress = {
                viewModelScope.launch {
                    _exitApplication.emit(Unit)
                }
            }
        )
    }

    private val backPressHandler = BackPressHandler(
        viewModelScope,
        2500L
    )
}