package com.example.formbuilderpager.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LastNameScreen(firstName: String, initialLastName: String = "", onNext: (String) -> Unit) {
    var lastName by remember { mutableStateOf(initialLastName) }
    FormStep(title = "Last Name") {
        Text("Hello, $firstName")
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNext(lastName) },
            enabled = lastName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Next") }
    }
}
