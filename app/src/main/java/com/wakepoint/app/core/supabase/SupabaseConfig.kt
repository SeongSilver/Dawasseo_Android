package com.wakepoint.app.core.supabase

import com.wakepoint.app.BuildConfig
import javax.inject.Inject

data class SupabaseConfig @Inject constructor(
    val url: String = BuildConfig.SUPABASE_URL,
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY
) {
    val isConfigured: Boolean = url.isNotBlank() && anonKey.isNotBlank()
}
