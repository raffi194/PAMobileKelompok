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
import com.example.pamobilekelompok.model.Event

// Import UI Screens
import com.example.pamobilekelompok.ui.HomeScreen
import com.example.pamobilekelompok.ui.ProfileScreen
import com.example.pamobilekelompok.ui.auth.LoginScreen
import com.example.pamobilekelompok.ui.auth.RegisterScreen
import com.example.pamobilekelompok.ui.booking.BookingDestinationScreen
import com.example.pamobilekelompok.ui.booking.BookingDestinationSuccessScreen
import com.example.pamobilekelompok.ui.booking.HotelBookingScreen
import com.example.pamobilekelompok.ui.booking.HotelBookingSuccessScreen
import com.example.pamobilekelompok.ui.booking.OrderDetailScreen
import com.example.pamobilekelompok.ui.booking.OrderHistoryScreen
import com.example.pamobilekelompok.ui.booking.PaymentDestinationScreen
import com.example.pamobilekelompok.ui.destinations.DestinationDetailScreen
import com.example.pamobilekelompok.ui.destinations.DestinationScreen
import com.example.pamobilekelompok.ui.events.AddEventScreen
import com.example.pamobilekelompok.ui.events.BookingEventScreen
import com.example.pamobilekelompok.ui.events.EventDetailScreen
import com.example.pamobilekelompok.ui.events.EventScreen
import com.example.pamobilekelompok.ui.foods.FoodScreen
import com.example.pamobilekelompok.ui.hotels.HotelScreen
import com.example.pamobilekelompok.ui.reviews.ReviewScreen
import com.example.pamobilekelompok.ui.trips.TripScreen
import com.example.pamobilekelompok.ui.theme.PAMobileKelompokTheme
import com.example.pamobilekelompok.ui.booking.PaymentHotelScreen
import com.example.pamobilekelompok.ui.hotels.HotelDetailScreen // ✅ JANGAN LUPA IMPORT INI

// Import ViewModels
import com.example.pamobilekelompok.viewmodel.AuthViewModel
import com.example.pamobilekelompok.viewmodel.Booking
import com.example.pamobilekelompok.viewmodel.BookingViewModel
import com.example.pamobilekelompok.viewmodel.DestinationViewModel
import com.example.pamobilekelompok.viewmodel.EventViewModel
import com.example.pamobilekelompok.viewmodel.FoodViewModel
import com.example.pamobilekelompok.viewmodel.ReviewViewModel
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
                val reviewViewModel: ReviewViewModel = viewModel()
                val eventViewModel: EventViewModel = viewModel()
                val foodViewModel: FoodViewModel = viewModel()

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
                            // --- AUTH ---
                            composable("login") {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onNavigateSuccess = {
                                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                    },
                                    onNavigateToRegister = { navController.navigate("register") }
                                )
                            }
                            composable("register") {
                                RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
                            }

                            // --- HOME ---
                            composable("home") {
                                HomeScreen(
                                    authViewModel = authViewModel,
                                    eventViewModel = eventViewModel,
                                    onNavigateToFeature = { navController.navigate(it) },
                                    onNavigateToProfile = { navController.navigate("profile") },
                                    onNavigateToEventDetail = { event ->
                                        val encodedUrl = Uri.encode(event.posterUrl ?: "")
                                        val encodedDesc = Uri.encode(event.description)
                                        navController.navigate("event_detail/${event.id}/${event.title}/$encodedDesc/${event.eventDate}/$encodedUrl/${event.price}")
                                    }
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

                            // --- DESTINASI ---
                            composable("destinations") {
                                DestinationScreen(
                                    viewModel = destinationViewModel,
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDetail = { dest ->
                                        val url = Uri.encode(dest.imageUrl ?: "")
                                        val desc = Uri.encode(dest.description ?: "")
                                        val price = dest.price ?: 0L
                                        navController.navigate("destination_detail/${dest.id}/${dest.name}/$desc/$url/$price")
                                    }
                                )
                            }
                            composable(
                                "destination_detail/{id}/{name}/{desc}/{url}/{price}",
                                arguments = listOf(
                                    navArgument("id") { type = NavType.LongType },
                                    navArgument("name") { type = NavType.StringType },
                                    navArgument("desc") { type = NavType.StringType },
                                    navArgument("url") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.LongType }
                                )
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                val desc = backStackEntry.arguments?.getString("desc") ?: ""
                                val url = backStackEntry.arguments?.getString("url") ?: ""
                                val price = backStackEntry.arguments?.getLong("price") ?: 0L

                                DestinationDetailScreen(
                                    id = id,
                                    name = name,
                                    description = desc,
                                    imageUrl = url,
                                    price = price,
                                    reviewViewModel = reviewViewModel,
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToWriteReview = { navController.navigate("review/$id/$name") },
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
                            ) { backStackEntry ->
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                val price = backStackEntry.arguments?.getLong("price") ?: 0L
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
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("bookingId") ?: 0L
                                val total = backStackEntry.arguments?.getLong("total") ?: 0L
                                val vm: BookingViewModel = viewModel()
                                val context = LocalContext.current

                                PaymentDestinationScreen(
                                    bookingId = id,
                                    totalPrice = total,
                                    isLoading = vm.isLoading,
                                    onPayClicked = {
                                        vm.updatePaymentStatus(id, context) {
                                            navController.navigate("booking_destination_success")
                                        }
                                    }
                                )
                            }

                            composable("booking_destination_success") {
                                BookingDestinationSuccessScreen(onNavigateHome = {
                                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                                })
                            }

                            // --- HOTELS ---
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
                                        // Pastikan URL tujuan benar
                                        navController.navigate("hotel_detail/${hotel.id}/${hotel.name}/$encodedAddress/$encodedPrice/$encodedDesc/$encodedFacilities/$encodedUrl")
                                    }
                                )
                            }

                            // ✅ FIX: TAMBAHKAN ROUTE DETAIL HOTEL DI SINI
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
                                    onNavigateToBooking = { hId, hName, hPrice ->
                                        val safePrice = Uri.encode(hPrice ?: "0")
                                        navController.navigate("booking_hotel/$hId/$hName/$safePrice")
                                    }
                                )
                            }

                            // --- BOOKING HOTEL ---
                            composable(
                                "booking_hotel/{id}/{name}/{price}",
                                arguments = listOf(
                                    navArgument("id") { type = NavType.LongType },
                                    navArgument("name") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                val price = backStackEntry.arguments?.getString("price") ?: "0"
                                val vm: BookingViewModel = viewModel() // Ambil VM

                                HotelBookingScreen(
                                    hotelId = id,
                                    hotelName = name,
                                    pricePerNight = price,
                                    viewModel = vm, // Gunakan VM yang benar
                                    onNavigateBack = { navController.popBackStack() },
                                    onConfirmBooking = { bookingId, total ->
                                        navController.navigate("payment_hotel/$bookingId/$total")
                                    }
                                )
                            }

                            // --- PAYMENT HOTEL ---
                            composable(
                                route = "payment_hotel/{bookingId}/{totalPrice}",
                                arguments = listOf(
                                    navArgument("bookingId") { type = NavType.LongType },
                                    navArgument("totalPrice") { type = NavType.LongType }
                                )
                            ) { backStackEntry ->
                                val bookingId = backStackEntry.arguments?.getLong("bookingId") ?: 0
                                val totalPrice = backStackEntry.arguments?.getLong("totalPrice") ?: 0
                                val vm: BookingViewModel = viewModel() // Gunakan VM yang sama untuk update status
                                val context = LocalContext.current

                                // Gunakan PaymentHotelScreen
                                PaymentHotelScreen(
                                    bookingId = bookingId,
                                    totalPrice = totalPrice,
                                    viewModel = vm, // Kirim ViewModel
                                    onPaymentSuccess = {
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

                            // --- ORDER HISTORY ---
                            composable("booking_list") {
                                // ✅ FIX: Hapus parameter eventViewModel yang tidak ada
                                OrderHistoryScreen(
                                    viewModel = viewModel(), // Cukup ini saja (BookingViewModel)
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

                            // --- REVIEW ---
                            composable(
                                "review/{id}/{name}",
                                arguments = listOf(
                                    navArgument("id") { type = NavType.LongType },
                                    navArgument("name") { type = NavType.StringType }
                                )
                            ) {
                                val id = it.arguments?.getLong("id") ?: 0L
                                val destinationName = it.arguments?.getString("name") ?: ""

                                ReviewScreen(
                                    destinationId = id,
                                    destinationName = destinationName,
                                    viewModel = reviewViewModel,
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // --- EVENTS ---
                            composable("events") {
                                EventScreen(
                                    viewModel = eventViewModel,
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateToAdd = { navController.navigate("add_event") },
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEdit = { event ->
                                        val encodedUrl = Uri.encode(event.posterUrl ?: "")
                                        val encodedDesc = Uri.encode(event.description)
                                        navController.navigate("edit_event/${event.id}/${event.title}/$encodedDesc/${event.eventDate}/$encodedUrl/${event.price}")
                                    },
                                    onNavigateToDetail = { event ->
                                        val encodedUrl = Uri.encode(event.posterUrl ?: "")
                                        val encodedDesc = Uri.encode(event.description)
                                        navController.navigate("event_detail/${event.id}/${event.title}/$encodedDesc/${event.eventDate}/$encodedUrl/${event.price}")
                                    }
                                )
                            }

                            composable("add_event") {
                                AddEventScreen(onNavigateBack = { navController.popBackStack() })
                            }

                            composable(
                                route = "edit_event/{id}/{title}/{desc}/{date}/{url}/{price}",
                                arguments = listOf(
                                    navArgument("id") { type = NavType.LongType },
                                    navArgument("title") { type = NavType.StringType },
                                    navArgument("desc") { type = NavType.StringType },
                                    navArgument("date") { type = NavType.StringType },
                                    navArgument("url") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.LongType }
                                )
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("id")
                                val title = backStackEntry.arguments?.getString("title") ?: ""
                                val desc = backStackEntry.arguments?.getString("desc") ?: ""
                                val date = backStackEntry.arguments?.getString("date") ?: ""
                                val url = backStackEntry.arguments?.getString("url") ?: ""
                                val price = backStackEntry.arguments?.getLong("price") ?: 0L

                                AddEventScreen(
                                    eventId = id,
                                    initialTitle = title,
                                    initialDesc = desc,
                                    initialDate = date,
                                    initialPrice = price.toString(),
                                    initialImageUrl = url,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = "event_detail/{id}/{title}/{desc}/{date}/{url}/{price}",
                                arguments = listOf(
                                    navArgument("id") { type = NavType.LongType },
                                    navArgument("title") { type = NavType.StringType },
                                    navArgument("desc") { type = NavType.StringType },
                                    navArgument("date") { type = NavType.StringType },
                                    navArgument("url") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.LongType }
                                )
                            ) { entry ->
                                val event = Event(
                                    id = entry.arguments?.getLong("id"),
                                    title = entry.arguments?.getString("title") ?: "",
                                    description = entry.arguments?.getString("desc") ?: "",
                                    eventDate = entry.arguments?.getString("date") ?: "",
                                    posterUrl = entry.arguments?.getString("url") ?: "",
                                    price = entry.arguments?.getLong("price") ?: 0L
                                )
                                EventDetailScreen(
                                    event = event,
                                    isAdmin = authViewModel.isAdmin,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToBooking = {
                                        navController.navigate("booking_event/${event.id}/${event.title}/${event.price}/${event.eventDate}")
                                    }
                                )
                            }

                            composable(
                                route = "booking_event/{id}/{title}/{price}/{date}",
                                arguments = listOf(
                                    navArgument("id") { type = NavType.LongType },
                                    navArgument("title") { type = NavType.StringType },
                                    navArgument("price") { type = NavType.LongType },
                                    navArgument("date") { type = NavType.StringType }
                                )
                            ) { entry ->
                                val event = Event(
                                    id = entry.arguments?.getLong("id"),
                                    title = entry.arguments?.getString("title") ?: "",
                                    description = "",
                                    eventDate = entry.arguments?.getString("date") ?: "",
                                    price = entry.arguments?.getLong("price") ?: 0L
                                )
                                BookingEventScreen(
                                    event = event,
                                    viewModel = eventViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPayment = { bookingId, totalPrice ->
                                        navController.navigate("payment_event/$bookingId/$totalPrice")
                                    }
                                )
                            }

                            composable(
                                route = "payment_event/{bookingId}/{total}",
                                arguments = listOf(
                                    navArgument("bookingId") { type = NavType.LongType },
                                    navArgument("total") { type = NavType.LongType }
                                )
                            ) { backStackEntry ->
                                val bookingId = backStackEntry.arguments?.getLong("bookingId") ?: 0L
                                val total = backStackEntry.arguments?.getLong("total") ?: 0L
                                val context = LocalContext.current

                                PaymentDestinationScreen(
                                    bookingId = bookingId,
                                    totalPrice = total,
                                    isLoading = eventViewModel.isLoading,
                                    onPayClicked = {
                                        eventViewModel.updatePaymentStatus(bookingId, context) {
                                            navController.navigate("booking_destination_success")
                                        }
                                    }
                                )
                            }

                            // --- FOODS ---
                            composable("foods") {
                                FoodScreen(
                                    viewModel = foodViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}