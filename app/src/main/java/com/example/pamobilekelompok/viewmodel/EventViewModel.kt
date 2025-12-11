package com.example.pamobilekelompok.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pamobilekelompok.data.SupabaseClient
import com.example.pamobilekelompok.model.Event
import com.example.pamobilekelompok.model.EventBooking
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventViewModel : ViewModel() {

    var events by mutableStateOf<List<Event>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun getEvents() {
        viewModelScope.launch {
            try {
                isLoading = true
                val result = SupabaseClient.client.from("events")
                    .select {
                        order("id", Order.DESCENDING)
                    }.decodeList<Event>()
                events = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // --- UPDATED: TAMBAH PARAMETER PRICE ---
    fun uploadEvent(
        title: String,
        desc: String,
        date: String,
        price: Long, // <--- Parameter Baru
        imageUrl: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                var publicUrl: String? = null

                if (imageUrl != null) {
                    val inputStream = context.contentResolver.openInputStream(imageUrl)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val fileName = "event_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("event-posters")
                        bucket.upload(fileName, bytes)
                        publicUrl = bucket.publicUrl(fileName)
                    }
                }

                val newEvent = Event(
                    title = title,
                    description = desc,
                    eventDate = date,
                    price = price, // <--- Simpan Harga
                    posterUrl = publicUrl
                )

                SupabaseClient.client.from("events").insert(newEvent)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Event berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                getEvents()
                onSuccess()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteEvent(event: Event, context: Context) {
        viewModelScope.launch {
            try {
                isLoading = true
                val id = event.id ?: return@launch

                SupabaseClient.client.from("events").delete {
                    filter { eq("id", id) }
                }

                try {
                    val fileName = event.posterUrl?.substringAfterLast("/")
                    if (fileName != null) {
                        SupabaseClient.client.storage.from("event-posters").delete(fileName)
                    }
                } catch (e: Exception) { }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Event Terhapus", Toast.LENGTH_SHORT).show()
                }
                getEvents()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal Hapus: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // --- UPDATED: TAMBAH PARAMETER PRICE ---
    fun updateEvent(
        id: Long,
        title: String,
        desc: String,
        date: String,
        price: Long, // <--- Parameter Baru
        newImageUri: Uri?,
        currentImageUrl: String?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                var finalImageUrl = currentImageUrl

                if (newImageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(newImageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val fileName = "event_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("event-posters")
                        bucket.upload(fileName, bytes)
                        finalImageUrl = bucket.publicUrl(fileName)
                    }
                }

                val updatedEvent = Event(
                    id = id,
                    title = title,
                    description = desc,
                    eventDate = date,
                    price = price, // <--- Update Harga
                    posterUrl = finalImageUrl
                )

                SupabaseClient.client.from("events").update(updatedEvent) {
                    filter { eq("id", id) }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Event Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
                }
                getEvents()
                onSuccess()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal Update: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun bookEvent(
        event: Event,
        bookingDate: String,
        ticketCount: Int,
        context: Context,
        onSuccess: (Long) -> Unit // UBAH KE: (Long) -> Unit agar bisa kirim ID
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val totalPrice = event.price * ticketCount

                val newBooking = EventBooking(
                    userId = currentUser.id,
                    eventId = event.id ?: 0,
                    eventTitle = event.title,
                    bookingDate = bookingDate,
                    ticketCount = ticketCount,
                    totalPrice = totalPrice,
                    status = "Menunggu Pembayaran" // Status Awal
                )

                // PENTING: Tambahkan { select() } agar Supabase mengembalikan data yang baru diinsert (termasuk ID)
                val result = SupabaseClient.client.from("event_bookings")
                    .insert(newBooking) { select() }
                    .decodeSingle<EventBooking>()

                val bookingId = result.id ?: throw Exception("Gagal mendapatkan ID Booking")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Pesanan Dibuat! Silakan Bayar.", Toast.LENGTH_SHORT).show()
                }
                onSuccess(bookingId) // Kirim ID ke layar Payment

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal Booking: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // --- FUNGSI UPDATE PEMBAYARAN (BARU) ---
    fun updatePaymentStatus(bookingId: Long, context: Context, onSuccess: () -> Unit) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                // Update status jadi "Lunas" di tabel event_bookings
                SupabaseClient.client.from("event_bookings").update(
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
                    Toast.makeText(context, "Gagal Bayar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Variabel penampung list history event
    var eventBookings by mutableStateOf<List<EventBooking>>(emptyList())

    // --- FUNGSI AMBIL RIWAYAT EVENT ---
    fun getUserEventBookings() {
        viewModelScope.launch {
            try {
                isLoading = true
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch

                // Ambil data dari tabel 'event_bookings'
                val result = SupabaseClient.client.from("event_bookings")
                    .select {
                        filter { eq("user_id", userId) }
                        order("id", Order.DESCENDING) // Urutkan dari yang terbaru
                    }.decodeList<EventBooking>()

                eventBookings = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}