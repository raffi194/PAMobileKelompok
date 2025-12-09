package com.example.pamobilekelompok.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pamobilekelompok.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // State untuk Loading dan Error
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Fungsi Login
    fun login(emailInput: String, passwordInput: String, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Panggil Supabase Auth (Sign In)
                SupabaseClient.client.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }

                // Jika sukses
                Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                onSuccess()

            } catch (e: Exception) {
                errorMessage = e.message ?: "Terjadi kesalahan saat login"
            } finally {
                isLoading = false
            }
        }
    }

    // Fungsi Register
    fun register(emailInput: String, passwordInput: String, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Panggil Supabase Auth (Sign Up)
                SupabaseClient.client.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }

                // Pesan Verifikasi Email (Karena Confirm Email Aktif)
                Toast.makeText(
                    context,
                    "Registrasi Berhasil! Cek INBOX EMAIL Anda untuk verifikasi.",
                    Toast.LENGTH_LONG
                ).show()

                onSuccess()

            } catch (e: Exception) {
                errorMessage = e.message ?: "Terjadi kesalahan saat register"
            } finally {
                isLoading = false
            }
        }
    }
}