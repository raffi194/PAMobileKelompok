package com.example.pamobilekelompok.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamobilekelompok.model.EventBooking
import com.example.pamobilekelompok.viewmodel.Booking
import com.example.pamobilekelompok.viewmodel.BookingViewModel
import com.example.pamobilekelompok.viewmodel.EventViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    bookingViewModel: BookingViewModel = viewModel(),
    eventViewModel: EventViewModel = viewModel(), // Inject Event ViewModel
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Booking) -> Unit,
    // Callback opsional jika ingin detail event (sementara bisa dikosongkan)
    onNavigateToEventDetail: (EventBooking) -> Unit = {}
) {
    // State Tab (0 = Wisata, 1 = Event)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Wisata", "Event")

    // Ambil data saat layar dibuka
    LaunchedEffect(Unit) {
        bookingViewModel.getUserBookings()
        eventViewModel.getUserEventBookings() // Panggil fungsi baru tadi
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            // --- TAB ROW ---
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // --- ISI KONTEN ---
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (selectedTab == 0) {
                    // === LIST DESTINASI ===
                    if (bookingViewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (bookingViewModel.bookingList.isEmpty()) {
                        Text("Belum ada pesanan wisata.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(bookingViewModel.bookingList) { booking ->
                                BookingItem(
                                    title = booking.destination_name,
                                    date = booking.visit_date,
                                    count = booking.ticket_count,
                                    total = booking.total_price,
                                    status = booking.status,
                                    onClick = { onNavigateToDetail(booking) }
                                )
                            }
                        }
                    }
                } else {
                    // === LIST EVENT (BARU) ===
                    if (eventViewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (eventViewModel.eventBookings.isEmpty()) {
                        Text("Belum ada pesanan event.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(eventViewModel.eventBookings) { eventBooking ->
                                BookingItem(
                                    title = eventBooking.eventTitle,
                                    date = eventBooking.bookingDate,
                                    count = eventBooking.ticketCount,
                                    total = eventBooking.totalPrice,
                                    status = eventBooking.status, // "Menunggu Pembayaran" akan muncul di sini
                                    onClick = { onNavigateToEventDetail(eventBooking) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Komponen Card yang Disederhanakan (Bisa dipakai Destination & Event)
@Composable
fun BookingItem(
    title: String,
    date: String,
    count: Int,
    total: Long,
    status: String,
    onClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                StatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tanggal:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(date, style = MaterialTheme.typography.bodyMedium)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tiket:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("$count x", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total: ${formatRupiah2(total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    // Logika warna: Lunas (Hijau), Menunggu Pembayaran (Orange/Kuning)
    val bgColor = when(status) {
        "Lunas" -> Color(0xFFE8F5E9)
        "Menunggu Pembayaran" -> Color(0xFFFFF3E0)
        else -> Color.LightGray
    }
    val textColor = when(status) {
        "Lunas" -> Color(0xFF2E7D32)
        "Menunggu Pembayaran" -> Color(0xFFEF6C00)
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = status, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

fun formatRupiah2(number: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(number).replace("Rp", "Rp ")
}