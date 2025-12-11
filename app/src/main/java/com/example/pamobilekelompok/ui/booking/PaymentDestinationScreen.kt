package com.example.pamobilekelompok.ui.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.pamobilekelompok.viewmodel.BookingViewModel

@Composable
fun PaymentDestinationScreen(
    bookingId: Long,
    totalPrice: Long,
    viewModel: BookingViewModel = viewModel(),
    onPaymentSuccess: () -> Unit
) {
    val context = LocalContext.current
    val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=BayarTagihan-$bookingId"

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pembayaran QRIS", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Scan QR Code di bawah ini", color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.size(250.dp).background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(qrCodeUrl),
                contentDescription = "QRIS",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Total Tagihan", fontSize = 16.sp)
        Text(text = "Rp $totalPrice", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(48.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.updatePaymentStatus(bookingId, context) { onPaymentSuccess() } },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Saya Sudah Bayar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}