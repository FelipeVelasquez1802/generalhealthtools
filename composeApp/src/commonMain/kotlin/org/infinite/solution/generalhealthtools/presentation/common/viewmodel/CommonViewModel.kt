package org.infinite.solution.generalhealthtools.presentation.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal abstract class CommonViewModel<UiStateType, EventTpe>(
    initialState: UiStateType
) : ViewModel() {
    protected val mutableUiState: MutableStateFlow<UiStateType> = MutableStateFlow(initialState)
    val uiState: StateFlow<UiStateType> = mutableUiState
        .onStart { onStart() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialState,
        )

    protected val channelEvent: Channel<EventTpe> = Channel()
    val effect = channelEvent.receiveAsFlow()

    protected open suspend fun onStart() {
//        Intentionally not implemented
    }

    protected open suspend fun onLaunchError() {
        //Intentionally not implemented
    }

    protected fun onLaunch(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (exception: Exception) {
                onLaunchError()
            }
        }
    }
}