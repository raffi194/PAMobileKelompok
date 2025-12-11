package com.example.pamobilekelompok.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HotelBooking(
    val id: Long? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("hotel_name")
    val hotelName: String,

    @SerialName("guest_name")
    val guestName: String,

    @SerialName("guest_email")
    val guestEmail: String,

    @SerialName("guest_phone")
    val guestPhone: String,

    @SerialName("check_in_date")
    val checkInDate: String,

    @SerialName("check_out_date")
    val checkOutDate: String,

    @SerialName("total_nights")
    val totalNights: Int,

    @SerialName("price_per_night")
    val pricePerNight: Long,

    @SerialName("total_price")
    val totalPrice: Long,

    val status: String = "Menunggu Pembayaran",

    @SerialName("created_at")
    val createdAt: String? = null
)
