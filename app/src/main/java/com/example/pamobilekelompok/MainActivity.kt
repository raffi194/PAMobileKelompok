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
// Import Semua Screen
import com.example.pamobilekelompok.ui.booking.BookingDestinationScreen
import com.example.pamobilekelompok.ui.booking.BookingDestinationSuccessScreen
import com.example.pamobilekelompok.ui.booking.PaymentDestinationScreen
import com.example.pamobilekelompok.ui.booking.OrderHistoryScreen
import com.example.pamobilekelompok.ui.booking.OrderDetailScreen
import com.example.pamobilekelompok.ui.booking.HotelBookingScreen
import com.example.pamobilekelompok.ui.booking.HotelBookingSuccessScreen
import com.example.pamobilekelompok.ui.HomeScreen
import com.example.pamobilekelompok.ui.ProfileScreen
import com.example.pamobilekelompok.ui.auth.LoginScreen
import com.example.pamobilekelompok.ui.auth.RegisterScreen
import com.example.pamobilekelompok.ui.destinations.DestinationDetailScreen
import com.example.pamobilekelompok.ui.destinations.DestinationScreen
import com.example.pamobilekelompok.ui.events.AddEventScreen
import com.example.pamobilekelompok.ui.events.EventScreen
import com.example.pamobilekelompok.ui.hotels.HotelScreen
import com.example.pamobilekelompok.ui.hotels.HotelDetailScreen
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
                            // --- LOGIN ---
                            composable("login") {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onNavigateSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
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
                                    onNavigateToFeature = { navController.navigate(it) },
                                    onNavigateToProfile = { navController.navigate("profile") }
                                )
                            }

                            // --- PROFILE ---
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

                            // --- DESTINASI DETAIL ---
                            composable(
                                "destination_detail/{name}/{desc}/{url}/{price}",
                                arguments = listOf(
                                    navArgument("name") { type = NavType.StringType },
                                    navArgument("desc") { type = NavType.StringType },
                                    navArgument("url") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.LongType }
                                )
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

                            // --- BOOKING DESTINASI ---
                            composable(
                                "booking_destination/{name}/{price}",
                                arguments = listOf(
                                    navArgument("name") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.LongType }
                                )
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

                            // --- PAYMENT DESTINASI ---
                            composable(
                                "payment/{bookingId}/{total}",
                                arguments = listOf(
                                    navArgument("bookingId") { type = NavType.LongType },
                                    navArgument("total") { type = NavType.LongType }
                                )
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

                            // 🏨 --- HOTELS LIST ---
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
                                        navController.navigate("hotel_detail/${hotel.id}/${hotel.name}/$encodedAddress/$encodedPrice/$encodedDesc/$encodedFacilities/$encodedUrl")
                                    }
                                )
                            }

                            // 🏨 --- HOTELS DETAIL ---
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

                            // 🏨 --- HOTEL BOOKING FORM ---
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

                            // 🏨 --- HOTEL BOOKING SUCCESS ---
                            composable("booking_hotel_success") {
                                HotelBookingSuccessScreen(
                                    onNavigateHome = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // --- RIWAYAT (ORDER HISTORY) ---
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

                            // --- TRIPS (KOMUNITAS) ---
                            composable("trips") {
                                TripScreen(
                                    tripViewModel = tripViewModel,
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // --- REVIEW ---
                            composable("review/{name}") {
                                val destinationName = it.arguments?.getString("name") ?: ""
                                ReviewScreen(
                                    destinationName = destinationName,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // --- PLACEHOLDER ---
                            composable("foods") { Text("Halaman Kuliner") }

                            // --- FITUR EVENT WISATA (UPDATED) ---
                            composable("events") {
                                EventScreen(
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateToAdd = { navController.navigate("add_event") },
                                    onNavigateBack = { navController.popBackStack() },
                                    // Navigasi ke Edit dengan mengirim Data via URL
                                    onNavigateToEdit = { event ->
                                        // Encode URL agar aman dikirim lewat navigasi
                                        val encodedUrl = Uri.encode(event.posterUrl ?: "")
                                        val encodedDesc = Uri.encode(event.description)

                                        navController.navigate("edit_event/${event.id}/${event.title}/$encodedDesc/${event.eventDate}/$encodedUrl")
                                    }
                                )
                            }

                            // --- HALAMAN TAMBAH (Create) ---
                            composable("add_event") {
                                AddEventScreen(onNavigateBack = { navController.popBackStack() })
                            }

                            // --- HALAMAN EDIT (Update) - Rute Baru ---
                            composable(
                                route = "edit_event/{id}/{title}/{desc}/{date}/{url}",
                                arguments = listOf(
                                    navArgument("id") { type = NavType.LongType },
                                    navArgument("title") { type = NavType.StringType },
                                    navArgument("desc") { type = NavType.StringType },
                                    navArgument("date") { type = NavType.StringType },
                                    navArgument("url") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("id")
                                val title = backStackEntry.arguments?.getString("title") ?: ""
                                val desc = backStackEntry.arguments?.getString("desc") ?: ""
                                val date = backStackEntry.arguments?.getString("date") ?: ""
                                val url = backStackEntry.arguments?.getString("url") ?: ""

                                AddEventScreen(
                                    eventId = id,
                                    initialTitle = title,
                                    initialDesc = desc,
                                    initialDate = date,
                                    initialImageUrl = url,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            // hotels sudah dihandle di atas
                            composable("reviews") { Text("Halaman Review") }
                        }
                    }
                }
            }
        }
    }
}