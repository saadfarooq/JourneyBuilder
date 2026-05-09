package io.github.saadfarooq.journeybuilder.sample.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onParcelable: () -> Unit, onNonParcelable: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("JourneyBuilder Demo", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Choose a flow to try. Both collect the same data, but behave differently when the app is killed in the background.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onParcelable, modifier = Modifier.fillMaxWidth()) {
            Text("Parcelable Flow")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "State survives process death via SavedStateHandle",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onNonParcelable, modifier = Modifier.fillMaxWidth()) {
            Text("Non-Parcelable Flow")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "State is lost when the process is killed",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}
