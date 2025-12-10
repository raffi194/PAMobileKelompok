// PAMobileKelompok/app/src/main/java/com/example/pamobilekelompok/viewmodel/ReviewViewModel.kt
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
import com.example.pamobilekelompok.data.model.Review
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ReviewViewModel : ViewModel() {

    // State untuk List Review
    var reviewList by mutableStateOf<List<Review>>(emptyList())
        private set

    // State untuk Loading dan Error
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchReviews()
    }

    // --- READ OPERATION ---
    fun fetchReviews() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Ambil data dari tabel reviews, diurutkan berdasarkan created_at terbaru
                val response = SupabaseClient.client.postgrest["reviews"]
                    .select {
                        order("created_at", Order.DESCENDING)
                    }

                reviewList = response.decodeList<Review>()

            } catch (e: Exception) {
                errorMessage = "Gagal memuat review: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // --- INSERT OPERATION (with optional image upload) ---
    fun insertReview(
        placeName: String,
        comment: String,
        rating: Int,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
            if (currentUser == null) {
                errorMessage = "Anda harus login untuk memberi review."
                isLoading = false
                return@launch
            }

            var imageUrl: String? = null

            // 1. UPLOAD IMAGE ke Supabase Storage (Jika ada)
            if (imageUri != null) {
                try {
                    // Buat nama file unik dan tentukan bucket 'reviews'
                    val filename = "review_image_${UUID.randomUUID()}.jpg"
                    val bucket = SupabaseClient.client.storage["reviews"]

                    // Mendapatkan ByteArray dari Uri file lokal
                    val imageByteArray = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                    }

                    if (imageByteArray == null) {
                        errorMessage = "Gagal membaca file gambar."
                        isLoading = false
                        return@launch
                    }

                    // Upload file
                    bucket.upload(
                        path = filename,
                        data = imageByteArray,
                        upsert = false
                    )

                    // Dapatkan URL publik
                    imageUrl = bucket.publicUrl(filename)

                } catch (e: Exception) {
                    errorMessage = "Gagal upload foto: ${e.message}"
                    isLoading = false
                    return@launch
                }
            }

            // 2. INSERT DATA ke Supabase DB
            try {
                val newReview = Review(
                    userEmail = currentUser.email ?: "Unknown User",
                    placeName = placeName,
                    comment = comment,
                    rating = rating,
                    imageUrl = imageUrl
                )

                SupabaseClient.client.postgrest["reviews"].insert(newReview)

                Toast.makeText(context, "Review berhasil diunggah!", Toast.LENGTH_SHORT).show()
                fetchReviews() // Refresh list setelah insert
                onSuccess()

            } catch (e: Exception) {
                errorMessage = "Gagal menyimpan review: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}