package com.example.pamobilekelompok.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pamobilekelompok.data.SupabaseClient
import com.example.pamobilekelompok.model.EventBooking
import com.example.pamobilekelompok.model.HotelBooking
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// Model untuk Booking Destinasi
@Serializable
data class Booking(
    val id: Long? = null,
    val user_id: String? = null,
    val destination_name: String,
    val visit_date: String,
    val ticket_count: Int,
    val total_price: Long,
    val status: String = "Menunggu Pembayaran"
)

// Sealed Class untuk Menggabungkan Semua Jenis Booking (Destinasi, Hotel, Event)
sealed class BookingItem {
    data class DestinationBookingItem(val booking: Booking) : BookingItem()
    data class HotelBookingItem(val booking: HotelBooking) : BookingItem()
    data class EventBookingItem(val booking: EventBooking) : BookingItem() // ✅ TAMBAHAN BARU
}

class BookingViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)

    // List untuk Destinasi Bookings
    var bookingList by mutableStateOf<List<Booking>>(emptyList())

    // List untuk Hotel Bookings
    var hotelBookingList by mutableStateOf<List<HotelBooking>>(emptyList())

    // List Gabungan untuk UI (Akan berisi Destinasi, Hotel, dan Event)
    var allBookings by mutableStateOf<List<BookingItem>>(emptyList())

    // ═══════════════════════════════════════════════════════════
    // 📍 DESTINASI BOOKING FUNCTIONS
    // ═══════════════════════════════════════════════════════════

    fun createBooking(
        destinationName: String,
        date: String,
        ticketCount: Int,
        totalPrice: Long,
        context: Context,
        onSuccess: (Long) -> Unit
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: Sesi habis. Login ulang.", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val newBooking = Booking(
                    user_id = currentUser.id,
                    destination_name = destinationName,
                    visit_date = date,
                    ticket_count = ticketCount,
                    total_price = totalPrice
                )

                val result = SupabaseClient.client.from("bookings")
                    .insert(newBooking) { select() }
                    .decodeSingle<Booking>()

                if (result.id != null) {
                    onSuccess(result.id)
                } else {
                    throw Exception("Gagal mendapatkan ID Booking")
                }
            } catch (e: Exception) {
                Log.e("BookingVM", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal Pesan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun updatePaymentStatus(bookingId: Long, context: Context, onSuccess: () -> Unit) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                SupabaseClient.client.from("bookings").update(
                    { set("status", "Lunas") }
                ) {
                    filter { eq("id", bookingId) }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()
                }
                onSuccess()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal Update: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 🏨 HOTEL BOOKING FUNCTIONS
    // ═══════════════════════════════════════════════════════════

    fun createHotelBooking(
        hotelName: String,
        guestName: String,
        guestEmail: String,
        guestPhone: String,
        checkInDate: String,
        checkOutDate: String,
        totalNights: Int,
        pricePerNight: Long,
        totalPrice: Long,
        context: Context,
        onSuccess: (Long) -> Unit
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: Login dulu!", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val newBooking = HotelBooking(
                    userId = currentUser.id,
                    hotelName = hotelName,
                    guestName = guestName,
                    guestEmail = guestEmail,
                    guestPhone = guestPhone,
                    checkInDate = checkInDate,
                    checkOutDate = checkOutDate,
                    totalNights = totalNights,
                    pricePerNight = pricePerNight,
                    totalPrice = totalPrice
                )

                val result = SupabaseClient.client.from("hotel_bookings")
                    .insert(newBooking) { select() }
                    .decodeSingle<HotelBooking>()

                if (result.id != null) {
                    Log.d("BookingVM", "✅ Hotel booking created with ID: ${result.id}")
                    onSuccess(result.id)
                } else {
                    throw Exception("Gagal mendapatkan ID Booking Hotel")
                }
            } catch (e: Exception) {
                Log.e("BookingVM", "❌ Error create hotel booking: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun updateHotelPaymentStatus(bookingId: Long, context: Context, onSuccess: () -> Unit) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                SupabaseClient.client.from("hotel_bookings").update(
                    { set("status", "Lunas") }
                ) {
                    filter { eq("id", bookingId) }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Pembayaran Hotel Berhasil!", Toast.LENGTH_SHORT).show()
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("BookingVM", "Error update hotel payment: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 📜 GET ALL BOOKINGS (DESTINASI + HOTEL + EVENT)
    // ═══════════════════════════════════════════════════════════

    fun getUserBookings() {
        viewModelScope.launch {
            try {
                isLoading = true
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch

                // 1. Get Destinasi Bookings
                val destinations = SupabaseClient.client.from("bookings")
                    .select {
                        filter { eq("user_id", userId) }
                        order("created_at", Order.DESCENDING)
                    }.decodeList<Booking>()

                // 2. Get Hotel Bookings
                val hotels = SupabaseClient.client.from("hotel_bookings")
                    .select {
                        filter { eq("user_id", userId) }
                        order("created_at", Order.DESCENDING)
                    }.decodeList<HotelBooking>()

                // 3. Get Event Bookings (✅ KODE BARU)
                val events = SupabaseClient.client.from("event_bookings")
                    .select {
                        filter { eq("user_id", userId) }
                        order("id", Order.DESCENDING)
                    }.decodeList<EventBooking>()

                // 4. Gabungkan semua ke satu list
                val combined = mutableListOf<BookingItem>()
                destinations.forEach { combined.add(BookingItem.DestinationBookingItem(it)) }
                hotels.forEach { combined.add(BookingItem.HotelBookingItem(it)) }
                events.forEach { combined.add(BookingItem.EventBookingItem(it)) }

                allBookings = combined

                Log.d("BookingVM", "✅ Loaded ${destinations.size} dest + ${hotels.size} hotel + ${events.size} event bookings")
            } catch (e: Exception) {
                Log.e("BookingVM", "Error Get History: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}