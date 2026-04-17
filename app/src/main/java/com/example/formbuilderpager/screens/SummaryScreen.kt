package com.example.formbuilderpager.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.formbuilderpager.RegistrationFormState

@Composable
fun SummaryScreen(state: RegistrationFormState.AgeDetails, onStartOver: () -> Unit) {
    FormStep(title = "Summary") {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("First Name: ${state.prev.prev.firstName}")
                Text("Last Name: ${state.prev.lastName}")
                Text("Age: ${state.age}")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartOver, modifier = Modifier.fillMaxWidth()) {
            Text("Start Over")
        }
    }
}
