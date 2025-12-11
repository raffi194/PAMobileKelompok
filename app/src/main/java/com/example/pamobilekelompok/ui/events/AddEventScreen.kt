package com.example.pamobilekelompok.ui.events

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pamobilekelompok.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    viewModel: EventViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    // Parameter Opsional untuk Edit Mode
    eventId: Long? = null,
    initialTitle: String = "",
    initialDesc: String = "",
    initialDate: String = "",
    initialPrice: String = "", // <--- Parameter Awal Harga (String agar mudah di input)
    initialImageUrl: String? = null
) {
    val isEditMode = eventId != null && eventId > 0

    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var description by rememberSaveable { mutableStateOf(initialDesc) }

    // State Harga
    var price by rememberSaveable { mutableStateOf(initialPrice) }

    var eventDateDisplay by rememberSaveable { mutableStateOf(initialDate) }
    var eventDateDb by rememberSaveable { mutableStateOf(initialDate) }

    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            val formatDisplay = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            eventDateDisplay = formatDisplay.format(calendar.time)

            val formatDb = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            eventDateDb = formatDb.format(calendar.time)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Event" else "Upload Event Baru") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- AREA GAMBAR ---
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (initialImageUrl != null) {
                    AsyncImage(model = initialImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    OutlinedButton(onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Text("Pilih Poster")
                    }
                }
            }
            if (imageUri != null || initialImageUrl != null) {
                TextButton(onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("Ganti Gambar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- FORM INPUT ---
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Judul Event") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // --- INPUT HARGA (BARU) ---
            OutlinedTextField(
                value = price,
                onValueChange = {
                    // Hanya izinkan angka
                    if (it.all { char -> char.isDigit() }) {
                        price = it
                    }
                },
                label = { Text("Harga Tiket (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("0") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = eventDateDisplay, onValueChange = { },
                label = { Text("Tanggal Event") },
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                enabled = false,
                trailingIcon = { Icon(Icons.Default.DateRange, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- TOMBOL AKSI ---
            Button(
                onClick = {
                    // Validasi input
                    if (title.isNotBlank() && description.isNotBlank() && eventDateDb.isNotBlank() && price.isNotBlank()) {

                        val priceLong = price.toLongOrNull() ?: 0L

                        if (isEditMode) {
                            // UPDATE
                            viewModel.updateEvent(
                                id = eventId!!,
                                title = title,
                                desc = description,
                                date = eventDateDb,
                                price = priceLong, // <--- Kirim Harga
                                newImageUri = imageUri,
                                currentImageUrl = initialImageUrl,
                                context = context,
                                onSuccess = onNavigateBack
                            )
                        } else {
                            // INSERT BARU
                            if (imageUri == null) {
                                Toast.makeText(context, "Pilih gambar dulu!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.uploadEvent(
                                    title,
                                    description,
                                    eventDateDb,
                                    priceLong, // <--- Kirim Harga
                                    imageUri,
                                    context,
                                    onNavigateBack
                                )
                            }
                        }
                    } else {
                        Toast.makeText(context, "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isEditMode) "Simpan Perubahan" else "Upload Event")
                }
            }
        }
    }
}