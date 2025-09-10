package com.umermahar.biometriclogin.presentation.enable_biometric

data class EnableBiometricState(
    val email: String = "",
    val password: String = "",
    val isNextButtonEnabled: Boolean = false
)
