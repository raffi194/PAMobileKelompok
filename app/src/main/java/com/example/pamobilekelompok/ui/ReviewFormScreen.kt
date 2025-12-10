// PAMobileKelompok/app/src/main/java/com/example/pamobilekelompok/ui/ReviewFormScreen.kt
package com.example.pamobilekelompok.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.pamobilekelompok.viewmodel.ReviewViewModel

@Composable
fun ReviewFormScreen(
    reviewViewModel: ReviewViewModel = viewModel(),
    onReviewSuccess: () -> Unit
) {
    var placeName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Launcher untuk memilih gambar dari galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tambah Ulasan Baru", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Input Nama Tempat
        OutlinedTextField(
            value = placeName,
            onValueChange = { placeName = it },
            label = { Text("Nama Tempat Wisata") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Input Komentar
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Komentar (Min. 10 Karakter)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            singleLine = false
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Rating Star Picker
        Text("Rating: $rating/5", modifier = Modifier.align(Alignment.Start))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(5) { index ->
                val starColor = if (index < rating) Color(0xFFFFC107) else Color.Gray
                IconButton(onClick = { rating = index + 1 }) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Star ${index + 1}",
                        tint = starColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Image Preview & Picker Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Pilih Foto (Opsional)")
            }
            Spacer(modifier = Modifier.width(16.dp))

            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Foto Review",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Tampilkan Error
        reviewViewModel.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Tombol Submit
        Button(
            onClick = {
                if (placeName.isBlank() || comment.length < 10 || rating == 0) {
                    reviewViewModel.errorMessage = "Mohon isi Nama Tempat, Komentar (min 10), dan Rating."
                    return@Button
                }

                reviewViewModel.insertReview(
                    placeName = placeName,
                    comment = comment,
                    rating = rating,
                    imageUri = selectedImageUri,
                    context = context,
                    onReviewSuccess
                )
            },
            enabled = !reviewViewModel.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (reviewViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Kirim Ulasan")
            }
        }
    }
}