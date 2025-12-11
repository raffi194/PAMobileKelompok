package com.example.pamobilekelompok.ui.booking

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDestinationScreen(
    destinationName: String,
    ticketPrice: Long,
    onNavigateBack: () -> Unit,
    onConfirmBooking: (String, Int, Long) -> Unit
) {
    var dateText by remember { mutableStateOf("") }
    var ticketCount by remember { mutableIntStateOf(1) }
    val totalPrice = ticketPrice * ticketCount

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            val format = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            dateText = format.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesan Tiket Wisata") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Kembali") } }
            )
        },
        bottomBar = {
            Button(
                onClick = { onConfirmBooking(dateText, ticketCount, totalPrice) },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                enabled = dateText.isNotEmpty() && ticketCount > 0
            ) {
                Text("Konfirmasi Pembayaran", fontSize = 16.sp)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize()) {
            Text(text = "Destinasi:", color = Color.Gray)
            Text(text = destinationName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = dateText,
                onValueChange = { },
                label = { Text("Tanggal Kunjungan") },
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                trailingIcon = { Icon(Icons.Default.DateRange, null) },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Jumlah Tiket", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                FilledIconButton(onClick = { if (ticketCount > 1) ticketCount-- }, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Remove, "Kurang") }
                Text(text = "$ticketCount", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                FilledIconButton(onClick = { ticketCount++ }, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Add, "Tambah") }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Harga per tiket")
                Text(formatRupiah(ticketPrice))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Bayar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = formatRupiah(totalPrice), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

fun formatRupiah(number: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(number).replace("Rp", "Rp ")
}