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
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventViewModel : ViewModel() {

    // data yang dibaca ama ui
    // variabel ini berubah trs layar otomatis keganti
    var events by mutableStateOf<List<Event>>(emptyList())
    var isLoading by mutableStateOf(false)

    // read
    fun getEvents() {
        viewModelScope.launch {
            try {
                isLoading = true

                // request table events
                val result = SupabaseClient.client.from("events")
                    .select {
                        order ("id", Order.DESCENDING) //diurutin dri yg terbaru
                    }.decodeList<Event>() // convert json jdi list

                events = result // simpan ke state
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // create
    fun uploadEvent(
        title: String,
        desc: String,
        date: String,
        imageUrl: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (isLoading) return // biar gk keklik double
        isLoading = true

        viewModelScope.launch {
            try {
                var publicUrl: String? = null

                // upload gambar
                if (imageUrl != null) {
                    // read gambar dri hp
                    val inputStream = context.contentResolver.openInputStream(imageUrl)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        // buat nama file
                        val fileName = "event_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("event-posters")
                        // upload
                        bucket.upload(fileName, bytes)

                        // dapat link gambar buat public
                        publicUrl = bucket.publicUrl(fileName)
                    }
                }

                // save teks dan link ke supabase
                val newEvent = Event(
                    title = title,
                    description = desc,
                    eventDate = date,
                    posterUrl = publicUrl // masukin link gambar sebelumnya
                )

                // masukin ke table events
                SupabaseClient.client.from("events").insert(newEvent)

                // muncul notif dan refresh data
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Event berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                getEvents() // ngambil data terbaru
                onSuccess() // balik ke halaman sebelumnya

            } catch (e: Exception) {

                // klo gagal muncul notif
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // --- FUNGSI DELETE (HAPUS) ---
    fun deleteEvent(event: Event, context: Context) {
        viewModelScope.launch {
            try {
                isLoading = true
                val id = event.id ?: return@launch

                // 1. Hapus dari Database
                SupabaseClient.client.from("events").delete {
                    filter { eq("id", id) }
                }

                // 2. Hapus Gambar dari Storage (Opsional tapi bersih)
                try {
                    val fileName = event.posterUrl?.substringAfterLast("/")
                    if (fileName != null) {
                        SupabaseClient.client.storage.from("event-posters").delete(fileName)
                    }
                } catch (e: Exception) {
                    // Abaikan jika gagal hapus gambar
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Event Terhapus", Toast.LENGTH_SHORT).show()
                }
                getEvents() // Refresh list

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal Hapus: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // --- FUNGSI UPDATE (EDIT) ---
    fun updateEvent(
        id: Long,
        title: String,
        desc: String,
        date: String,
        newImageUri: Uri?,     // Gambar baru (bisa null jika tidak diganti)
        currentImageUrl: String?, // URL gambar lama
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                var finalImageUrl = currentImageUrl

                // 1. Jika user memilih gambar baru, upload dulu
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

                // 2. Update data di Database
                val updatedEvent = Event(
                    id = id,
                    title = title,
                    description = desc,
                    eventDate = date,
                    posterUrl = finalImageUrl
                )

                SupabaseClient.client.from("events").update(updatedEvent) {
                    filter { eq("id", id) }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Event Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
                }
                getEvents() // Refresh list
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
}