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
import androidx.compose.runtime.* // Import penting
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel // Import viewModel
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
import com.example.pamobilekelompok.ui.hotels.HotelScreen
import com.example.pamobilekelompok.ui.hotels.HotelDetailScreen
import com.example.pamobilekelompok.ui.booking.HotelBookingScreen
import com.example.pamobilekelompok.ui.booking.HotelBookingSuccessScreen
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
                            composable("login") {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onNavigateSuccess = {
                                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                        authViewModel.getCurrentUser() // Refresh data setelah login
                                    },
                                    onNavigateToRegister = { navController.navigate("register") }
                                )
                            }
                            composable("register") {
                                RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
                            }
                            composable("home") {
                                HomeScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToFeature = { route -> navController.navigate(route) },
                                    onNavigateToProfile = { navController.navigate("profile") }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onLogoutSuccess = {
                                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                    }
                                )
                            }
                            composable("destinations") {
                                DestinationScreen(
                                    isAdmin = authViewModel.isAdmin, // Kirim status admin ke layar destinasi
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDetail = { destination ->
                                        val encodedUrl = Uri.encode(destination.imageUrl ?: "")
                                        val encodedDesc = Uri.encode(destination.description ?: "")
                                        navController.navigate("destination_detail/${destination.name}/$encodedDesc/$encodedUrl")
                                    }
                                )
                            }
                            // 🏨 ROUTE HOTEL
                            composable("hotels") {
                                HotelScreen(
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDetail = { hotel ->
                                        val encodedUrl = Uri.encode(hotel.imageUrl ?: "")
                                        val encodedAddress = Uri.encode(hotel.address ?: "")
                                        val encodedPrice = Uri.encode(hotel.price ?: "")
                                        val encodedDesc = Uri.encode(hotel.description ?: "")
                                        val encodedFacilities = Uri.encode(hotel.facilities ?: "")
                                        navController.navigate(
                                            "hotel_detail/${hotel.id}/${hotel.name}/$encodedAddress/$encodedPrice/$encodedDesc/$encodedFacilities/$encodedUrl"
                                        )
                                    }
                                )
                            }

                            composable(
                                route = "hotel_detail/{id}/{name}/{address}/{price}/{desc}/{facilities}/{url}",
                                arguments = listOf(
                                    navArgument("id") { type = NavType.LongType },
                                    navArgument("name") { type = NavType.StringType },
                                    navArgument("address") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.StringType },
                                    navArgument("desc") { type = NavType.StringType },
                                    navArgument("facilities") { type = NavType.StringType },
                                    navArgument("url") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("id")
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                val address = backStackEntry.arguments?.getString("address")
                                val price = backStackEntry.arguments?.getString("price")
                                val desc = backStackEntry.arguments?.getString("desc")
                                val facilities = backStackEntry.arguments?.getString("facilities")
                                val url = backStackEntry.arguments?.getString("url")

                                HotelDetailScreen(
                                    hotelId = id,
                                    name = name,
                                    address = address,
                                    price = price,
                                    description = desc,
                                    facilities = facilities,
                                    imageUrl = url,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToBooking = { hotelId, hotelName, hotelPrice ->
                                        val encodedName = Uri.encode(hotelName)
                                        val encodedPrice = Uri.encode(hotelPrice ?: "")
                                        navController.navigate("booking_hotel/$hotelId/$encodedName/$encodedPrice")
                                    }
                                )
                            }
// 📝 ROUTE BOOKING HOTEL (BARU)
                            composable(
                                route = "booking_hotel/{hotelId}/{hotelName}/{price}",
                                arguments = listOf(
                                    navArgument("hotelId") { type = NavType.LongType },
                                    navArgument("hotelName") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val hotelId = backStackEntry.arguments?.getLong("hotelId") ?: 0
                                val hotelName = backStackEntry.arguments?.getString("hotelName") ?: ""
                                val price = backStackEntry.arguments?.getString("price")

                                HotelBookingScreen(
                                    hotelId = hotelId,
                                    hotelName = hotelName,
                                    pricePerNight = price,
                                    onNavigateBack = { navController.popBackStack() },
                                    onConfirmBooking = {
                                        navController.navigate("booking_hotel_success")
                                    }
                                )
                            }

                            composable("booking_hotel_success") {
                                HotelBookingSuccessScreen(
                                    onNavigateHome = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            // ... (Route lain tetap sama) ...
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

                                DestinationDetailScreen(
                                    name = name,
                                    description = desc,
                                    imageUrl = url,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToWriteReview = { navController.navigate("review/$name") }
                                )
                            }
                            composable("review/{name}") { backStackEntry ->
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                ReviewScreen(destinationName = name, onNavigateBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}