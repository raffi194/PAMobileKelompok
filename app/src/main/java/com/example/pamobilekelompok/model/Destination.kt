package com.example.pamobilekelompok.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Destination(
    val id: Long? = null, // Gunakan Long untuk tipe data bigint di Supabase
    val name: String,
    val description: String? = null, // Ubah jadi Nullable agar aman jika kosong di DB
    @SerialName("image_url") val imageUrl: String? = null
)