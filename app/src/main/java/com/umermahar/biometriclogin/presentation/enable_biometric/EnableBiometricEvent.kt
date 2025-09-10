package com.umermahar.biometriclogin.presentation.enable_biometric

sealed interface EnableBiometricEvent {
    data class ShowBiometricPrompt(val data: String): EnableBiometricEvent
    data object PopBackStack: EnableBiometricEvent
}