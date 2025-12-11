package com.example.pamobilekelompok.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalFood(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl:String
)