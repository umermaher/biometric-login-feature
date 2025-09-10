package com.umermahar.biometriclogin.presentation.enable_biometric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umermahar.biometriclogin.domain.Credentials
import com.umermahar.biometriclogin.domain.UserDataSource
import com.umermahar.biometriclogin.presentation.enable_biometric.EnableBiometricEvent.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EnableBiometricViewModel(
    private val userDataSource: UserDataSource
): ViewModel() {
    private val _state = MutableStateFlow(EnableBiometricState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<EnableBiometricEvent>()
    val event = eventChannel.receiveAsFlow()

    init {
        state
            .distinctUntilChangedBy { it.email }
            .map { it.email.isNotBlank() }
            .onEach { isEmailFilled ->
                _state.update {
                    it.copy(isNextButtonEnabled = isEmailFilled && it.password.isNotBlank())
                }
            }
            .launchIn(viewModelScope)

        state
            .distinctUntilChangedBy { it.password }
            .map { it.password.isNotBlank() }
            .onEach { isPasswordFilled ->
                _state.update {
                    it.copy(isNextButtonEnabled = isPasswordFilled && it.email.isNotBlank())
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: EnableBiometricAction) {
        when(action) {
            is EnableBiometricAction.OnEmailChanged -> _state.update {
                it.copy(email = action.value)
            }

            is EnableBiometricAction.OnPasswordChanged -> _state.update {
                it.copy(password = action.value)
            }

            EnableBiometricAction.OnNextClicked -> viewModelScope.launch {
                eventChannel.send(
                    ShowBiometricPrompt(
                        data = state.value.password
                    )
                )
            }

            is EnableBiometricAction.OnAuthenticationSucceeded -> viewModelScope.launch {
                userDataSource.saveEncryptedCredentials(
                    data = Credentials(
                        email = state.value.email,
                        encryptedPassword = action.data
                    )
                )
                eventChannel.send(PopBackStack)
            }

            EnableBiometricAction.OnBackButtonClicked -> viewModelScope.launch {
                eventChannel.send(PopBackStack)
            }
        }
    }
}