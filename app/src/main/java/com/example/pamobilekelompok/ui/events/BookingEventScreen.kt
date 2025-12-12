package com.example.pamobilekelompok.ui.events

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamobilekelompok.model.Event
import com.example.pamobilekelompok.viewmodel.EventViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingEventScreen(
    event: Event,
    viewModel: EventViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (Long, Long) -> Unit
) {
    // state awal jumlah tiket
    var ticketCount by remember { mutableIntStateOf(1) }

    // logika hitung harga total otomatis
    val totalPrice = event.price * ticketCount

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Konfirmasi Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            // konfirmasi pemesanan
            Button(
                onClick = {
                    // panggil fungsi booking di ViewModel
                    viewModel.bookEvent(
                        event = event,
                        bookingDate = event.eventDate, // otomatis pakai tanggal event yg terikat dengan eventnya
                        ticketCount = ticketCount,
                        context = context,
                        onSuccess = { bookingId ->
                            // navigasi ke payment dengan membawa data bookingId dan total harga
                            onNavigateToPayment(bookingId, totalPrice)
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                enabled = !viewModel.isLoading // loading akan otomatis membuat tombol nonaktif
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        text = "Bayar Sekarang - ${formatRupiahBooking(totalPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // deskripsi event
            Text(
                text = "Event yang dipilih:",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // deskripsi tanggal event
            Text(text = "Tanggal Kunjungan", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = event.eventDate, // langsung ambil dari database
                onValueChange = {}, // tidak ada perubahan
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                ),
                enabled = false // membuat bagian ini seperti disabled agar user tahu ini fix
            )

            Spacer(modifier = Modifier.height(24.dp))

            // penghitung tiket
            Text(text = "Jumlah Tiket", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // kurang
                FilledIconButton(
                    onClick = { if (ticketCount > 1) ticketCount-- },
                    modifier = Modifier.size(40.dp)
                ) { Icon(Icons.Default.Remove, "Kurang") }

                // angka jumlah
                Text(
                    text = "$ticketCount",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // tambah
                FilledIconButton(
                    onClick = { ticketCount++ },
                    modifier = Modifier.size(40.dp)
                ) { Icon(Icons.Default.Add, "Tambah") }
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            // penjelasan harga
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Harga Satuan")
                Text(formatRupiahBooking(event.price))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Tagihan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = formatRupiahBooking(totalPrice),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

//private fun EventViewModel.bookEvent(
//    event: Event,
//    bookingDate: String,
//    ticketCount: Int,
//    context: Context,
//    onSuccess: () -> Unit
//) {
//}

// buat format ulang Rupiah
fun formatRupiahBooking(number: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(number).replace("Rp", "Rp ")
}