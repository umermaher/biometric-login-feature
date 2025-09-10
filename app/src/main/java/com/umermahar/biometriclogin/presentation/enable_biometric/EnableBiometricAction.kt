package com.umermahar.biometriclogin.presentation.enable_biometric

sealed interface EnableBiometricAction {
    data object OnBackButtonClicked: EnableBiometricAction
    data class OnEmailChanged(val value: String): EnableBiometricAction
    data class OnPasswordChanged(val value: String): EnableBiometricAction
    data object OnNextClicked: EnableBiometricAction
    data class OnAuthenticationSucceeded(val data: String): EnableBiometricAction
}