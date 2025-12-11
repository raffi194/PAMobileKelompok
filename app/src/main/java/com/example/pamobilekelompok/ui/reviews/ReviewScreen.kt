package com.example.pamobilekelompok.ui.reviews

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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
import coil.compose.AsyncImage
import com.example.pamobilekelompok.viewmodel.AuthViewModel
import com.example.pamobilekelompok.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    destinationId: Long,
    destinationName: String,
    viewModel: ReviewViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) } // Default 5 bintang
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val currentUser = authViewModel.currentUserEmail

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beri Ulasan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Header Info
            Text("Tulis ulasan untuk:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = destinationName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- INPUT RATING (BINTANG) ---
            Text("Berikan Rating:", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                repeat(5) { index ->
                    val starIndex = index + 1
                    Icon(
                        imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Bintang $starIndex",
                        tint = if (starIndex <= rating) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { rating = starIndex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- INPUT KOMENTAR ---
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Bagaimana pengalaman Anda?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- INPUT FOTO ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedImageUri == null) "Tambah Foto" else "Ganti Foto")
                }
            }

            // Preview Foto
            if (selectedImageUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Preview Foto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- TOMBOL KIRIM ---
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (currentUser != null && reviewText.isNotEmpty()) {
                            viewModel.submitReview(
                                destinationId = destinationId,
                                userName = currentUser?.substringBefore("@") ?: "Pengunjung",
                                rating = rating,
                                comment = reviewText,
                                imageUri = selectedImageUri,
                                context = context,
                                onSuccess = onNavigateBack
                            )
                        } else if (currentUser == null) {
                            Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Mohon isi komentar ulasan", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Kirim Ulasan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}