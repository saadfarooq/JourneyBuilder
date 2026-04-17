package com.github.saadfarooq.journeybuilder

interface BackNavigable<T> {
    fun previousState(): T
}
