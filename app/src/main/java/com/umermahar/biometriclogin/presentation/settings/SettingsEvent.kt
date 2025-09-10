package com.umermahar.biometriclogin.presentation.settings

sealed interface SettingsEvent {
    data class Navigate(val route: Any, val isNavigateOnLogout: Boolean = false): SettingsEvent
}