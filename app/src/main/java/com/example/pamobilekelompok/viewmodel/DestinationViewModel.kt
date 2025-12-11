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

    // --- TAMBAH DATA BARU ---
    fun uploadDestination(
        name: String,
        description: String,
        price: Long,
        imageUri: Uri,
        context: Context,
        onSuccess: () -> Unit
    ) {
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

                val newDest = Destination(
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = publicUrl
                )
                SupabaseClient.client.from("destinations").insert(newDest)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                }
                getDestinations()
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

    // --- DELETE DATA
    fun deleteDestination(destination: Destination, context: Context) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val destId = destination.id ?: throw Exception("ID tidak valid")
                SupabaseClient.client.from("destinations").delete { filter { eq("id", destId) } }

                try {
                    val fileName = destination.imageUrl?.substringAfterLast("/")
                    if (fileName != null) SupabaseClient.client.storage.from("destinations").delete(fileName)
                } catch (e: Exception) {}

                withContext(Dispatchers.Main) { Toast.makeText(context, "Data terhapus", Toast.LENGTH_SHORT).show() }
                getDestinations()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Gagal hapus", Toast.LENGTH_SHORT).show() }
            } finally {
                isLoading = false
            }
        }
    }

    // --- UPDATE DATA ---
    fun updateDestination(
        id: Long,
        name: String,
        description: String,
        price: Long,
        newImageUri: Uri?,
        currentImageUrl: String?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                var finalImageUrl = currentImageUrl

                if (newImageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(newImageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val fileName = "dest_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("destinations")
                        bucket.upload(fileName, bytes)
                        finalImageUrl = bucket.publicUrl(fileName)
                    }
                }

                val updatedDest = Destination(
                    id = id,
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = finalImageUrl
                )

                SupabaseClient.client.from("destinations").update(updatedDest) {
                    filter { eq("id", id) }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Data Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
                }
                getDestinations()
                onSuccess()

            } catch (e: Exception) {
                Log.e("Update", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal Update: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }
}