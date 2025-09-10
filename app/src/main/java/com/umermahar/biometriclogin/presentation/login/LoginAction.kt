package com.umermahar.biometriclogin.presentation.login

sealed interface LoginAction {
    data object OnLoginButtonClicked: LoginAction
    data class OnLoginSucceeded(val data: String): LoginAction
}