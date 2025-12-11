package com.example.pamobilekelompok.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamobilekelompok.viewmodel.AuthViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToFeature: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    // Ambil data user agar nama tampil
    LaunchedEffect(Unit) {
        authViewModel.getCurrentUser()
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

        // --- 2. FITUR UTAMA (4 KOLOM) ---
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

                // Feature 3: Penginapan
                FeatureItem(
                    icon = Icons.Default.Home,
                    label = "Hotel",
                    onClick = { onNavigateToFeature("hotels") }
                )

                // Feature 4: Komunitas (BARU)
                FeatureItem(
                    icon = Icons.Default.Person, // Menggunakan icon Person untuk komunitas
                    label = "Komunitas",
                    onClick = { onNavigateToFeature("trips") }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3. EVENT (HORIZONTAL SCROLL - TETAP ADA) ---
        Text("Event Seru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(5) { // Contoh 5 Event Dummy
                EventCard()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. TRIP DOCS / GALERI PESANAN ---
        Text("Pesanan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable { onNavigateToFeature("booking_list") }, // Ubah route jika perlu
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Lihat Riwayat Pesanan", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Komponen Kecil untuk Menu Utama (Perlu penyesuaian ukuran font jika 4 kolom terlalu sempit)
@Composable
fun FeatureItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(56.dp) // Sedikit diperkecil agar muat 4 (tadinya 60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall, // Gunakan bodySmall agar teks muat
            fontWeight = FontWeight.Medium
        )
    }
}

// Komponen Dummy Event Card
@Composable
fun EventCard() {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Event")
        }
    }
}