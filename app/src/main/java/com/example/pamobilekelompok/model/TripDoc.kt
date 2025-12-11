package com.example.pamobilekelompok.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripDoc(
    val id: Long? = null,

    // Foreign Key ke tabel users
    @SerialName("user_id")
    val userId: String,

    val caption: String,

    @SerialName("media_url")
    val mediaUrl: String,

    @SerialName("created_at")
    val createdAt: String? = null,

    // Relasi: Mengambil data dari tabel 'users'
    // Nama variabel 'users' harus sama dengan nama tabel referensi di Supabase
    val users: UserProfile? = null
)

// Class helper untuk menangkap data hasil Join
@Serializable
data class UserProfile(
    @SerialName("display_name")
    val displayName: String? = null
)