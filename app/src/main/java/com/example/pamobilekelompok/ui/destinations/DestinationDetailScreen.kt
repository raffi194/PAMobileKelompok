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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationDetailScreen(
    name: String,
    description: String,
    imageUrl: String?,
    price: Long, // Parameter Harga
    isAdmin: Boolean = false,
    onNavigateBack: () -> Unit,
    onNavigateToWriteReview: () -> Unit,
    onNavigateToBooking: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Informasi", "Ulasan")
    val mockReviews = listOf(
        ReviewMock("Budi", "Tempatnya indah!", 5),
        ReviewMock("Siti", "Akses mudah.", 4)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Kembali") } }
            )
        },
        bottomBar = {
            if (!isAdmin) {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.surface, contentPadding = PaddingValues(16.dp)) {
                    Button(onClick = onNavigateToBooking, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text("Pesan Tiket Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AsyncImage(model = imageUrl, contentDescription = name, modifier = Modifier.fillMaxWidth().height(250.dp), contentScale = ContentScale.Crop)
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title) })
                }
            }
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTabIndex) {
                    0 -> {
                        Column {
                            Text("Deskripsi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)

                            Spacer(modifier = Modifier.height(24.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Info Harga
                            Text("Harga Tiket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = formatRupiah(price),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    1 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (!isAdmin) {
                                Button(onClick = onNavigateToWriteReview, modifier = Modifier.fillMaxWidth()) { Text("Tulis Ulasan Anda") }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { items(mockReviews) { review -> ReviewItem(review) } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: ReviewMock) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Text(review.username.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(review.username, style = MaterialTheme.typography.titleSmall)
                    Row { repeat(5) { index -> Icon(Icons.Default.Star, null, tint = if (index < review.rating) Color(0xFFFFD700) else Color.Gray, modifier = Modifier.size(16.dp)) } }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

data class ReviewMock(val username: String, val comment: String, val rating: Int)

fun formatRupiah(number: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(number).replace("Rp", "Rp ")
}