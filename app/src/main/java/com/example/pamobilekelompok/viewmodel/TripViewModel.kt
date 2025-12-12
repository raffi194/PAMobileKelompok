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

    // get trip (baca db)
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
            } finally {
                isLoading = false
            }
        }
    }

    // upload trip
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
                // upload gambar
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close() ?: throw Exception("Gagal baca file")

                val fileName = "trip_${System.currentTimeMillis()}.jpg"
                val bucket = SupabaseClient.client.storage.from("trips")
                bucket.upload(fileName, bytes!!)
                val publicUrl = bucket.publicUrl(fileName)

                // insert db
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

    // update trip
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

                // pili foto baru = upload yang baru hapus lama
                if (newImageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(newImageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val fileName = "trip_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("trips")
                        bucket.upload(fileName, bytes)
                        finalImageUrl = bucket.publicUrl(fileName)

                        // hapus foto lama
                        try {
                            val oldFileName = trip.mediaUrl.substringAfterLast("/")
                            bucket.delete(oldFileName)
                        } catch (e: Exception) { }
                    }
                }

                // update database
                val updatedTrip = trip.copy(
                    caption = newCaption,
                    mediaUrl = finalImageUrl
                )

                // kirim update ke Supabase
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

    // delete trip
    fun deleteTrip(trip: TripDoc, context: Context) {
        viewModelScope.launch {
            try {
                isLoading = true
                val id = trip.id ?: return@launch
                SupabaseClient.client.from("trip_docs").delete { filter { eq("id", id) } }

                // hapus gambar
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