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
import com.example.pamobilekelompok.model.LocalFood
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class FoodViewModel : ViewModel() {
    var foodList by mutableStateOf<List<LocalFood>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        getFoods()
    }

    // READ: Ambil semua data makanan
    fun getFoods() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Select * from local_foods order by id desc
                val result = SupabaseClient.client.from("local_foods")
                    .select().decodeList<LocalFood>()
                foodList = result.reversed() // Tampilkan yang terbaru di atas
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // CREATE: Upload Gambar -> Dapat URL -> Insert DB
    fun uploadFood(name: String, description: String, imageUri: Uri, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Baca byte gambar dari URI
                val imageBytes = context.contentResolver.openInputStream(imageUri)?.use {
                    it.readBytes()
                } ?: throw Exception("Gagal membaca gambar")

                // 2. Generate nama file unik (misal: foods/uuid.jpg)
                val fileName = "foods/${UUID.randomUUID()}.jpg"

                // 3. Upload ke Supabase Storage (Bucket: "foods")
                val bucket = SupabaseClient.client.storage.from("foods")
                bucket.upload(fileName, imageBytes)

                // 4. Dapatkan Public URL
                val publicUrl = bucket.publicUrl(fileName)

                // 5. Simpan ke Database Table "local_foods"
                val newFood = LocalFood(
                    name = name,
                    description = description,
                    imageUrl = publicUrl
                )
                SupabaseClient.client.from("local_foods").insert(newFood)

                // 6. Refresh List
                getFoods()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Berhasil upload kuliner!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // FITUR DOWNLOAD
    fun downloadImage(context: Context, url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setTitle("Download $fileName")
            request.setDescription("Mengunduh gambar kuliner...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "$fileName.jpg")
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(context, "Mulai mengunduh...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal download: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}