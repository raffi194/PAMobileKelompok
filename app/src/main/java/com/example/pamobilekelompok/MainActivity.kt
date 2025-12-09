package com.example.pamobilekelompok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pamobilekelompok.data.SupabaseClient
import com.example.pamobilekelompok.ui.AuthScreen
import com.example.pamobilekelompok.ui.HomeScreen
import com.example.pamobilekelompok.ui.destinations.DestinationScreen
import com.example.pamobilekelompok.ui.theme.PAMobileKelompokTheme
import io.github.jan.supabase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAMobileKelompokTheme {
                val navController = rememberNavController()

                // State awal null = sedang loading cek sesi
                var startDestination by remember { mutableStateOf<String?>(null) }

                // --- 1. CEK SESI (AUTO LOGIN) ---
                LaunchedEffect(Unit) {
                    SupabaseClient.client.auth.awaitInitialization()
                    val session = SupabaseClient.client.auth.currentSessionOrNull()

                    if (session != null) {
                        startDestination = "home"
                    } else {
                        startDestination = "login"
                    }
                }

                if (startDestination == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination!!,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // --- HALAMAN LOGIN ---
                            composable("login") {
                                AuthScreen(
                                    isRegister = false,
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
                                    isRegister = true,
                                    onNavigateSuccess = {
                                        // Opsional
                                    },
                                    onNavigateToOtherScreen = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // --- HALAMAN HOME ---
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

                            // --- FITUR INDIVIDU 1: DESTINASI ---
                            // Perbaikan ada di sini: Mengganti Text placeholder dengan DestinationScreen
                            composable("destinations") {
                                DestinationScreen(
                                    onNavigateBack = {
                                        navController.popBackStack() // Fungsi tombol kembali
                                    }
                                )
                            }

                            // --- PLACEHOLDER FITUR LAIN ---
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