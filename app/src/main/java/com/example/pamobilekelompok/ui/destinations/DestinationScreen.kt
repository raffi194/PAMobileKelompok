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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // State untuk Dialog Konfirmasi Hapus
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
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                }
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (viewModel.isLoading && !showDialog && destinationToDelete == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else if (viewModel.destinations.isEmpty()) {
                Text(
                    text = "Belum ada destinasi.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.destinations) { destination ->
                        DestinationItem(
                            destination = destination,
                            isAdmin = isAdmin, // Kirim status admin ke item
                            onClick = { onNavigateToDetail(destination) },
                            onDeleteClick = { destinationToDelete = destination } // Trigger dialog hapus
                        )
                    }
                }
            }
        }

        // --- DIALOG TAMBAH DATA ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Tambah Destinasi") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedImageUri == null) "Pilih Foto" else "Ganti Foto")
                        }

                        // --- PERBAIKAN: PREVIEW FOTO ---
                        // Menampilkan foto setelah dipilih
                        if (selectedImageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (name.isNotEmpty() && selectedImageUri != null) {
                            viewModel.uploadDestination(name, description, selectedImageUri!!, context) {
                                name = ""; description = ""; selectedImageUri = null; showDialog = false
                            }
                        }
                    }) { Text("Simpan") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
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
    onDeleteClick: () -> Unit
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
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp),
                    maxLines = 1
                )
            }

            // --- TOMBOL DELETE KHUSUS ADMIN ---
            if (isAdmin) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
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