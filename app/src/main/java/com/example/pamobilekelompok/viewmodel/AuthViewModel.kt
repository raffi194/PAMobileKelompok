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
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// Model sederhana untuk mengambil role
@Serializable
data class UserRole(val role: String)

class AuthViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var currentUserEmail by mutableStateOf<String?>(null)
    var currentUserDisplay by mutableStateOf<String?>(null)

    // State baru: Apakah user admin?
    var isAdmin by mutableStateOf(false)

    fun getCurrentUser() {
        viewModelScope.launch {
            val user = SupabaseClient.client.auth.currentUserOrNull()
            if (user != null) {
                currentUserEmail = user.email
                val metadata = user.userMetadata
                currentUserDisplay = metadata?.get("username")?.toString()?.removeSurrounding("\"") ?: "User"

                // Cek Role di Database
                checkUserRole(user.id)
            }
        }
    }

    private suspend fun checkUserRole(userId: String) {
        try {
            // Query ke tabel 'users' untuk ambil kolom 'role'
            val result = SupabaseClient.client.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingleOrNull<UserRole>()

            isAdmin = result?.role == "admin"
        } catch (e: Exception) {
            isAdmin = false // Default user biasa jika error
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                onSuccess()
            } catch (e: Exception) { }
        }
    }

    fun login(emailInput: String, passwordInput: String, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                SupabaseClient.client.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Login Gagal"
            } finally {
                isLoading = false
            }
        }
    }

    fun register(emailInput: String, passwordInput: String, usernameInput: String, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                SupabaseClient.client.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                    data = buildJsonObject { put("username", usernameInput) }
                }
                Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_LONG).show()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Register Gagal"
            } finally {
                isLoading = false
            }
        }
    }
}