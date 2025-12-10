package com.example.pamobilekelompok.ui.reviews

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pamobilekelompok.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    destinationName: String,
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = viewModel()
) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Photo Picker Launcher
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Tulis ulasan untuk:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = destinationName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Rating Bintang
            Text("Rating:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { index ->
                    IconButton(onClick = { rating = index + 1 }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating ${index + 1}",
                            tint = if (index < rating) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Komentar
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Pendapat Anda") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Pilih Foto (Opsional)
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedImageUri == null) "Tambah Foto (Opsional)" else "Ganti Foto")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preview Foto
            selectedImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Preview Foto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Kirim
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (reviewText.isNotEmpty()) {
                            viewModel.addReview(
                                destinationName = destinationName,
                                rating = rating,
                                comment = reviewText,
                                imageUri = selectedImageUri,
                                context = context,
                                onSuccess = { onNavigateBack() }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kirim Ulasan")
                }
            }
        }
    }
}