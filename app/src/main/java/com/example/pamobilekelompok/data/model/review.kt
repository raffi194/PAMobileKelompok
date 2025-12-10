package com.example.pamobilekelompok.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: Long? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("destination_name") val destinationName: String,
    val rating: Int, // 1-5
    val comment: String,
    @SerialName("image_url") val imageUrl: String? = null, // Optional
    @SerialName("created_at") val createdAt: String? = null
)