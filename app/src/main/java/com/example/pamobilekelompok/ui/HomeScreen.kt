package com.example.pamobilekelompok.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pamobilekelompok.model.Event
import com.example.pamobilekelompok.viewmodel.AuthViewModel
import com.example.pamobilekelompok.viewmodel.EventViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = viewModel(),
    eventViewModel: EventViewModel = viewModel(),
    onNavigateToFeature: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEventDetail: (Event) -> Unit
) {
    // Ambil data user agar nama tampil
    LaunchedEffect(Unit) {
        authViewModel.getCurrentUser()
        eventViewModel.getEvents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Agar bisa discroll vertikal
    ) {
        // --- 1. HEADER PROFILE ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToProfile() }
                .padding(vertical = 16.dp)
        ) {
            // Foto Profil Kiri
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Nama User Kanan
            Column {
                Text(text = "Halo,", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    text = authViewModel.currentUserDisplay ?: "Guest",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. FITUR UTAMA (4 KOLOM - Menggunakan Layout Teman agar ada Komunitas) ---
        Text("Jelajahi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween // Mengatur jarak agar 4 item muat rapi
            ) {
                // Feature 1: Destinasi
                FeatureItem(
                    icon = Icons.Default.Place,
                    label = "Destinasi",
                    onClick = { onNavigateToFeature("destinations") }
                )

                // Feature 2: Kuliner
                FeatureItem(
                    icon = Icons.Default.Star,
                    label = "Kuliner",
                    onClick = { onNavigateToFeature("foods") }
                )

                // Feature 3: Hotel
                FeatureItem(
                    icon = Icons.Default.Home,
                    label = "Hotel",
                    onClick = { onNavigateToFeature("hotels") }
                )

                // Feature 4: Komunitas (Trip)
                FeatureItem(
                    icon = Icons.Default.Person,
                    label = "Komunitas",
                    onClick = { onNavigateToFeature("trips") }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3. EVENT SERU ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Event Seru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { onNavigateToFeature("events") }) {
                Text("Lihat Semua")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- LOGIKA TAMPILAN (Loading vs Kosong vs Ada Data) ---
        if (eventViewModel.isLoading) {
            // KONDISI 1: SEDANG LOADING
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), // Tinggi disesuaikan dengan kartu
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else if (eventViewModel.events.isEmpty()) {
            // KONDISI 2: DATA KOSONG (PERMINTAAN ANDA)
            // Kita buat kotak abu-abu tipis dengan ikon agar tidak terlihat "bug"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada event wisata",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

        } else {
            // KONDISI 3: ADA DATA (Tampilkan List)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(eventViewModel.events) { event ->
                    EventCard(
                        event = event,
                        onClick = { onNavigateToEventDetail(event) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. PESANAN SAYA (Menggunakan Desain Anda yang Lebih Detail) ---
        Text("Pesanan Saya", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // Compact size
                .clickable { onNavigateToFeature("booking_list") }, // Route ke History
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Warna biru muda
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cek Status Pesanan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Lihat detail & bayar tagihan", fontSize = 12.sp, color = Color.Gray)
                }
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Komponen Kecil untuk Menu Utama (Ukuran 56dp agar muat 4 kolom)
@Composable
fun FeatureItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// Komponen Dummy Event Card
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit // Tambahkan parameter onClick
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable { onClick() }, // Pasang clickable di sini
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = event.posterUrl,
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.eventDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}