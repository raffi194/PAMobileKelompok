// Path: app/src/main/java/com/example/pamobilekelompok/ui/hotels/HotelDetailScreen.kt

package com.example.pamobilekelompok.ui.hotels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pamobilekelompok.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    hotelId: Long?,
    name: String,
    address: String?,
    price: String?,
    description: String?,
    facilities: String?,
    imageUrl: String?,
    onNavigateBack: () -> Unit,
    onNavigateToBooking: (Long, String, String?) -> Unit,
    viewModel: HotelViewModel = viewModel()
) {
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Informasi")


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
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Harga per malam", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(price ?: "Hubungi kami", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = {
                            if (hotelId != null) {
                                onNavigateToBooking(hotelId, name, price)
                            }
                        },
                        modifier = Modifier.width(150.dp)
                    ) {
                        Text("Pesan Sekarang") // ✅ TANPA ICON
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
                        // TAB INFORMASI
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            item {
                                Text(name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }

                            item {
                                address?.let {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(it, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }

                            item { HorizontalDivider() }

                            item {
                                description?.let {
                                    Text("Deskripsi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(it, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                                }
                            }

                            item {
                                facilities?.let {
                                    Text("Fasilitas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val facilityList = it.split(",").map { f -> f.trim() }

                                    facilityList.chunked(2).forEach { rowFacilities ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            rowFacilities.forEach { facility ->
                                                AssistChip(onClick = { }, label = { Text(facility) }, modifier = Modifier.weight(1f))
                                            }
                                            if (rowFacilities.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }

                            item {
                                viewModel.downloadProgress?.let {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(it)
                                        }
                                    }
                                }
                            }

                            item {
                                if (imageUrl != null) {
                                    OutlinedButton(
                                        onClick = { viewModel.downloadHotelImage(imageUrl, name, context) { } },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Download Brosur Hotel") // ✅ TANPA ICON
                                    }
                                }
                            }
                        }
                    }

                    1 -> {

                    }
                }
            }
        }
    }
}
@Composable
fun HotelReviewItem(review: HotelReviewMock) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Text(review.username.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(review.username, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row {
                        repeat(5) { index ->
                            Icon(Icons.Default.Star, null, tint = if (index < review.rating) Color(0xFFFFD700) else Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
data class HotelReviewMock(val username: String, val comment: String, val rating: Int)