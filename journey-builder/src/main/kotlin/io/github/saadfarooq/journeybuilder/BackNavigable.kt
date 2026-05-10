package io.github.saadfarooq.journeybuilder

interface BackNavigable<T> {
    fun previousState(): T
}
