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
import com.example.pamobilekelompok.model.TripDoc
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TripViewModel : ViewModel() {

    var tripDocs by mutableStateOf<List<TripDoc>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun getTrips() {
        viewModelScope.launch {
            try {
                isLoading = true
                val result = SupabaseClient.client.from("trip_docs")
                    .select(columns = Columns.list("*, users(display_name)")) {
                        order("created_at", Order.DESCENDING)
                    }.decodeList<TripDoc>()
                tripDocs = result
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    fun uploadTrip(
        caption: String,
        imageUri: Uri,
        userId: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                // 1. Upload Gambar
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close() ?: throw Exception("Gagal baca file")

                val fileName = "trip_${System.currentTimeMillis()}.jpg"
                val bucket = SupabaseClient.client.storage.from("trips")
                bucket.upload(fileName, bytes!!)
                val publicUrl = bucket.publicUrl(fileName)

                // 2. Insert ke DB
                val newTrip = TripDoc(userId = userId, caption = caption, mediaUrl = publicUrl)
                SupabaseClient.client.from("trip_docs").insert(newTrip)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Berhasil diposting!", Toast.LENGTH_SHORT).show()
                }
                getTrips()
                onSuccess()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // --- FUNGSI BARU UNTUK UPDATE ---
    fun updateTrip(
        trip: TripDoc,
        newCaption: String,
        newImageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                var finalImageUrl = trip.mediaUrl

                // 1. Jika User Memilih Foto Baru -> Upload Foto Baru & Hapus Lama
                if (newImageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(newImageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val fileName = "trip_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("trips")
                        bucket.upload(fileName, bytes)
                        finalImageUrl = bucket.publicUrl(fileName)

                        // Hapus foto lama (Opsional, agar hemat storage)
                        try {
                            val oldFileName = trip.mediaUrl.substringAfterLast("/")
                            bucket.delete(oldFileName)
                        } catch (e: Exception) { }
                    }
                }

                // 2. Update Database
                // Kita buat objek baru dengan data yang diubah
                val updatedTrip = trip.copy(
                    caption = newCaption,
                    mediaUrl = finalImageUrl
                )

                // Kirim update ke Supabase (Hanya kolom caption dan media_url yang perlu diupdate sebenernya)
                // Tapi cara termudah pakai object replace dengan filter ID
                SupabaseClient.client.from("trip_docs").update(
                    {
                        set("caption", newCaption)
                        set("media_url", finalImageUrl)
                    }
                ) {
                    filter { eq("id", trip.id!!) }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Berhasil diedit!", Toast.LENGTH_SHORT).show()
                }
                getTrips()
                onSuccess()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal edit: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteTrip(trip: TripDoc, context: Context) {
        viewModelScope.launch {
            try {
                isLoading = true
                val id = trip.id ?: return@launch
                SupabaseClient.client.from("trip_docs").delete { filter { eq("id", id) } }

                // Hapus gambar
                try {
                    val fileName = trip.mediaUrl.substringAfterLast("/")
                    SupabaseClient.client.storage.from("trips").delete(fileName)
                } catch (e: Exception) {}

                withContext(Dispatchers.Main) { Toast.makeText(context, "Terhapus", Toast.LENGTH_SHORT).show() }
                getTrips()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Gagal hapus", Toast.LENGTH_SHORT).show() }
            } finally {
                isLoading = false
            }
        }
    }
}