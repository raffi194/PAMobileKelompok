package com.example.pamobilekelompok.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventBooking(
    val id: Long? = null,

    // ID User yang login (Foreign Key ke auth.users)
    @SerialName("user_id")
    val userId: String,

    // ID Event yang dibooking (Foreign Key ke events)
    @SerialName("event_id")
    val eventId: Long,

    // Nama Event (Disimpan agar history tetap ada walau event asli dihapus/ubah)
    @SerialName("event_title")
    val eventTitle: String,

    // Tanggal kedatangan (Format: yyyy-MM-dd)
    @SerialName("booking_date")
    val bookingDate: String,

    // Jumlah tiket
    @SerialName("ticket_count")
    val ticketCount: Int,

    // Total bayar
    @SerialName("total_price")
    val totalPrice: Long,

    // Status pembayaran (Default: Menunggu Pembayaran)
    val status: String = "Menunggu Pembayaran")