// Path: app/src/main/java/com/example/pamobilekelompok/model/Hotel.kt

package com.example.pamobilekelompok.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Hotel(
    val id: Long? = null,
    val name: String,
    val address: String? = null,
    val price: String? = null,  // Format: "Rp 500.000/malam"
    val description: String? = null,
    val facilities: String? = null,  // Format: "WiFi, AC, TV, Breakfast"
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)