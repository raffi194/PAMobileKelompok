// Path: app/src/main/java/com/example/pamobilekelompok/ui/hotels/HotelDetailScreen.kt

package com.example.pamobilekelompok.ui.hotels

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pamobilekelompok.ui.theme.PAMobileKelompokTheme
import com.example.pamobilekelompok.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    name: String,
    address: String?,
    price: String?,
    description: String?,
    facilities: String?,
    imageUrl: String?,
    onNavigateBack: () -> Unit,
    viewModel: HotelViewModel = viewModel()
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // ═══════ TOMBOL DOWNLOAD (ICON DIPERBAIKI) ═══════
                    if (imageUrl != null) {
                        IconButton(
                            onClick = {
                                viewModel.downloadHotelImage(
                                    imageUrl = imageUrl,
                                    hotelName = name,
                                    context = context
                                ) { file ->
                                    // Callback setelah download sukses
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward, // ✅ GANTI DENGAN INI
                                contentDescription = "Download Brosur",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ═══════ GAMBAR UTAMA ═══════
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            // ═══════ KONTEN DETAIL ═══════
            Column(modifier = Modifier.padding(16.dp)) {

                // Nama Hotel
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Harga
                price?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Alamat
                address?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Divider
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Deskripsi
                description?.let {
                    Text(
                        text = "📝 Deskripsi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fasilitas
                facilities?.let {
                    Text(
                        text = "✨ Fasilitas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Split facilities by comma dan tampilkan dalam chips
                    val facilityList = it.split(",").map { f -> f.trim() }

                    facilityList.chunked(2).forEach { rowFacilities ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowFacilities.forEach { facility ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(facility) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if odd number
                            if (rowFacilities.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Download Progress Indicator
                viewModel.downloadProgress?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(it)
                        }
                    }
                }
            }
        }
    }
}