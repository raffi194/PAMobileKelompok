// PAMobileKelompok/app/src/main/java/com/example/pamobilekelompok/data/model/Review.kt
package com.example.pamobilekelompok.data.model

import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.annotations.PostgrestSerialName
import io.github.jan.supabase.postgrest.annotations.ColumnName

@Serializable
@PostgrestSerialName("reviews") // Nama tabel di Supabase
data class Review(
    @ColumnName("id")
    val id: Long = 0,
    @ColumnName("user_email")
    val userEmail: String,
    @ColumnName("place_name")
    val placeName: String, // Nama tempat yang direview
    @ColumnName("comment")
    val comment: String,
    @ColumnName("rating")
    val rating: Int,
    @ColumnName("image_url")
    val imageUrl: String? = null, // URL foto dari Storage (opsional)
    @ColumnName("created_at")
    val createdAt: String? = null // Supabase harusnya mengisi ini otomatis
)