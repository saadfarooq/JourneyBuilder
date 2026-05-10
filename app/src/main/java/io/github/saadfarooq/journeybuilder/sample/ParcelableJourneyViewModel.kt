package io.github.saadfarooq.journeybuilder.sample

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.saadfarooq.journeybuilder.JourneyStateMachine
import kotlinx.coroutines.launch

class ParcelableJourneyViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val machine = JourneyStateMachine(
        savedStateHandle.get<ParcelableRegistrationFormState>("state")
            ?: ParcelableRegistrationFormState.Initial()
    )

    init {
        viewModelScope.launch {
            machine.state.collect { savedStateHandle["state"] = it }
        }
    }
}
