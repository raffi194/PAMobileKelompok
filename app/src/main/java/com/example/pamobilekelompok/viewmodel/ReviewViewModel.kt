package com.example.pamobilekelompok.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pamobilekelompok.data.SupabaseClient
import com.example.pamobilekelompok.model.Review
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

    fun getReviews(destinationId: Long) {
        viewModelScope.launch {
            try {
                isLoading = true
                val result = SupabaseClient.client.from("review")
                    .select {
                        filter {
                            eq("destination_id", destinationId)
                        }
                        order("id", Order.DESCENDING)
                    }.decodeList<Review>()
                reviews = result
            } catch (e: Exception) {
                errorMessage = "Gagal memuat review: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun submitReview(
        destinationId: Long,
        userName: String,
        rating: Int,
        comment: String,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                var finalImageUrl: String? = null

                if (imageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val fileName = "review_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("review")
                        bucket.upload(fileName, bytes)
                        finalImageUrl = bucket.publicUrl(fileName)
                    }
                }

                val newReview = Review(
                    destinationId = destinationId,
                    userName = userName,
                    rating = rating,
                    comment = comment,
                    imageUrl = finalImageUrl
                )

                SupabaseClient.client.from("review").insert(newReview)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ulasan terkirim!", Toast.LENGTH_SHORT).show()
                }
                onSuccess()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal kirim", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun downloadReviewImage(context: Context, url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Foto Review")
                .setDescription("Mengunduh foto ulasan...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    "Review_${System.currentTimeMillis()}.jpg"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(context, "Mulai mengunduh...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal download: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}