package com.example.pamobilekelompok.ui.destinations

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pamobilekelompok.model.Destination
import com.example.pamobilekelompok.viewmodel.DestinationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationScreen(
    viewModel: DestinationViewModel = viewModel(),
    isAdmin: Boolean = false,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Destination) -> Unit
) {
    // State Form
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    // State Harga (String agar bisa diedit di TextField)
    var priceInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // State untuk Mode Edit
    var isEditMode by remember { mutableStateOf(false) }
    var currentEditingId by remember { mutableStateOf<Long?>(null) }
    var currentEditingImageUrl by remember { mutableStateOf<String?>(null) }

    // State Konfirmasi Hapus
    var destinationToDelete by remember { mutableStateOf<Destination?>(null) }

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    LaunchedEffect(Unit) {
        viewModel.getDestinations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Destinasi Wisata") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = {
                    // Reset Form untuk Mode Tambah Baru
                    isEditMode = false
                    name = ""
                    description = ""
                    priceInput = "" // Reset harga
                    selectedImageUri = null
                    currentEditingId = null
                    currentEditingImageUrl = null
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                }
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (viewModel.isLoading && !showDialog && destinationToDelete == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.destinations.isEmpty()) {
                Text(
                    text = "Belum ada destinasi.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.destinations) { destination ->
                        DestinationItem(
                            destination = destination,
                            isAdmin = isAdmin,
                            onClick = { onNavigateToDetail(destination) },
                            onDeleteClick = { destinationToDelete = destination },
                            onEditClick = {
                                // --- ISI DATA KE FORM UNTUK EDIT ---
                                isEditMode = true
                                currentEditingId = destination.id
                                name = destination.name
                                description = destination.description ?: ""
                                // Load harga lama ke input
                                priceInput = destination.price?.toString() ?: ""
                                currentEditingImageUrl = destination.imageUrl
                                selectedImageUri = null // Reset gambar baru
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        // --- DIALOG INPUT (BISA TAMBAH / EDIT) ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (isEditMode) "Edit Destinasi" else "Tambah Destinasi") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Input Harga
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            label = { Text("Harga Tiket (Rp)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Deskripsi") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedImageUri == null) "Pilih Foto Baru" else "Ganti Foto")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // LOGIKA PREVIEW FOTO
                        if (selectedImageUri != null) {
                            // 1. Tampilkan Foto Baru yg dipilih dari HP
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Preview Baru",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else if (isEditMode && currentEditingImageUrl != null) {
                            // 2. Tampilkan Foto Lama (Jika user belum pilih foto baru)
                            AsyncImage(
                                model = currentEditingImageUrl,
                                contentDescription = "Foto Lama",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text("Foto Saat Ini", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // Konversi Harga
                        val priceLong = priceInput.toLongOrNull() ?: 0L

                        if (name.isNotEmpty()) {
                            if (isEditMode) {
                                // --- MODE UPDATE ---
                                if (currentEditingId != null) {
                                    viewModel.updateDestination(
                                        id = currentEditingId!!,
                                        name = name,
                                        description = description,
                                        price = priceLong, // Kirim Harga Baru
                                        newImageUri = selectedImageUri, // Bisa null
                                        currentImageUrl = currentEditingImageUrl,
                                        context = context
                                    ) {
                                        showDialog = false
                                    }
                                }
                            } else {
                                // --- MODE CREATE ---
                                if (selectedImageUri != null) {
                                    viewModel.uploadDestination(
                                        name,
                                        description,
                                        priceLong, // Kirim Harga
                                        selectedImageUri!!,
                                        context
                                    ) {
                                        showDialog = false
                                    }
                                } else {
                                    Toast.makeText(context, "Foto wajib dipilih!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Nama wajib diisi!", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Simpan") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Batal") }
                }
            )
        }

        // --- DIALOG KONFIRMASI HAPUS ---
        if (destinationToDelete != null) {
            AlertDialog(
                onDismissRequest = { destinationToDelete = null },
                title = { Text("Hapus Destinasi") },
                text = { Text("Apakah Anda yakin ingin menghapus '${destinationToDelete?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteDestination(destinationToDelete!!, context)
                            destinationToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { destinationToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun DestinationItem(
    destination: Destination,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit // Callback Edit Baru
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box {
            Column {
                AsyncImage(
                    model = destination.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp),
                    maxLines = 1
                )
            }

            // --- TOMBOL EDIT & DELETE (KHUSUS ADMIN) ---
            if (isAdmin) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    // Tombol Edit (Kuning/Biru)
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFFFFA000) // Warna Orange
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Tombol Delete (Merah)
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