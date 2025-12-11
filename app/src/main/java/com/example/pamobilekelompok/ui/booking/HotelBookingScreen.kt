// Path: app/src/main/java/com/example/pamobilekelompok/ui/hotels/HotelBookingScreen.kt
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelBookingScreen(
    hotelId: Long,
    hotelName: String,
    pricePerNight: String?,
    onNavigateBack: () -> Unit,
    onConfirmBooking: () -> Unit
) {
    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var guestName by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val checkInPickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            val format = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            checkInDate = format.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.minDate = System.currentTimeMillis() }

    val checkOutPickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            val format = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            checkOutDate = format.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.minDate = System.currentTimeMillis() }

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
                    if (guestName.isNotEmpty() && guestEmail.isNotEmpty() && guestPhone.isNotEmpty() && checkInDate.isNotEmpty() && checkOutDate.isNotEmpty()) {
                        onConfirmBooking()
                    } else {
                        android.widget.Toast.makeText(context, "Semua field wajib diisi!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
            ) {
                Text("Konfirmasi Pembayaran", fontSize = 16.sp) // ✅ TANPA ICON
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize()) {
            Text("Hotel:", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(hotelName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

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
                modifier = Modifier.fillMaxWidth().clickable { checkInPickerDialog.show() },
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
                modifier = Modifier.fillMaxWidth().clickable { checkOutPickerDialog.show() },
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Harga per malam")
                Text(pricePerNight ?: "-")
            }
        }
    }
}