package com.example.pamobilekelompok.ui.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pamobilekelompok.model.Review
import com.example.pamobilekelompok.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationDetailScreen(
    name: String,
    description: String,
    imageUrl: String?,
    onNavigateBack: () -> Unit,
    onNavigateToWriteReview: () -> Unit,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    // State untuk Tab
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Informasi", "Ulasan")

    // Load reviews saat screen dibuka
    LaunchedEffect(name) {
        reviewViewModel.getReviewsByDestination(name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name, maxLines = 1) },
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
                .fillMaxSize()
        ) {
            // Gambar Utama
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            // Tab Row
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Konten Tab
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTabIndex) {
                    0 -> { // TAB INFORMASI
                        Column {
                            Text(
                                text = "Deskripsi",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 24.sp
                            )
                        }
                    }
                    1 -> { // TAB ULASAN (DARI DATABASE)
                        Column(modifier = Modifier.fillMaxSize()) {
                            Button(
                                onClick = onNavigateToWriteReview,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tulis Ulasan Anda")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Loading atau List Review
                            if (reviewViewModel.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else if (reviewViewModel.reviews.isEmpty()) {
                                Text(
                                    text = "Belum ada ulasan. Jadilah yang pertama!",
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = Color.Gray
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(reviewViewModel.reviews) { review ->
                                        ReviewItemCard(review)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Komponen Card untuk Menampilkan Review
@Composable
fun ReviewItemCard(review: Review) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Avatar + Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.userId.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "User ${review.userId.take(8)}", // Tampilkan sebagian ID
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < review.rating) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Komentar
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)

            // Foto Review (Jika Ada)
            review.imageUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Foto Review",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}