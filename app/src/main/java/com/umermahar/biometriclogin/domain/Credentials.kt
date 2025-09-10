package com.umermahar.biometriclogin.domain

data class Credentials(
    val email: String,
    val encryptedPassword: String,
)