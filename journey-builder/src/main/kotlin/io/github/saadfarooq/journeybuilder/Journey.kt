package io.github.saadfarooq.journeybuilder

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Journey(val parcelable: Boolean = false)
