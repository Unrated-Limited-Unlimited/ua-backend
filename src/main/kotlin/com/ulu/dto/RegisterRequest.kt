package com.ulu.dto

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val img: String?,
)
