package com.github.saadfarooq.journeybuilder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class JourneyStateMachine<T>(initialState: T) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<T> = _state.asStateFlow()

    fun next(newState: T) {
        _state.value = newState
    }

    fun back(): Boolean {
        val current = _state.value
        @Suppress("UNCHECKED_CAST")
        val previous = (current as? BackNavigable<T>)?.previousState() ?: return false
        _state.value = if (previous is BackRestorable<*>) {
            @Suppress("UNCHECKED_CAST")
            (previous as BackRestorable<T>).withComingFrom(current)
        } else previous
        return true
    }

    fun reset(initialState: T) {
        _state.value = initialState
    }
}
