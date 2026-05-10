package io.github.saadfarooq.journeybuilder.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.saadfarooq.journeybuilder.BackNavigable
import io.github.saadfarooq.journeybuilder.JourneyStateMachine
import io.github.saadfarooq.journeybuilder.sample.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}

private enum class AppDestination { Home, Parcelable, NonParcelable }

@Composable
private fun App() {
    var destination by rememberSaveable { mutableStateOf(AppDestination.Home) }
    when (destination) {
        AppDestination.Home -> HomeScreen(
            onParcelable = { destination = AppDestination.Parcelable },
            onNonParcelable = { destination = AppDestination.NonParcelable }
        )
        AppDestination.Parcelable -> ParcelableFlow(onExit = { destination = AppDestination.Home })
        AppDestination.NonParcelable -> NonParcelableFlow(onExit = { destination = AppDestination.Home })
    }
}

@Composable
private fun ParcelableFlow(onExit: () -> Unit) {
    val vm: ParcelableJourneyViewModel = viewModel()
    val machine = vm.machine
    RegistrationFlow(
        machine = machine,
        onExit = onExit,
        stateToProps = { state ->
            when (state) {
                is ParcelableRegistrationFormState.Initial -> FlowProps(
                    firstName = state.previousPersonalInfo?.firstName ?: "",
                    lastName = null,
                    age = null,
                    step = FlowStep.FirstName
                )
                is ParcelableRegistrationFormState.PersonalInfo -> FlowProps(
                    firstName = state.firstName,
                    lastName = state.previousContactDetails?.lastName ?: "",
                    age = null,
                    step = FlowStep.LastName
                )
                is ParcelableRegistrationFormState.ContactDetails -> FlowProps(
                    firstName = state.prev.firstName,
                    lastName = state.lastName,
                    age = state.previousAgeDetails?.age ?: "",
                    step = FlowStep.Age
                )
                is ParcelableRegistrationFormState.AgeDetails -> FlowProps(
                    firstName = state.prev.prev.firstName,
                    lastName = state.prev.lastName,
                    age = state.age,
                    step = FlowStep.Summary
                )
            }
        },
        onNext = { state, value ->
            when (state) {
                is ParcelableRegistrationFormState.Initial -> machine.nextFrom(state, value)
                is ParcelableRegistrationFormState.PersonalInfo -> machine.nextFrom(state, value)
                is ParcelableRegistrationFormState.ContactDetails -> machine.nextFrom(state, value)
                is ParcelableRegistrationFormState.AgeDetails -> {}
            }
        },
        onBack = { machine.back() },
        onReset = { machine.reset(ParcelableRegistrationFormState.Initial()) },
        canGoBack = { it is BackNavigable<*> },
        infoBanner = { StateSurvivalBanner() }
    )
}

@Composable
private fun NonParcelableFlow(onExit: () -> Unit) {
    val machine = remember { JourneyStateMachine<RegistrationFormState>(RegistrationFormState.Initial()) }
    RegistrationFlow(
        machine = machine,
        onExit = onExit,
        stateToProps = { state ->
            when (state) {
                is RegistrationFormState.Initial -> FlowProps(
                    firstName = state.previousPersonalInfo?.firstName ?: "",
                    lastName = null,
                    age = null,
                    step = FlowStep.FirstName
                )
                is RegistrationFormState.PersonalInfo -> FlowProps(
                    firstName = state.firstName,
                    lastName = state.previousContactDetails?.lastName ?: "",
                    age = null,
                    step = FlowStep.LastName
                )
                is RegistrationFormState.ContactDetails -> FlowProps(
                    firstName = state.prev.firstName,
                    lastName = state.lastName,
                    age = state.previousAgeDetails?.age ?: "",
                    step = FlowStep.Age
                )
                is RegistrationFormState.AgeDetails -> FlowProps(
                    firstName = state.prev.prev.firstName,
                    lastName = state.prev.lastName,
                    age = state.age,
                    step = FlowStep.Summary
                )
            }
        },
        onNext = { state, value ->
            when (state) {
                is RegistrationFormState.Initial -> machine.nextFrom(state, value)
                is RegistrationFormState.PersonalInfo -> machine.nextFrom(state, value)
                is RegistrationFormState.ContactDetails -> machine.nextFrom(state, value)
                is RegistrationFormState.AgeDetails -> {}
            }
        },
        onBack = { machine.back() },
        onReset = { machine.reset(RegistrationFormState.Initial()) },
        canGoBack = { it is BackNavigable<*> },
        infoBanner = {
            StateLossBanner()
        }
    )
}

@Composable
private fun StateSurvivalBanner() {
    TestingBanner(
        title = "Test state survival:",
        body = "Fill in some fields, background the app, then either:\n" +
            "• Settings → Developer options → \"Don't keep activities\"\n" +
            "• adb shell am kill io.github.saadfarooq.journeybuilder.sample\n\n" +
            "Return via recents — your progress should be restored.",
        color = MaterialTheme.colorScheme.primaryContainer,
        onColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
private fun StateLossBanner() {
    TestingBanner(
        title = "Test state loss:",
        body = "Fill in some fields, then background the app and run:\n" +
            "adb shell am kill io.github.saadfarooq.journeybuilder.sample\n\n" +
            "Return via recents — you'll restart from step 1.",
        color = MaterialTheme.colorScheme.secondaryContainer,
        onColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
}

@Composable
private fun TestingBanner(title: String, body: String, color: androidx.compose.ui.graphics.Color, onColor: androidx.compose.ui.graphics.Color) {
    Surface(color = color, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = onColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodySmall, color = onColor)
        }
    }
}

private enum class FlowStep { FirstName, LastName, Age, Summary }

private data class FlowProps(
    val firstName: String,
    val lastName: String?,
    val age: String?,
    val step: FlowStep
)

@Composable
private fun <S : Any> RegistrationFlow(
    machine: JourneyStateMachine<S>,
    onExit: () -> Unit,
    stateToProps: (S) -> FlowProps,
    onNext: (S, String) -> Unit,
    onBack: () -> Unit,
    onReset: () -> Unit,
    canGoBack: (S) -> Boolean,
    infoBanner: (@Composable () -> Unit)?
) {
    val state by machine.state.collectAsState()
    val props = stateToProps(state)
    val goBack = canGoBack(state)

    BackHandler(enabled = goBack) { onBack() }

    Scaffold(
        topBar = { if (infoBanner != null) infoBanner() }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (props.step) {
                FlowStep.FirstName -> FirstNameScreen(
                    initialFirstName = props.firstName,
                    onNext = { onNext(state, it) }
                )
                FlowStep.LastName -> LastNameScreen(
                    firstName = props.firstName,
                    initialLastName = props.lastName ?: "",
                    onNext = { onNext(state, it) }
                )
                FlowStep.Age -> AgeScreen(
                    initialAge = props.age ?: "",
                    onNext = { onNext(state, it) }
                )
                FlowStep.Summary -> SummaryScreen(
                    firstName = props.firstName,
                    lastName = props.lastName ?: "",
                    age = props.age ?: "",
                    onStartOver = onReset
                )
            }
            IconButton(
                onClick = if (goBack) onBack else onExit,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (goBack) "Back" else "Exit")
            }
        }
    }
}
