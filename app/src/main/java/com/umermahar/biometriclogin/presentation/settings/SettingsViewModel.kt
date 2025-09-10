package com.umermahar.biometriclogin.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umermahar.biometriclogin.domain.UserDataSource
import com.umermahar.biometriclogin.presentation.EnableBiometric
import com.umermahar.biometriclogin.presentation.Login
import com.umermahar.biometriclogin.presentation.settings.SettingsEvent.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userDataSource: UserDataSource
): ViewModel() {

    val isBiometricEnabled = userDataSource.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val eventChannel = Channel<SettingsEvent>()
    val event = eventChannel.receiveAsFlow()

    fun onAction(action: SettingsAction) {
        when(action) {
            is SettingsAction.OnEnableBiometric -> viewModelScope.launch {
                if(action.shouldEnable) {
                    eventChannel.send(Navigate(EnableBiometric))
                } else userDataSource.disableBiometric()
            }

            SettingsAction.OnLogoutButtonClicked -> viewModelScope.launch {
                eventChannel.send(
                    Navigate(route = Login, isNavigateOnLogout = true)
                )
            }
        }
    }
}