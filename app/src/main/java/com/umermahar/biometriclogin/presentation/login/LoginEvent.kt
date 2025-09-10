package com.umermahar.biometriclogin.presentation.login

sealed interface LoginEvent {
    data object ProcessBiometricSetupFlow: LoginEvent
    data class ShowBiometricPrompt(val data: String): LoginEvent
}