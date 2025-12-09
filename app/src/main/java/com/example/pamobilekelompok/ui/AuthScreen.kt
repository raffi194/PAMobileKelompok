package com.example.pamobilekelompok.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamobilekelompok.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(),
    isRegister: Boolean = false, // Default false artinya halaman Login
    onNavigateSuccess: () -> Unit,
    onNavigateToOtherScreen: () -> Unit // Pindah dari Login ke Register atau sebaliknya
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegister) "Daftar Akun Baru" else "Selamat Datang",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tampilkan Pesan Error jika ada
        authViewModel.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Tombol Login / Register
        if (authViewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        if (isRegister) {
                            authViewModel.register(email, password, context) {
                                onNavigateToOtherScreen() // Setelah daftar, pindah ke Login
                            }
                        } else {
                            authViewModel.login(email, password, context, onNavigateSuccess)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isRegister) "Daftar Sekarang" else "Masuk")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Ganti Mode (Login <-> Register)
        TextButton(onClick = onNavigateToOtherScreen) {
            Text(
                text = if (isRegister) "Sudah punya akun? Login" else "Belum punya akun? Daftar"
            )
        }
    }
}