// PAMobileKelompok/app/src/main/java/com/example/pamobilekelompok/ui/ReviewListScreen.kt
package com.example.pamobilekelompok.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.pamobilekelompok.data.model.Review
import com.example.pamobilekelompok.viewmodel.ReviewViewModel

@Composable
fun ReviewListScreen(
    reviewViewModel: ReviewViewModel = viewModel(),
    onNavigateToForm: () -> Unit
) {
    // Memuat daftar review saat layar pertama kali diakses
    remember {
        reviewViewModel.fetchReviews()
        true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Review Wisata") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToForm) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Review")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (reviewViewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (reviewViewModel.errorMessage != null) {
                Text(
                    "Error: ${reviewViewModel.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (reviewViewModel.reviewList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada ulasan. Ayo buat yang pertama!")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reviewViewModel.reviewList) { review ->
                        ReviewCard(review = review)
                    }
                }
            }
        }
    }
}

// Komponen Card untuk menampilkan satu review
@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Judul Tempat
            Text(
                review.placeName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Rating Star
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(review.rating) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Menampilkan email user yang membuat review
                Text("(${review.rating}/5) oleh ${review.userEmail}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Komentar
            Text(review.comment)
            Spacer(modifier = Modifier.height(12.dp))

            // Foto Review (Download & Tampilkan menggunakan Coil)
            review.imageUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = "Foto Review",
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}