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
import com.example.pamobilekelompok.model.Review
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewViewModel : ViewModel() {

    var reviews by mutableStateOf<List<Review>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // --- AMBIL SEMUA REVIEW BERDASARKAN DESTINASI ---
    fun getReviewsByDestination(destinationName: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                val result = SupabaseClient.client.from("reviews")
                    .select {
                        filter {
                            eq("destination_name", destinationName)
                        }
                        order("created_at", Order.DESCENDING)
                    }.decodeList<Review>()
                reviews = result
            } catch (e: Exception) {
                errorMessage = "Gagal memuat ulasan: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // --- TAMBAH REVIEW BARU (DENGAN FOTO OPSIONAL) ---
    fun addReview(
        destinationName: String,
        rating: Int,
        comment: String,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true

                // Ambil User ID dari Session
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("User belum login")

                var imageUrl: String? = null

                // Upload Foto (Jika Ada)
                if (imageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val fileName = "review_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("reviews")
                        bucket.upload(fileName, bytes)
                        imageUrl = bucket.publicUrl(fileName)
                    }
                }

                // Insert ke Database
                val newReview = Review(
                    userId = userId,
                    destinationName = destinationName,
                    rating = rating,
                    comment = comment,
                    imageUrl = imageUrl
                )

                SupabaseClient.client.from("reviews").insert(newReview)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ulasan berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                }

                // Refresh data
                getReviewsByDestination(destinationName)
                onSuccess()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal menambah ulasan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }
}