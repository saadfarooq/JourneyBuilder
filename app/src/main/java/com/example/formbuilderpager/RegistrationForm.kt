package com.example.formbuilderpager

import com.github.saadfarooq.journeybuilder.Journey

@Journey
interface RegistrationForm {
    interface PersonalInfo { val firstName: String }
    interface ContactDetails : PersonalInfo { val lastName: String }
    interface AgeDetails : ContactDetails { val age: String }
}
