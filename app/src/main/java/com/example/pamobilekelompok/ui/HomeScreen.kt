package com.example.pamobilekelompok.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pamobilekelompok.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToFeature: (String) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Menu Utama Pariwisata")
        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Navigasi ke Fitur Individu
        Button(onClick = { onNavigateToFeature("destinations") }) { Text("1. Destinasi") }
        Button(onClick = { onNavigateToFeature("foods") }) { Text("2. Kuliner") }
        Button(onClick = { onNavigateToFeature("events") }) { Text("3. Event") }
        Button(onClick = { onNavigateToFeature("hotels") }) { Text("4. Penginapan") }
        Button(onClick = { onNavigateToFeature("reviews") }) { Text("5. Review") }
        Button(onClick = { onNavigateToFeature("trips") }) { Text("6. Trip Docs") }

        Spacer(modifier = Modifier.height(32.dp))

        // Tombol Logout
        Button(onClick = {
            scope.launch {
                try {
                    SupabaseClient.client.auth.signOut()
                    onLogout()
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }) {
            Text("Logout")
        }
    }
}