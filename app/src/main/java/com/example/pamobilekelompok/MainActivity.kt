package com.example.pamobilekelompok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pamobilekelompok.data.SupabaseClient
import com.example.pamobilekelompok.ui.AuthScreen
import com.example.pamobilekelompok.ui.theme.PAMobileKelompokTheme
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAMobileKelompokTheme {
                val navController = rememberNavController()

                // State untuk menentukan halaman awal (Login atau Home)
                // Bernilai null saat aplikasi baru dibuka (sedang loading cek sesi)
                var startDestination by remember { mutableStateOf<String?>(null) }

                // 1. CEK SESI (AUTO LOGIN)
                // Efek ini jalan sekali saat aplikasi dibuka
                LaunchedEffect(Unit) {
                    val session = SupabaseClient.client.auth.currentSessionOrNull()
                    if (session != null) {
                        startDestination = "home" // User sudah login -> ke Home
                    } else {
                        startDestination = "login" // Belum login -> ke Login
                    }
                }

                // Tampilkan Loading selagi mengecek sesi
                if (startDestination == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Struktur Navigasi Utama
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination!!,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // --- HALAMAN LOGIN ---
                            composable("login") {
                                AuthScreen(
                                    isRegister = false, // Mode Login
                                    onNavigateSuccess = {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onNavigateToOtherScreen = {
                                        navController.navigate("register")
                                    }
                                )
                            }

                            // --- HALAMAN REGISTER ---
                            composable("register") {
                                AuthScreen(
                                    isRegister = true, // Mode Register
                                    onNavigateSuccess = {
                                        // Opsional: Langsung masuk atau minta login ulang
                                    },
                                    onNavigateToOtherScreen = {
                                        navController.popBackStack() // Kembali ke Login
                                    }
                                )
                            }

                            // --- HALAMAN HOME (MENU UTAMA) ---
                            composable("home") {
                                HomeScreen(
                                    onNavigateToFeature = { route ->
                                        navController.navigate(route)
                                    },
                                    onLogout = {
                                        navController.navigate("login") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // --- NAVIGATION PLACEHOLDER UNTUK ANGGOTA KELOMPOK ---
                            // Nanti diganti dengan Screen masing-masing individu
                            composable("destinations") { Text("Halaman Destinasi (Individu 1)") }
                            composable("foods") { Text("Halaman Kuliner (Individu 2)") }
                            composable("events") { Text("Halaman Event (Individu 3)") }
                            composable("hotels") { Text("Halaman Penginapan (Individu 4)") }
                            composable("reviews") { Text("Halaman Review (Individu 5)") }
                            composable("trips") { Text("Halaman Trip (Individu 6)") }
                        }
                    }
                }
            }
        }
    }
}

// --- KOMPONEN HOME SCREEN (MENU) ---
// Bisa dipindah ke file ui/HomeScreen.kt jika ingin lebih rapi
@Composable
fun HomeScreen(onNavigateToFeature: (String) -> Unit, onLogout: () -> Unit) {
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
                    SupabaseClient.client.auth.signOut() // Logout dari Supabase
                    onLogout() // Pindah ke halaman Login
                } catch (e: Exception) {
                    // Handle error jika logout gagal (jarang terjadi)
                }
            }
        }) {
            Text("Logout")
        }
    }
}