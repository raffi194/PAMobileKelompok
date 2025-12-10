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
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    var isEditMode by remember { mutableStateOf(false) }
    var currentEditingId by remember { mutableStateOf<Long?>(null) }
    var currentEditingImageUrl by remember { mutableStateOf<String?>(null) }
    var destinationToDelete by remember { mutableStateOf<Destination?>(null) }

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    LaunchedEffect(Unit) { viewModel.getDestinations() }

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
                    isEditMode = false
                    name = ""; description = ""; priceInput = ""
                    selectedImageUri = null
                    currentEditingId = null; currentEditingImageUrl = null
                    showDialog = true
                }) { Icon(Icons.Default.Add, "Tambah") }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (viewModel.isLoading && !showDialog && destinationToDelete == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.destinations.isEmpty()) {
                Text("Belum ada destinasi.", modifier = Modifier.align(Alignment.Center))
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
                                isEditMode = true
                                currentEditingId = destination.id
                                name = destination.name
                                description = destination.description ?: ""
                                priceInput = destination.price?.toString() ?: ""
                                currentEditingImageUrl = destination.imageUrl
                                selectedImageUri = null
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { if (!viewModel.isLoading) showDialog = false },
                title = { Text(if (isEditMode) "Edit Destinasi" else "Tambah Destinasi") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !viewModel.isLoading
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            label = { Text("Harga Tiket (Rp)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !viewModel.isLoading
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Deskripsi") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !viewModel.isLoading
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !viewModel.isLoading
                        ) {
                            Text(if (selectedImageUri == null) "Pilih Foto" else "Ganti Foto")
                        }
                        if (selectedImageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(model = selectedImageUri, contentDescription = "Preview", modifier = Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Crop)
                        } else if (isEditMode && currentEditingImageUrl != null) {
                            AsyncImage(model = currentEditingImageUrl, contentDescription = "Foto Lama", modifier = Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Crop)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val priceLong = priceInput.toLongOrNull() ?: 0L

                            // VALIDASI
                            if (name.isBlank() || description.isBlank() || priceLong <= 0) {
                                Toast.makeText(context, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (isEditMode) {
                                if (currentEditingId != null) {
                                    viewModel.updateDestination(
                                        id = currentEditingId!!,
                                        name = name,
                                        description = description,
                                        price = priceLong,
                                        newImageUri = selectedImageUri,
                                        currentImageUrl = currentEditingImageUrl,
                                        context = context
                                    ) { showDialog = false }
                                }
                            } else {
                                if (selectedImageUri != null) {
                                    viewModel.uploadDestination(
                                        name, description, priceLong, selectedImageUri!!, context
                                    ) { showDialog = false }
                                } else {
                                    Toast.makeText(context, "Foto wajib dipilih!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Simpan")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false },
                        enabled = !viewModel.isLoading
                    ) { Text("Batal") }
                }
            )
        }

        if (destinationToDelete != null) {
            AlertDialog(
                onDismissRequest = { if(!viewModel.isLoading) destinationToDelete = null },
                title = { Text("Hapus Destinasi") },
                text = { Text("Hapus '${destinationToDelete?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteDestination(destinationToDelete!!, context)
                            destinationToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = !viewModel.isLoading
                    ) { Text("Hapus") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { destinationToDelete = null },
                        enabled = !viewModel.isLoading
                    ) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
fun DestinationItem(destination: Destination, isAdmin: Boolean, onClick: () -> Unit, onDeleteClick: () -> Unit, onEditClick: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.clickable { onClick() }) {
        Box {
            Column {
                AsyncImage(model = destination.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp), contentScale = ContentScale.Crop)
                Text(text = destination.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp), maxLines = 1)
            }
            if (isAdmin) {
                Row(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFFFA000)) }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, "Hapus", tint = Color.Red) }
                }
            }
        }
    }
}