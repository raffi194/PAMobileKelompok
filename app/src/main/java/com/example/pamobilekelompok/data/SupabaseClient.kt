package com.example.pamobilekelompok.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    // URL dan Key Project Anda
    private const val SUPABASE_URL = "https://svlllxlgjffmzycctoit.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN2bGxseGxnamZmbXp5Y2N0b2l0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUyNzYyOTgsImV4cCI6MjA4MDg1MjI5OH0.GwxsCC_mUVI0oS4YZmz0a3HNLIhAWZ546jMPyN6w8Os"
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}