package com.example.pamobilekelompok.ui.booking

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamobilekelompok.viewmodel.BookingViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelBookingScreen(
    hotelId: Long,
    hotelName: String,
    pricePerNight: String?,
    onNavigateBack: () -> Unit,
    onConfirmBooking: (Long, Long) -> Unit, // (bookingId, totalPrice)
    viewModel: BookingViewModel = viewModel()
) {
    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var checkInDateMs by remember { mutableLongStateOf(0L) }
    var checkOutDateMs by remember { mutableLongStateOf(0L) }

    var guestName by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Ekstrak harga per malam
    val pricePerNightLong = remember(pricePerNight) {
        pricePerNight?.replace(Regex("[^0-9]"), "")?.toLongOrNull() ?: 0L
    }

    // Hitung jumlah malam
    val totalNights = remember(checkInDateMs, checkOutDateMs) {
        if (checkInDateMs > 0 && checkOutDateMs > checkInDateMs) {
            val diff = checkOutDateMs - checkInDateMs
            TimeUnit.MILLISECONDS.toDays(diff).toInt()
        } else {
            0
        }
    }

    // Hitung total harga
    val totalPrice = pricePerNightLong * totalNights

    // Date Picker untuk Check-In
    val checkInPickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            val format = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            checkInDate = format.format(calendar.time)
            checkInDateMs = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }

    // Date Picker untuk Check-Out
    val checkOutPickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            val format = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            checkOutDate = format.format(calendar.time)
            checkOutDateMs = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        // Set minDate berdasarkan Check-In jika sudah dipilih
        if (checkInDateMs > 0) {
            datePicker.minDate = checkInDateMs + (24 * 60 * 60 * 1000) // Minimal 1 hari setelah check-in
        } else {
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesan Hotel") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (guestName.isNotEmpty() &&
                        guestEmail.isNotEmpty() &&
                        guestPhone.isNotEmpty() &&
                        checkInDate.isNotEmpty() &&
                        checkOutDate.isNotEmpty() &&
                        totalNights > 0) {

                        // Format tanggal untuk database (yyyy-MM-dd)
                        val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val checkInDb = dbFormat.format(Date(checkInDateMs))
                        val checkOutDb = dbFormat.format(Date(checkOutDateMs))

                        viewModel.createHotelBooking(
                            hotelName = hotelName,
                            guestName = guestName,
                            guestEmail = guestEmail,
                            guestPhone = guestPhone,
                            checkInDate = checkInDb,
                            checkOutDate = checkOutDb,
                            totalNights = totalNights,
                            pricePerNight = pricePerNightLong,
                            totalPrice = totalPrice,
                            context = context,
                            onSuccess = { bookingId ->
                                onConfirmBooking(bookingId, totalPrice)
                            }
                        )
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "Semua field wajib diisi!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                enabled = !viewModel.isLoading && totalNights > 0
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Konfirmasi Pembayaran", fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Hotel:", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                hotelName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = guestName,
                onValueChange = { guestName = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = guestEmail,
                onValueChange = { guestEmail = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = guestPhone,
                onValueChange = { guestPhone = it },
                label = { Text("No. Telepon") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = checkInDate,
                onValueChange = {},
                label = { Text("Tanggal Check-In") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { checkInPickerDialog.show() },
                trailingIcon = { Icon(Icons.Default.DateRange, null) },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = checkOutDate,
                onValueChange = {},
                label = { Text("Tanggal Check-Out") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { checkOutPickerDialog.show() },
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
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Harga per malam")
                Text(pricePerNight ?: "-", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Jumlah malam")
                Text("$totalNights malam", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Bayar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Rp ${String.format("%,d", totalPrice).replace(',', '.')}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}