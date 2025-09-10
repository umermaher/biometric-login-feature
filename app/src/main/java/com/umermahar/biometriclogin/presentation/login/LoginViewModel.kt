package com.umermahar.biometriclogin.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umermahar.biometriclogin.domain.CredentialsForLogin
import com.umermahar.biometriclogin.domain.UserDataSource
import com.umermahar.biometriclogin.presentation.login.LoginEvent.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userDataSource: UserDataSource
): ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<LoginEvent>()
    val  event = eventChannel.receiveAsFlow()

    private var email: String? = null

    fun onAction(action: LoginAction) {
        when(action) {
            LoginAction.OnLoginButtonClicked -> viewModelScope.launch {
                val credentials = userDataSource.getEncryptedCredentials()
                if(credentials == null) {
                    eventChannel.send(ProcessBiometricSetupFlow)
                } else {
                    email = credentials.email
                    eventChannel.send(ShowBiometricPrompt(credentials.encryptedPassword))
                }
            }

            is LoginAction.OnLoginSucceeded -> {
                // send it to server
                _state.update {
                    it.copy(
                    credentials = CredentialsForLogin(
                        email = email ?: return,
                        password = action.data
                    )
                ) }
            }
        }
    }

    fun clearBiometricAuthentication() = viewModelScope.launch {
        userDataSource.disableBiometric()
        eventChannel.send(ProcessBiometricSetupFlow)
    }
}