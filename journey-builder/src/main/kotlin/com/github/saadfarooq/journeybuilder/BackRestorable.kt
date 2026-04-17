package com.github.saadfarooq.journeybuilder

interface BackRestorable<T> {
    fun withComingFrom(from: T?): T
}
