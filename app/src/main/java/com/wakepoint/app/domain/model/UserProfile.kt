package com.wakepoint.app.domain.model

data class UserProfile(
    val id: String,
    val email: String,
    val nickname: String,
    val avatarUrl: String?,
    val pushToken: String?
)
