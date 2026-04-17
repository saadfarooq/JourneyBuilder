package com.example.formbuilderpager.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FirstNameScreen(initialFirstName: String = "", onNext: (String) -> Unit) {
    var firstName by remember { mutableStateOf(initialFirstName) }
    FormStep(title = "First Name") {
        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNext(firstName) },
            enabled = firstName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Next") }
    }
}
