// Path: app/src/main/java/com/example/pamobilekelompok/viewmodel/HotelViewModel.kt

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
import com.example.pamobilekelompok.model.Hotel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class HotelViewModel : ViewModel() {

    // State untuk List Hotel
    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // State untuk Download Progress
    var downloadProgress by mutableStateOf<String?>(null)

    init {
        getHotels() // Auto load saat ViewModel dibuat
    }

    // ═══════════════════════════════════════════════════════════
    // 📖 READ - Ambil semua data hotel (dengan Realtime Update)
    // ═══════════════════════════════════════════════════════════
    fun getHotels() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Query dengan sorting terbaru
                val result = SupabaseClient.client.from("hotels")
                    .select {
                        order("id", Order.DESCENDING)
                    }.decodeList<Hotel>()

                _hotels.value = result
                Log.d("HotelVM", "Loaded ${result.size} hotels")

            } catch (e: Exception) {
                errorMessage = "Gagal memuat data: ${e.message}"
                Log.e("HotelVM", "Error loading hotels", e)
            } finally {
                isLoading = false
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ➕ CREATE - Upload hotel baru dengan gambar
    // ═══════════════════════════════════════════════════════════
    fun uploadHotel(
        name: String,
        address: String,
        price: String,
        description: String,
        facilities: String,
        imageUri: Uri,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true

                // ✅ TAMBAHKAN CEK SESSION
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                if (session == null) {
                    throw Exception("❌ User belum login! Session tidak ditemukan.")
                }

                Log.d("HotelVM", "User ID: ${session.user?.id}")
                Log.d("HotelVM", "User Email: ${session.user?.email}")

                // 1. BACA FILE GAMBAR dari URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    throw Exception("Gagal membaca file gambar")
                }

                // 2. UPLOAD KE SUPABASE STORAGE
                val fileName = "hotel_${System.currentTimeMillis()}.jpg"
                val bucket = SupabaseClient.client.storage.from("hotels")

                bucket.upload(fileName, bytes)
                Log.d("HotelVM", "Image uploaded: $fileName")

                // 3. DAPATKAN PUBLIC URL
                val publicUrl = bucket.publicUrl(fileName)
                Log.d("HotelVM", "Public URL: $publicUrl")

                // 4. SIMPAN DATA KE DATABASE
                val newHotel = Hotel(
                    name = name,
                    address = address,
                    price = price,
                    description = description,
                    facilities = facilities,
                    imageUrl = publicUrl
                )

                // ✅ TAMBAHKAN TRY-CATCH KHUSUS UNTUK INSERT
                try {
                    SupabaseClient.client.from("hotels").insert(newHotel)
                    Log.d("HotelVM", "✅ Hotel inserted to database")
                } catch (insertError: Exception) {
                    Log.e("HotelVM", "❌ INSERT ERROR: ${insertError.message}")
                    Log.e("HotelVM", "Stack trace: ", insertError)
                    throw Exception("Gagal simpan ke database: ${insertError.message}\n\nKemungkinan: RLS Policy belum diatur dengan benar. Cek Supabase Dashboard!")
                }

                // 5. REFRESH DATA
                getHotels()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Hotel berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                }

                onSuccess()

            } catch (e: Exception) {
                Log.e("HotelVM", "Upload error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "❌ Gagal upload: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ✏️ UPDATE - Edit hotel dengan opsi ganti gambar
    // ═══════════════════════════════════════════════════════════
    fun updateHotel(
        id: Long,
        name: String,
        address: String,
        price: String,
        description: String,
        facilities: String,
        newImageUri: Uri?,
        currentImageUrl: String?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                var finalImageUrl = currentImageUrl

                // 1. JIKA ADA GAMBAR BARU, UPLOAD DULU
                if (newImageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(newImageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val fileName = "hotel_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("hotels")

                        bucket.upload(fileName, bytes)
                        finalImageUrl = bucket.publicUrl(fileName)

                        Log.d("HotelVM", "New image uploaded: $fileName")
                    }
                }

                // 2. UPDATE DATA DI DATABASE
                val updatedHotel = Hotel(
                    id = id,
                    name = name,
                    address = address,
                    price = price,
                    description = description,
                    facilities = facilities,
                    imageUrl = finalImageUrl
                )

                SupabaseClient.client.from("hotels").update(updatedHotel) {
                    filter {
                        eq("id", id)
                    }
                }

                Log.d("HotelVM", "Hotel updated in database")

                // 3. REFRESH DATA
                getHotels()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Data berhasil diupdate!", Toast.LENGTH_SHORT).show()
                }

                onSuccess()

            } catch (e: Exception) {
                Log.e("HotelVM", "Update error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "❌ Gagal update: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 🗑️ DELETE - Hapus hotel dan gambarnya
    // ═══════════════════════════════════════════════════════════
    fun deleteHotel(hotel: Hotel, context: Context) {
        viewModelScope.launch {
            try {
                isLoading = true
                val hotelId = hotel.id ?: throw Exception("ID tidak valid")

                // 1. HAPUS DARI DATABASE
                SupabaseClient.client.from("hotels").delete {
                    filter { eq("id", hotelId) }
                }

                Log.d("HotelVM", "Hotel deleted from database")

                // 2. HAPUS FILE DARI STORAGE (Optional)
                try {
                    val fileName = hotel.imageUrl?.substringAfterLast("/")
                    if (fileName != null) {
                        SupabaseClient.client.storage.from("hotels").delete(fileName)
                        Log.d("HotelVM", "Image deleted from storage")
                    }
                } catch (e: Exception) {
                    Log.e("HotelVM", "Failed to delete image: ${e.message}")
                }

                // 3. REFRESH DATA
                getHotels()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "🗑️ Hotel dihapus", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("HotelVM", "Delete error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "❌ Gagal hapus: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 📥 DOWNLOAD - Download gambar hotel ke storage lokal
    // ═══════════════════════════════════════════════════════════
    fun downloadHotelImage(
        imageUrl: String,
        hotelName: String,
        context: Context,
        onSuccess: (File) -> Unit
    ) {
        viewModelScope.launch {
            try {
                downloadProgress = "Mengunduh..."

                // 1. AMBIL FILE NAME
                val fileName = imageUrl.substringAfterLast("/")
                val bucket = SupabaseClient.client.storage.from("hotels")

                // 2. DOWNLOAD DARI STORAGE
                val imageBytes = bucket.downloadAuthenticated(fileName)
                Log.d("HotelVM", "Downloaded ${imageBytes.size} bytes")

                // 3. SIMPAN KE LOCAL STORAGE
                val downloadsDir = File(context.getExternalFilesDir(null), "HotelBrochures")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val localFileName = "${hotelName.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
                val localFile = File(downloadsDir, localFileName)

                FileOutputStream(localFile).use { outputStream ->
                    outputStream.write(imageBytes)
                }

                Log.d("HotelVM", "File saved: ${localFile.absolutePath}")

                withContext(Dispatchers.Main) {
                    downloadProgress = null
                    Toast.makeText(
                        context,
                        "✅ Download berhasil!\nLokasi: ${localFile.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                    onSuccess(localFile)
                }

            } catch (e: Exception) {
                Log.e("HotelVM", "Download error", e)
                withContext(Dispatchers.Main) {
                    downloadProgress = null
                    Toast.makeText(
                        context,
                        "❌ Download gagal: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}