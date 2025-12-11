// Path: app/src/main/java/com/example/pamobilekelompok/ui/hotels/HotelScreen.kt

package com.example.pamobilekelompok.ui.hotels

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pamobilekelompok.model.Hotel
import com.example.pamobilekelompok.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelScreen(
    viewModel: HotelViewModel = viewModel(),
    isAdmin: Boolean = false,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Hotel) -> Unit
) {
    // ═══════ STATE MANAGEMENT ═══════
    // Form States
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var facilities by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Edit Mode States
    var isEditMode by remember { mutableStateOf(false) }
    var currentEditingId by remember { mutableStateOf<Long?>(null) }
    var currentEditingImageUrl by remember { mutableStateOf<String?>(null) }

    // Delete Confirmation
    var hotelToDelete by remember { mutableStateOf<Hotel?>(null) }

    val context = LocalContext.current

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // Collect StateFlow dengan collectAsState
    val hotels by viewModel.hotels.collectAsState()

    // ═══════ UI LAYOUT ═══════
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏨 Hotel/Homestay") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        // Reset untuk mode CREATE
                        isEditMode = false
                        name = ""
                        address = ""
                        price = ""
                        description = ""
                        facilities = ""
                        selectedImageUri = null
                        currentEditingId = null
                        currentEditingImageUrl = null
                        showDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Hotel")
                }
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // ═══════ LOADING STATE ═══════
            if (viewModel.isLoading && !showDialog && hotelToDelete == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // ═══════ EMPTY STATE ═══════
            else if (hotels.isEmpty() && !viewModel.isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏨",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum ada data hotel/homestay",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // ═══════ GRID HOTEL LIST ═══════
            else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(hotels) { hotel ->
                        HotelItem(
                            hotel = hotel,
                            isAdmin = isAdmin,
                            onClick = { onNavigateToDetail(hotel) },
                            onDeleteClick = { hotelToDelete = hotel },
                            onEditClick = {
                                // Isi form untuk EDIT
                                isEditMode = true
                                currentEditingId = hotel.id
                                name = hotel.name
                                address = hotel.address ?: ""
                                price = hotel.price ?: ""
                                description = hotel.description ?: ""
                                facilities = hotel.facilities ?: ""
                                currentEditingImageUrl = hotel.imageUrl
                                selectedImageUri = null
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 📝 DIALOG FORM (CREATE / UPDATE)
        // ═══════════════════════════════════════════════════════════
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(if (isEditMode) "✏️ Edit Hotel" else "➕ Tambah Hotel")
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Input Nama Hotel
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Hotel *") },
                            placeholder = { Text("Hotel Mewah Paradise") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Input Alamat
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Alamat") },
                            placeholder = { Text("Jl. Raya Kuta No. 123") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Input Harga
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Harga") },
                            placeholder = { Text("Rp 500.000/malam") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Input Deskripsi
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Deskripsi") },
                            placeholder = { Text("Hotel bintang 5 dengan pemandangan laut...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Input Fasilitas
                        OutlinedTextField(
                            value = facilities,
                            onValueChange = { facilities = it },
                            label = { Text("Fasilitas") },
                            placeholder = { Text("WiFi, AC, TV, Kolam Renang, Sarapan") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tombol Pilih Foto
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (selectedImageUri == null) "📷 Pilih Foto"
                                else "✅ Ganti Foto"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Preview Gambar
                        if (selectedImageUri != null) {
                            // Gambar baru yang dipilih
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Preview Baru",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                "✨ Foto Baru",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (isEditMode && currentEditingImageUrl != null) {
                            // Gambar lama (mode edit)
                            AsyncImage(
                                model = currentEditingImageUrl,
                                contentDescription = "Foto Saat Ini",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                "📸 Foto Saat Ini",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                if (isEditMode) {
                                    // ✏️ MODE UPDATE
                                    if (currentEditingId != null) {
                                        viewModel.updateHotel(
                                            id = currentEditingId!!,
                                            name = name,
                                            address = address,
                                            price = price,
                                            description = description,
                                            facilities = facilities,
                                            newImageUri = selectedImageUri,
                                            currentImageUrl = currentEditingImageUrl,
                                            context = context
                                        ) {
                                            showDialog = false
                                        }
                                    }
                                } else {
                                    // ➕ MODE CREATE
                                    if (selectedImageUri != null) {
                                        viewModel.uploadHotel(
                                            name = name,
                                            address = address,
                                            price = price,
                                            description = description,
                                            facilities = facilities,
                                            imageUri = selectedImageUri!!,
                                            context = context
                                        ) {
                                            showDialog = false
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "⚠️ Foto wajib dipilih!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "⚠️ Nama hotel wajib diisi!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Text("💾 Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("❌ Batal")
                    }
                }
            )
        }

        // ═══════════════════════════════════════════════════════════
        // 🗑️ DIALOG KONFIRMASI HAPUS
        // ═══════════════════════════════════════════════════════════
        if (hotelToDelete != null) {
            AlertDialog(
                onDismissRequest = { hotelToDelete = null },
                title = { Text("🗑️ Hapus Hotel?") },
                text = {
                    Text("Apakah Anda yakin ingin menghapus '${hotelToDelete?.name}'?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteHotel(hotelToDelete!!, context)
                            hotelToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { hotelToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 🏨 HOTEL ITEM COMPONENT
// ═══════════════════════════════════════════════════════════
@Composable
fun HotelItem(
    hotel: Hotel,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box {
            Column {
                // Gambar Hotel
                AsyncImage(
                    model = hotel.imageUrl,
                    contentDescription = hotel.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )

                // Info Hotel
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = hotel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    hotel.price?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Tombol Admin (Edit & Delete)
            if (isAdmin) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    // Edit Button
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFFFFA000)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete Button
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}