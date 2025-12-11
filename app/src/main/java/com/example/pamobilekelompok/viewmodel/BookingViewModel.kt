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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

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

class BookingViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var bookingList by mutableStateOf<List<Booking>>(emptyList())

    // 1. Simpan Pesanan Baru
    fun createBooking(
        destinationName: String,
        date: String,
        ticketCount: Int,
        totalPrice: Long,
        context: Context,
        onSuccess: (Long) -> Unit
    ) {
        if (isLoading) return // Anti-Spam Guard
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

                // Insert dan return ID
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

    // 2. Ambil Riwayat
    fun getUserBookings() {
        viewModelScope.launch {
            try {
                isLoading = true
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch

                val result = SupabaseClient.client.from("bookings")
                    .select {
                        filter { eq("user_id", userId) }
                        order("created_at", Order.DESCENDING)
                    }.decodeList<Booking>()

                bookingList = result
            } catch (e: Exception) {
                Log.e("BookingVM", "Error Get History: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // 3. Update Pembayaran
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
}