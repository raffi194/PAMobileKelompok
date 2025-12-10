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
import com.example.pamobilekelompok.ui.HomeScreen
import com.example.pamobilekelompok.ui.ProfileScreen
import com.example.pamobilekelompok.ui.auth.LoginScreen
import com.example.pamobilekelompok.ui.auth.RegisterScreen
import com.example.pamobilekelompok.ui.destinations.DestinationDetailScreen
import com.example.pamobilekelompok.ui.destinations.DestinationScreen
import com.example.pamobilekelompok.ui.reviews.ReviewScreen
import com.example.pamobilekelompok.ui.theme.PAMobileKelompokTheme
import com.example.pamobilekelompok.viewmodel.AuthViewModel
import com.example.pamobilekelompok.viewmodel.ReviewViewModel
import io.github.jan.supabase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAMobileKelompokTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }

                // Instance AuthViewModel di level Activity agar datanya awet
                val authViewModel: AuthViewModel = viewModel()

                LaunchedEffect(Unit) {
                    SupabaseClient.client.auth.awaitInitialization()
                    val session = SupabaseClient.client.auth.currentSessionOrNull()
                    if (session != null) {
                        startDestination = "home"
                        authViewModel.getCurrentUser() // Load data user & role saat aplikasi mulai
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
                            // ===== AUTH ROUTES =====
                            composable("login") {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onNavigateSuccess = {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                        authViewModel.getCurrentUser() // Refresh data setelah login
                                    },
                                    onNavigateToRegister = { navController.navigate("register") }
                                )
                            }

                            composable("register") {
                                RegisterScreen(
                                    onNavigateToLogin = { navController.popBackStack() }
                                )
                            }

                            // ===== HOME ROUTE =====
                            composable("home") {
                                HomeScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToFeature = { route -> navController.navigate(route) },
                                    onNavigateToProfile = { navController.navigate("profile") }
                                )
                            }

                            // ===== PROFILE ROUTE =====
                            composable("profile") {
                                ProfileScreen(
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onLogoutSuccess = {
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // ===== DESTINATION ROUTES =====
                            composable("destinations") {
                                DestinationScreen(
                                    isAdmin = authViewModel.isAdmin, // Kirim status admin
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDetail = { destination ->
                                        val encodedUrl = Uri.encode(destination.imageUrl ?: "")
                                        val encodedDesc = Uri.encode(destination.description ?: "")
                                        navController.navigate("destination_detail/${destination.name}/$encodedDesc/$encodedUrl")
                                    }
                                )
                            }

                            composable(
                                route = "destination_detail/{name}/{desc}/{url}",
                                arguments = listOf(
                                    navArgument("name") { type = NavType.StringType },
                                    navArgument("desc") { type = NavType.StringType },
                                    navArgument("url") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                val desc = backStackEntry.arguments?.getString("desc") ?: ""
                                val url = backStackEntry.arguments?.getString("url") ?: ""

                                // Instance ReviewViewModel untuk screen ini
                                val reviewViewModel: ReviewViewModel = viewModel()

                                DestinationDetailScreen(
                                    name = name,
                                    description = desc,
                                    imageUrl = url,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToWriteReview = {
                                        navController.navigate("review/$name")
                                    },
                                    reviewViewModel = reviewViewModel
                                )
                            }

                            // ===== REVIEW ROUTE =====
                            composable(
                                route = "review/{name}",
                                arguments = listOf(
                                    navArgument("name") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val name = backStackEntry.arguments?.getString("name") ?: ""

                                // Instance ReviewViewModel untuk form review
                                val reviewViewModel: ReviewViewModel = viewModel()

                                ReviewScreen(
                                    destinationName = name,
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = reviewViewModel
                                )
                            }

                            // ===== PLACEHOLDER ROUTES (Untuk Fitur Lain) =====
                            composable("foods") {
                                // TODO: Implementasi FoodScreen
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Text("Fitur Kuliner (Coming Soon)")
                                }
                            }

                            composable("hotels") {
                                // TODO: Implementasi HotelScreen
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Text("Fitur Penginapan (Coming Soon)")
                                }
                            }

                            composable("trips") {
                                // TODO: Implementasi TripScreen
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Text("Galeri Perjalanan (Coming Soon)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}