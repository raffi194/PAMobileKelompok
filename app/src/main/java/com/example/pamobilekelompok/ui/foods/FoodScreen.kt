package com.example.pamobilekelompok.ui.foods

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pamobilekelompok.model.LocalFood
import com.example.pamobilekelompok.viewmodel.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(
    viewModel: FoodViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kuliner Lokal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Food")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (viewModel.isLoading && viewModel.foodList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.foodList) { food ->
                        FoodItem(food = food, onDownload = {
                            viewModel.downloadImage(context, food.imageUrl, food.name)
                        })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFoodDialog(
            onDismiss = { showAddDialog = false },
            onUpload = { name, desc, uri ->
                viewModel.uploadFood(name, desc, uri, context) {
                    showAddDialog = false
                }
            },
            isLoading = viewModel.isLoading
        )
    }
}

@Composable
fun FoodItem(food: LocalFood, onDownload: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Gambar Makanan
            AsyncImage(
                model = food.imageUrl,
                contentDescription = food.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    // Tombol Download
                    IconButton(onClick = onDownload) {
                        Icon(
                            imageVector = Icons.Default.Download, // Ikon download/arrow down
                            contentDescription = "Download"
                        )
                    }
                }

                if (!food.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = food.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodDialog(
    onDismiss: () -> Unit,
    onUpload: (String, String, Uri) -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher Galeri
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Tambah Kuliner") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Makanan") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Area Pilih Gambar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Pilih Foto")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && imageUri != null) {
                        onUpload(name, description, imageUri!!)
                    }
                },
                enabled = !isLoading && name.isNotEmpty() && imageUri != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading...")
                } else {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Batal")
            }
        }
    )
}