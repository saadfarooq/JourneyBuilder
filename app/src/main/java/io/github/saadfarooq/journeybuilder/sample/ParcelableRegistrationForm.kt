package io.github.saadfarooq.journeybuilder.sample

import io.github.saadfarooq.journeybuilder.Journey

@Journey(parcelable = true)
interface ParcelableRegistrationForm {
    interface PersonalInfo { val firstName: String }
    interface ContactDetails : PersonalInfo { val lastName: String }
    interface AgeDetails : ContactDetails { val age: String }
}
