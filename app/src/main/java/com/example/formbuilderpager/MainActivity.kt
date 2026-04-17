package com.example.formbuilderpager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.formbuilderpager.screens.*
import com.github.saadfarooq.journeybuilder.BackNavigable
import com.github.saadfarooq.journeybuilder.JourneyStateMachine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val machine = JourneyStateMachine<RegistrationFormState>(RegistrationFormState.Initial())
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RegistrationFlow(machine)
                }
            }
        }
    }
}

@Composable
fun RegistrationFlow(machine: JourneyStateMachine<RegistrationFormState>) {
    val state by machine.state.collectAsState()
    val canGoBack = state is BackNavigable<*>

    BackHandler(enabled = canGoBack) { machine.back() }

    Scaffold(
        bottomBar = { if (canGoBack) BackButton(onClick = { machine.back() }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            when (val s = state) {
                is RegistrationFormState.Initial -> FirstNameScreen(
                    initialFirstName = s.previousPersonalInfo?.firstName ?: "",
                    onNext = { machine.nextFrom(s, it) }
                )
                is RegistrationFormState.PersonalInfo -> LastNameScreen(
                    firstName = s.firstName,
                    initialLastName = s.previousContactDetails?.lastName ?: "",
                    onNext = { machine.nextFrom(s, it) }
                )
                is RegistrationFormState.ContactDetails -> AgeScreen(
                    initialAge = s.previousAgeDetails?.age ?: "",
                    onNext = { machine.nextFrom(s, it) }
                )
                is RegistrationFormState.AgeDetails -> SummaryScreen(
                    state = s,
                    onStartOver = { machine.reset(RegistrationFormState.Initial()) }
                )
            }

        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        TextButton(onClick = onClick, modifier = Modifier.align(Alignment.CenterStart)) {
            Text("Back")
        }
    }
}
