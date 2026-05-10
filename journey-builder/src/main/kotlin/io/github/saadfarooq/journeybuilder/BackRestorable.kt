package io.github.saadfarooq.journeybuilder

interface BackRestorable<T> {
    fun withComingFrom(from: T?): T
}
