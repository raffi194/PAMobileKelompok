package com.example.pamobilekelompok.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripDoc(
    val id: Long? = null,

    // foreign key ke tabel users
    @SerialName("user_id")
    val userId: String,

    val caption: String,

    @SerialName("media_url")
    val mediaUrl: String,

    @SerialName("created_at")
    val createdAt: String? = null,

    // ngambil data dari tabel users
    val users: UserProfile? = null
)

// class helper untuk menangkap data hasil join
@Serializable
data class UserProfile(
    @SerialName("display_name")
    val displayName: String? = null
)