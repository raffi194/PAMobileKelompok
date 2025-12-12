package com.example.pamobilekelompok.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Place
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
import com.example.pamobilekelompok.model.HotelBooking
import com.example.pamobilekelompok.viewmodel.Booking
import com.example.pamobilekelompok.viewmodel.BookingItem
import com.example.pamobilekelompok.viewmodel.BookingViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    viewModel: BookingViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Booking) -> Unit,
    onNavigateToHotelDetail: (HotelBooking) -> Unit = {}
) {
    // State untuk Tab yang dipilih
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Wisata", "Hotel", "Event")

    LaunchedEffect(Unit) {
        viewModel.getUserBookings()
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            //TAB ROW (MENU ATAS)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Place, null)
                                1 -> Icon(Icons.Default.Hotel, null)
                                2 -> Icon(Icons.Default.Event, null)
                            }
                        }
                    )
                }
            }

            //KONTEN LIST
            Box(modifier = Modifier.fillMaxSize()) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    // Filter Data Berdasarkan Tab yang Dipilih
                    val filteredList = when (selectedTabIndex) {
                        0 -> viewModel.allBookings.filterIsInstance<BookingItem.DestinationBookingItem>()
                        1 -> viewModel.allBookings.filterIsInstance<BookingItem.HotelBookingItem>()
                        2 -> viewModel.allBookings.filterIsInstance<BookingItem.EventBookingItem>()
                        else -> emptyList()
                    }

                    if (filteredList.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📭", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum ada riwayat ${tabs[selectedTabIndex]}",
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredList) { bookingItem ->
                                when (bookingItem) {
                                    is BookingItem.DestinationBookingItem -> {
                                        DestinationBookingCard(
                                            booking = bookingItem.booking,
                                            onClick = { onNavigateToDetail(bookingItem.booking) }
                                        )
                                    }
                                    is BookingItem.HotelBookingItem -> {
                                        HotelBookingCard(
                                            booking = bookingItem.booking,
                                            onClick = { onNavigateToHotelDetail(bookingItem.booking) }
                                        )
                                    }
                                    is BookingItem.EventBookingItem -> {
                                        EventBookingCard(
                                            booking = bookingItem.booking
                                        )
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
//KOMPONEN KARTU PESANAN
@Composable
fun DestinationBookingCard(booking: Booking, onClick: () -> Unit) {
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
                Column {
                    Text("Destinasi Wisata", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(
                        text = booking.destination_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tanggal:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(booking.visit_date, style = MaterialTheme.typography.bodyMedium)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tiket:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("${booking.ticket_count}x", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total: ${formatRupiah2(booking.total_price)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun HotelBookingCard(booking: HotelBooking, onClick: () -> Unit) {
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
                Column {
                    Text("Hotel / Homestay", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(
                        text = booking.hotelName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Check-In:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(booking.checkInDate, style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Durasi:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("${booking.totalNights} malam", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total: ${formatRupiah2(booking.totalPrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun EventBookingCard(booking: EventBooking) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tiket Event", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(
                        text = booking.eventTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tanggal:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(booking.bookingDate, style = MaterialTheme.typography.bodyMedium)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tiket:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("${booking.ticketCount}x", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total: ${formatRupiah2(booking.totalPrice)}",
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
    val bgColor = if (status == "Lunas") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
    val textColor = if (status == "Lunas") Color(0xFF2E7D32) else Color(0xFFEF6C00)

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