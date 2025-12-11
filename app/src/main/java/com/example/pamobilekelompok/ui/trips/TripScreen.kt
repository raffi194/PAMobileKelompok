package com.example.pamobilekelompok.ui.trips

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.pamobilekelompok.data.SupabaseClient
import com.example.pamobilekelompok.model.TripDoc
import com.example.pamobilekelompok.viewmodel.AuthViewModel
import com.example.pamobilekelompok.viewmodel.TripViewModel
import io.github.jan.supabase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    tripViewModel: TripViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    // State Navigasi Lokal
    var selectedTrip by remember { mutableStateOf<TripDoc?>(null) }

    // Handle tombol Back Hardware
    BackHandler(enabled = selectedTrip != null) {
        selectedTrip = null
    }

    // State Dialog
    var showDialog by remember { mutableStateOf(false) }
    var caption by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // State Edit Mode
    var isEditMode by remember { mutableStateOf(false) }
    var editingTrip by remember { mutableStateOf<TripDoc?>(null) }

    // State User
    var currentUserId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    LaunchedEffect(Unit) {
        tripViewModel.getTrips()
        val user = SupabaseClient.client.auth.currentUserOrNull()
        currentUserId = user?.id
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTrip == null) "Galeri Komunitas" else "Detail Postingan") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTrip != null) selectedTrip = null
                        else onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB animasi hilang saat masuk detail
            AnimatedVisibility(
                visible = selectedTrip == null,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(onClick = {
                    isEditMode = false; editingTrip = null
                    caption = ""; selectedImageUri = null
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, "Upload")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (tripViewModel.isLoading && !showDialog) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (tripViewModel.tripDocs.isEmpty()) {
                Text(
                    text = "Belum ada momen dibagikan.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {

                // --- ANIMASI ZOOM IN / OUT ---
                AnimatedContent(
                    targetState = selectedTrip,
                    transitionSpec = {
                        if (targetState != null) {
                            // SAAT MASUK DETAIL: Zoom In (Membesar) + Fade In
                            (fadeIn(animationSpec = tween(300)) +
                                    scaleIn(initialScale = 0.85f, animationSpec = tween(300)))
                                .togetherWith(fadeOut(animationSpec = tween(300)))
                        } else {
                            // SAAT KEMBALI KE GRID: Zoom Out (Detail Mengecil) + Fade Out
                            fadeIn(animationSpec = tween(300))
                                .togetherWith(fadeOut(animationSpec = tween(300)) +
                                        scaleOut(targetScale = 0.85f, animationSpec = tween(300)))
                        }
                    },
                    label = "ZoomTransition"
                ) { trip ->
                    if (trip == null) {
                        // TAMPILAN GRID
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(tripViewModel.tripDocs) { doc ->
                                TripThumbnail(
                                    trip = doc,
                                    onClick = { selectedTrip = doc }
                                )
                            }
                        }
                    } else {
                        // TAMPILAN DETAIL
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            TripCard(
                                trip = trip,
                                isOwner = trip.userId == currentUserId,
                                onDelete = {
                                    tripViewModel.deleteTrip(trip, context)
                                    selectedTrip = null
                                },
                                onEdit = {
                                    isEditMode = true
                                    editingTrip = trip
                                    caption = trip.caption
                                    selectedImageUri = null
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- DIALOG ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { if (!tripViewModel.isLoading) showDialog = false },
                title = { Text(if (isEditMode) "Edit Postingan" else "Bagikan Momen") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            label = { Text("Caption...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 5
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedImageUri != null) "Ganti Foto Terpilih" else "Pilih Foto")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedImageUri != null) {
                            AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        } else if (isEditMode && editingTrip != null) {
                            AsyncImage(model = editingTrip!!.mediaUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val uid = currentUserId ?: ""
                            if (uid.isNotEmpty()) {
                                if (isEditMode && editingTrip != null) {
                                    tripViewModel.updateTrip(editingTrip!!, caption, selectedImageUri, context) {
                                        showDialog = false
                                        selectedTrip = null
                                        tripViewModel.getTrips()
                                    }
                                } else {
                                    if (caption.isNotBlank() && selectedImageUri != null) {
                                        tripViewModel.uploadTrip(caption, selectedImageUri!!, uid, context) { showDialog = false }
                                    } else {
                                        Toast.makeText(context, "Lengkapi data", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = !tripViewModel.isLoading
                    ) { Text("Simpan") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
            )
        }
    }
}

// --- KOMPONEN THUMBNAIL ---
@Composable
fun TripThumbnail(trip: TripDoc, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = trip.mediaUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// --- KOMPONEN DETAIL CARD ---
@Composable
fun TripCard(
    trip: TripDoc,
    isOwner: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val displayName = trip.users?.displayName ?: "Traveler"

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp).fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = displayName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = displayName, fontWeight = FontWeight.Bold)
                }
                if (isOwner) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFFFA000)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, "Hapus", tint = Color.Red) }
                }
            }

            AsyncImage(
                model = trip.mediaUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                contentScale = ContentScale.FillWidth
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = trip.caption, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}