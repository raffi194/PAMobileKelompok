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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pamobilekelompok.data.SupabaseClient
import com.example.pamobilekelompok.ui.booking.BookingDestinationScreen
import com.example.pamobilekelompok.ui.booking.BookingDestinationSuccessScreen
import com.example.pamobilekelompok.ui.booking.PaymentDestinationScreen
import com.example.pamobilekelompok.ui.booking.OrderHistoryScreen
import com.example.pamobilekelompok.ui.booking.OrderDetailScreen
import com.example.pamobilekelompok.ui.HomeScreen
import com.example.pamobilekelompok.ui.ProfileScreen
import com.example.pamobilekelompok.ui.auth.LoginScreen
import com.example.pamobilekelompok.ui.auth.RegisterScreen
import com.example.pamobilekelompok.ui.destinations.DestinationDetailScreen
import com.example.pamobilekelompok.ui.destinations.DestinationScreen
import com.example.pamobilekelompok.ui.reviews.ReviewScreen
import com.example.pamobilekelompok.ui.trips.TripScreen
import com.example.pamobilekelompok.ui.theme.PAMobileKelompokTheme
import com.example.pamobilekelompok.viewmodel.AuthViewModel
import com.example.pamobilekelompok.viewmodel.Booking
import com.example.pamobilekelompok.viewmodel.BookingViewModel
import com.example.pamobilekelompok.viewmodel.DestinationViewModel
import com.example.pamobilekelompok.viewmodel.TripViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAMobileKelompokTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val destinationViewModel: DestinationViewModel = viewModel()
                val tripViewModel: TripViewModel = viewModel()

                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    SupabaseClient.client.auth.awaitInitialization()
                    val session = SupabaseClient.client.auth.currentSessionOrNull()
                    startDestination = if (session != null) "home" else "login"
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
                            // ... Route Login, Register, Home, Profile ...
                            composable("login") {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onNavigateSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                                    onNavigateToRegister = { navController.navigate("register") }
                                )
                            }
                            composable("register") {
                                RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
                            }
                            composable("home") {
                                HomeScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToFeature = { navController.navigate(it) },
                                    onNavigateToProfile = { navController.navigate("profile") }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onLogoutSuccess = { navController.navigate("login") { popUpTo(0) { inclusive = true } } }
                                )
                            }

                            // --- DESTINASI LIST ---
                            composable("destinations") {
                                DestinationScreen(
                                    viewModel = destinationViewModel,
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDetail = { dest ->
                                        val url = Uri.encode(dest.imageUrl ?: "")
                                        val desc = Uri.encode(dest.description ?: "")
                                        val price = dest.price ?: 0L
                                        navController.navigate("destination_detail/${dest.name}/$desc/$url/$price")
                                    }
                                )
                            }

                            composable(
                                "destination_detail/{name}/{desc}/{url}/{price}",
                                arguments = listOf(navArgument("name"){type=NavType.StringType}, navArgument("desc"){type=NavType.StringType}, navArgument("url"){type=NavType.StringType}, navArgument("price"){type=NavType.LongType})
                            ) {
                                val name = it.arguments?.getString("name") ?: ""
                                val desc = it.arguments?.getString("desc") ?: ""
                                val url = it.arguments?.getString("url") ?: ""
                                val price = it.arguments?.getLong("price") ?: 0L

                                DestinationDetailScreen(
                                    name = name,
                                    description = desc,
                                    imageUrl = url,
                                    price = price,
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToWriteReview = { navController.navigate("review/$name") },
                                    onNavigateToBooking = { navController.navigate("booking_destination/$name/$price") }
                                )
                            }

                            // --- BOOKING ---
                            composable(
                                "booking_destination/{name}/{price}",
                                arguments = listOf(navArgument("name"){type=NavType.StringType}, navArgument("price"){type=NavType.LongType})
                            ) {
                                val name = it.arguments?.getString("name") ?: ""
                                val price = it.arguments?.getLong("price") ?: 0L
                                val vm: BookingViewModel = viewModel()
                                val ctx = LocalContext.current

                                BookingDestinationScreen(
                                    destinationName = name,
                                    ticketPrice = price,
                                    onNavigateBack = { navController.popBackStack() },
                                    onConfirmBooking = { date, count, total ->
                                        vm.createBooking(name, date, count, total, ctx) { id ->
                                            navController.navigate("payment/$id/$total")
                                        }
                                    }
                                )
                            }

                            // --- PAYMENT ---
                            composable(
                                "payment/{bookingId}/{total}",
                                arguments = listOf(navArgument("bookingId"){type=NavType.LongType}, navArgument("total"){type=NavType.LongType})
                            ) {
                                val id = it.arguments?.getLong("bookingId") ?: 0L
                                val total = it.arguments?.getLong("total") ?: 0L

                                PaymentDestinationScreen(
                                    bookingId = id,
                                    totalPrice = total,
                                    onPaymentSuccess = { navController.navigate("booking_destination_success") }
                                )
                            }

                            composable("booking_destination_success") {
                                BookingDestinationSuccessScreen(onNavigateHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } })
                            }

                            // --- RIWAYAT ---
                            composable("booking_list") {
                                OrderHistoryScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDetail = { booking ->
                                        val jsonBooking = Uri.encode(Json.encodeToString(booking))
                                        navController.navigate("booking_detail/$jsonBooking")
                                    }
                                )
                            }

                            composable(
                                route = "booking_detail/{jsonBooking}",
                                arguments = listOf(navArgument("jsonBooking") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val jsonStr = backStackEntry.arguments?.getString("jsonBooking") ?: ""
                                val booking = Json.decodeFromString<Booking>(jsonStr)

                                OrderDetailScreen(
                                    booking = booking,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPayment = { bookingId, totalPrice ->
                                        navController.navigate("payment/$bookingId/$totalPrice")
                                    }
                                )
                            }

                            // --- TRIPS ---
                            composable("trips") {
                                TripScreen(
                                    tripViewModel = tripViewModel,
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // ... (Route Lainnya - Perbaikan Named Argument) ...
                            composable("review/{name}") {
                                val destinationName = it.arguments?.getString("name") ?: ""
                                ReviewScreen(
                                    destinationName = destinationName,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable("foods") { Text("Halaman Kuliner") }
                            composable("events") { Text("Halaman Event") }
                            composable("hotels") { Text("Halaman Penginapan") }
                            composable("reviews") { Text("Halaman Review") }
                        }
                    }
                }
            }
        }
    }
}