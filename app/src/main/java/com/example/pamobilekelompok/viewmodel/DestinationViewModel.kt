package com.example.pamobilekelompok.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log // Import Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pamobilekelompok.data.SupabaseClient
import com.example.pamobilekelompok.model.Destination
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class DestinationViewModel : ViewModel() {

    var destinations by mutableStateOf<List<Destination>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Fungsi mengambil data
    fun getDestinations() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                Log.d("Supabase", "Sedang mengambil data destinasi...")

                // Ambil data, urutkan dari ID terbesar (terbaru)
                // Jika error 'created_at' tidak ditemukan, ganti "created_at" menjadi "id"
                val result = SupabaseClient.client.from("destinations")
                    .select {
                        order("id", Order.DESCENDING)
                    }.decodeList<Destination>()

                destinations = result
                Log.d("Supabase", "Berhasil! Jumlah data: ${result.size}")

            } catch (e: Exception) {
                errorMessage = "Gagal memuat: ${e.message}"
                Log.e("Supabase", "Error Fetch: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Fungsi Upload
    fun uploadDestination(
        name: String,
        description: String,
        imageUri: Uri,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // 1. Baca File
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) throw Exception("Gagal membaca gambar")

                // 2. Upload Storage
                val fileName = "dest_${System.currentTimeMillis()}.jpg"
                val bucket = SupabaseClient.client.storage.from("destinations")
                bucket.upload(fileName, bytes)
                val publicUrl = bucket.publicUrl(fileName)

                // 3. Simpan Database
                val newDestination = Destination(
                    name = name,
                    description = description,
                    imageUrl = publicUrl
                )
                SupabaseClient.client.from("destinations").insert(newDestination)

                Log.d("Supabase", "Upload Berhasil: $name")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }

                // Refresh data otomatis
                getDestinations()
                onSuccess()

            } catch (e: Exception) {
                errorMessage = "Gagal: ${e.message}"
                Log.e("Supabase", "Error Upload: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }
}