package com.solenver.almacenpda.data.api.models

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val token: String
)
