package com.example.formbuilderpager.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AgeScreen(initialAge: String = "", onNext: (String) -> Unit) {
    var age by remember { mutableStateOf(initialAge) }
    FormStep(title = "Age") {
        TextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNext(age) },
            enabled = age.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Finish") }
    }
}
