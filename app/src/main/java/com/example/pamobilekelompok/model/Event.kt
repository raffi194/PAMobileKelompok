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