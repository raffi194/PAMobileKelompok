package com.example.pamobilekelompok.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: Long? = null,
    val title: String,
    val description: String,
    @SerialName("event_date") val eventDate: String,
    @SerialName("poster_url") val posterUrl: String? = null
)

@Serializable
data class Review(
    val id: Long? = null,

    @SerialName("destination_id")
    val destinationId: Long,

    @SerialName("user_name")
    val userName: String,

    val rating: Int,

    val comment: String,

    @SerialName("image_url")
    val imageUrl: String? = null
)