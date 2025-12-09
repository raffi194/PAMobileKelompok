package com.example.pamobilekelompok.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
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

    // --- AMBIL DATA ---
    fun getDestinations() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                val result = SupabaseClient.client.from("destinations")
                    .select {
                        order("id", Order.DESCENDING)
                    }.decodeList<Destination>()
                destinations = result
            } catch (e: Exception) {
                errorMessage = "Gagal memuat: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // --- UPLOAD DATA ---
    fun uploadDestination(name: String, description: String, imageUri: Uri, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) throw Exception("Gagal baca file")

                val fileName = "dest_${System.currentTimeMillis()}.jpg"
                val bucket = SupabaseClient.client.storage.from("destinations")
                bucket.upload(fileName, bytes)
                val publicUrl = bucket.publicUrl(fileName)

                val newDest = Destination(name = name, description = description, imageUrl = publicUrl)
                SupabaseClient.client.from("destinations").insert(newDest)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                }
                getDestinations() // Refresh Realtime
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

    // --- PERBAIKAN FUNGSI DELETE ---
    fun deleteDestination(destination: Destination, context: Context) {
        viewModelScope.launch {
            try {
                isLoading = true

                // Pastikan ID tidak null
                val destId = destination.id
                if (destId == null) {
                    throw Exception("ID Destinasi tidak valid")
                }

                Log.d("Delete", "Menghapus ID: $destId")

                // 1. Hapus dari Database Supabase
                SupabaseClient.client.from("destinations").delete {
                    filter {
                        // Pastikan kolom di database bernama 'id'
                        eq("id", destId)
                    }
                }

                // 2. Hapus Gambar (Optional, biar storage bersih)
                try {
                    val imageUrl = destination.imageUrl
                    if (imageUrl != null) {
                        // Ambil nama file dari URL (bagian terakhir setelah /)
                        val fileName = imageUrl.substringAfterLast("/")
                        SupabaseClient.client.storage.from("destinations").delete(fileName)
                    }
                } catch (e: Exception) {
                    Log.e("Delete", "Gagal hapus gambar: ${e.message}")
                }

                // 3. Feedback ke User
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                }

                // 4. PENTING: Refresh List agar hilang dari layar (Realtime effect)
                getDestinations()

            } catch (e: Exception) {
                Log.e("Delete", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal menghapus: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }
}