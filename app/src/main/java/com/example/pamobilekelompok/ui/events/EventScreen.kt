package com.example.pamobilekelompok.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import com.example.pamobilekelompok.model.Event
import com.example.pamobilekelompok.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    viewModel: EventViewModel = viewModel(),
    isAdmin: Boolean = false,
    onNavigateToAdd: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Event) -> Unit,
    onNavigateToDetail: (Event) -> Unit
) {
    LaunchedEffect(Unit) { viewModel.getEvents() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalender Event Wisata") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Kembali") } }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = onNavigateToAdd) { Icon(Icons.Default.Add, "Tambah") }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (viewModel.events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada event.", color = Color.Gray) }
            } else {
                val context = LocalContext.current
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(viewModel.events) { event ->
                        // 2. TERUSKAN KE EVENT ITEM
                        EventItem(
                            event = event,
                            isAdmin = isAdmin,
                            onEdit = { onNavigateToEdit(event) },
                            onDelete = { viewModel.deleteEvent(event, context) },
                            onClick = { onNavigateToDetail(event) } // PANGGIL DI SINI
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventItem(
    event: Event,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Event?") },
            text = { Text("Yakin hapus '${event.title}'?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Hapus", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") } }
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box {
            Column {
                AsyncImage(
                    model = event.posterUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(event.eventDate, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(event.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
            }
            if (isAdmin) {
                Row(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.background(Color.White.copy(0.7f), CircleShape)) {
                        Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFFFA000))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.background(Color.White.copy(0.7f), CircleShape)) {
                        Icon(Icons.Default.Delete, "Hapus", tint = Color.Red)
                    }
                }
            }
        }
    }
}