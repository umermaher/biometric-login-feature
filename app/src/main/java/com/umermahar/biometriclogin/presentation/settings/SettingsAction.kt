package com.umermahar.biometriclogin.presentation.settings

sealed interface SettingsAction {
    data class OnEnableBiometric(val shouldEnable: Boolean) : SettingsAction
    data object OnLogoutButtonClicked : SettingsAction
}