package com.example.pamobilekelompok

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pamobilekelompok.data.SupabaseClient
// Import Screen
import com.example.pamobilekelompok.ui.booking.BookingDestinationScreen
import com.example.pamobilekelompok.ui.booking.BookingDestinationSuccessScreen
import com.example.pamobilekelompok.ui.HomeScreen
import com.example.pamobilekelompok.ui.ProfileScreen
import com.example.pamobilekelompok.ui.auth.LoginScreen
import com.example.pamobilekelompok.ui.auth.RegisterScreen
import com.example.pamobilekelompok.ui.destinations.DestinationDetailScreen
import com.example.pamobilekelompok.ui.destinations.DestinationScreen
import com.example.pamobilekelompok.ui.reviews.ReviewScreen
import com.example.pamobilekelompok.ui.theme.PAMobileKelompokTheme
import com.example.pamobilekelompok.viewmodel.AuthViewModel
import io.github.jan.supabase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAMobileKelompokTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }
                val authViewModel: AuthViewModel = viewModel()

                LaunchedEffect(Unit) {
                    SupabaseClient.client.auth.awaitInitialization()
                    val session = SupabaseClient.client.auth.currentSessionOrNull()
                    if (session != null) {
                        startDestination = "home"
                        authViewModel.getCurrentUser()
                    } else {
                        startDestination = "login"
                    }
                }

                if (startDestination == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination!!,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // --- LOGIN ---
                            composable("login") {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onNavigateSuccess = {
                                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                        authViewModel.getCurrentUser()
                                    },
                                    onNavigateToRegister = { navController.navigate("register") }
                                )
                            }

                            // --- REGISTER ---
                            composable("register") {
                                RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
                            }

                            // --- HOME ---
                            composable("home") {
                                HomeScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToFeature = { route -> navController.navigate(route) },
                                    onNavigateToProfile = { navController.navigate("profile") }
                                )
                            }

                            // --- PROFILE ---
                            composable("profile") {
                                ProfileScreen(
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onLogoutSuccess = {
                                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                    }
                                )
                            }

                            // --- DESTINASI LIST ---
                            composable("destinations") {
                                DestinationScreen(
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDetail = { destination ->
                                        val encodedUrl = Uri.encode(destination.imageUrl ?: "")
                                        val encodedDesc = Uri.encode(destination.description ?: "")
                                        val price = destination.price ?: 0L // Ambil Harga dari Model

                                        // Kirim semua data termasuk harga ke detail
                                        navController.navigate("destination_detail/${destination.name}/$encodedDesc/$encodedUrl/$price")
                                    }
                                )
                            }

                            // --- DESTINASI DETAIL ---
                            composable(
                                route = "destination_detail/{name}/{desc}/{url}/{price}", // Tambah param {price}
                                arguments = listOf(
                                    navArgument("name") { type = NavType.StringType },
                                    navArgument("desc") { type = NavType.StringType },
                                    navArgument("url") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.LongType } // Tipe Long untuk harga
                                )
                            ) { backStackEntry ->
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                val desc = backStackEntry.arguments?.getString("desc") ?: ""
                                val url = backStackEntry.arguments?.getString("url") ?: ""
                                val price = backStackEntry.arguments?.getLong("price") ?: 0L // Terima Harga

                                DestinationDetailScreen(
                                    name = name,
                                    description = desc,
                                    imageUrl = url,
                                    price = price, // Pass ke screen
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToWriteReview = { navController.navigate("review/$name") },
                                    onNavigateToBooking = {
                                        // Kirim nama dan harga ke halaman booking
                                        navController.navigate("booking_destination/$name/$price")
                                    }
                                )
                            }

                            // --- HALAMAN BOOKING DESTINASI ---
                            composable(
                                route = "booking_destination/{name}/{price}", // Tambah param {price}
                                arguments = listOf(
                                    navArgument("name") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.LongType } // Tipe Long
                                )
                            ) { backStackEntry ->
                                val name = backStackEntry.arguments?.getString("name") ?: "Destinasi"
                                val price = backStackEntry.arguments?.getLong("price") ?: 0L // Terima Harga

                                BookingDestinationScreen(
                                    destinationName = name,
                                    ticketPrice = price, // Gunakan harga asli
                                    onNavigateBack = { navController.popBackStack() },
                                    onConfirmBooking = {
                                        navController.navigate("booking_destination_success")
                                    }
                                )
                            }

                            // --- HALAMAN BOOKING SUKSES ---
                            composable("booking_destination_success") {
                                BookingDestinationSuccessScreen(
                                    onNavigateHome = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // --- REVIEW ---
                            composable("review/{name}") { backStackEntry ->
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                ReviewScreen(destinationName = name, onNavigateBack = { navController.popBackStack() })
                            }

                            // --- PLACEHOLDER FITUR LAIN ---
                            composable("foods") { Text("Halaman Kuliner") }
                            composable("events") { Text("Halaman Event") }
                            composable("hotels") { Text("Halaman Penginapan") }
                            composable("reviews") { Text("Halaman Review") }
                            composable("trips") { Text("Halaman Trip") }
                        }
                    }
                }
            }
        }
    }
}