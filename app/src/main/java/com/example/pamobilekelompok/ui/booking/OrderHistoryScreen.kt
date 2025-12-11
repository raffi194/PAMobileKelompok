package com.example.pamobilekelompok.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Import ini penting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamobilekelompok.viewmodel.Booking
import com.example.pamobilekelompok.viewmodel.BookingViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    viewModel: BookingViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Booking) -> Unit // Callback baru: Buka Detail
) {
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
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (viewModel.bookingList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat pesanan.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.bookingList) { booking ->
                    BookingItem(
                        booking = booking,
                        onClick = { onNavigateToDetail(booking) } // Trigger klik
                    )
                }
            }
        }
    }
}

@Composable
fun BookingItem(booking: Booking, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable { onClick() } // Klik card buka detail
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.destination_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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