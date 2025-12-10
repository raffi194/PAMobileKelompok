package com.example.pamobilekelompok.ui.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationDetailScreen(
    name: String,
    description: String,
    imageUrl: String?,
    onNavigateBack: () -> Unit,
    onNavigateToWriteReview: () -> Unit // Callback ke halaman tulis ulasan
) {
    // State untuk Tab (0 = Informasi, 1 = Ulasan)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Informasi", "Ulasan")

    // Mock Data Ulasan (Hanya UI, tidak masuk database)
    val mockReviews = listOf(
        ReviewMock("Budi Santoso", "Tempatnya sangat indah dan sejuk!", 5),
        ReviewMock("Siti Aminah", "Akses jalan lumayan mudah, tapi parkir penuh.", 4),
        ReviewMock("Rudi Hartono", "Makanan di sekitar sini enak-enak.", 5),
        ReviewMock("Dewi Lestari", "Cocok untuk liburan keluarga.", 5),
        ReviewMock("Andi Saputra", "Pemandangan bagus, tapi toilet kurang bersih.", 3)
    )

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
            // 1. Gambar Utama
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            // 2. Tab Row (Menu)
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // 3. Konten Tab
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
                    1 -> { // TAB ULASAN
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Tombol Beri Ulasan
                            Button(
                                onClick = onNavigateToWriteReview,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tulis Ulasan Anda")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // List Ulasan Orang Lain
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(mockReviews) { review ->
                                    ReviewItem(review)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Komponen Item Ulasan
@Composable
fun ReviewItem(review: ReviewMock) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar Bulat (Inisial Nama)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.username.first().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = review.username, style = MaterialTheme.typography.titleSmall)
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
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// Data Class Sederhana untuk Mockup
data class ReviewMock(val username: String, val comment: String, val rating: Int)