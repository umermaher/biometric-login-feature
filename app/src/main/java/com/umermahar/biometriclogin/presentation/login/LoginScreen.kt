package com.umermahar.biometriclogin.presentation.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umermahar.biometriclogin.R
import com.umermahar.biometriclogin.presentation.BiometricPromptManager
import com.umermahar.biometriclogin.presentation.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import com.umermahar.biometriclogin.presentation.login.LoginAction.*
import com.umermahar.biometriclogin.presentation.utils.goToBiometricSettings
import com.umermahar.biometriclogin.presentation.utils.showToast

@Composable
fun LoginScreen(
    biometricPromptManager: BiometricPromptManager,
    navigateToSettingsScreen: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when(event) {
            is LoginEvent.ShowBiometricPrompt -> biometricPromptManager.showBiometricPrompt(
                title = context.getString(R.string.login),
                description = context.getString(R.string.authentication_to_continue),
                encryptedPayloadBase64 = event.data
            )

            LoginEvent.ProcessBiometricSetupFlow -> if(biometricPromptManager.canAuthenticate()) {
                navigateToSettingsScreen()
            }
        }
    }

    ObserveAsEvents(biometricPromptManager.promptResult) { res ->
        when(res) {
            is BiometricPromptManager.BiometricResult.AuthenticationError -> context.showToast(res.error)
            BiometricPromptManager.BiometricResult.AuthenticationFailed -> context.showToast(
                context.getString(R.string.authentication_failed)
            )
            BiometricPromptManager.BiometricResult.AuthenticationNotSet -> context.goToBiometricSettings()
            is BiometricPromptManager.BiometricResult.AuthenticationSuccess -> viewModel.onAction(
                OnLoginSucceeded(res.data)
            )
            BiometricPromptManager.BiometricResult.FeatureUnavailable -> context.showToast(
                context.getString(R.string.feature_unavailable)
            )
            BiometricPromptManager.BiometricResult.HardwareUnavailable -> context.showToast(
                context.getString(R.string.hardware_failed)
            )

            BiometricPromptManager.BiometricResult.AuthenticationFailedDueToBiometricEnrollment -> {
                context.showToast(context.getString(R.string.biometric_enrollment_changed_msg))
                viewModel.clearBiometricAuthentication()
            }
        }
    }

    LoginScreen(state, viewModel::onAction)
}

@Composable
private fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit
) {

    Scaffold { pd ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pd)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if(state.credentials != null) {
                Column {
                    Text(
                        text = "Send it to server",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        text = state.credentials.email + "\n" + state.credentials.password,
                        textAlign = TextAlign.Center
                    )
                }
            } else {

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onAction(OnLoginButtonClicked)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = stringResource(id = R.string.login)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.biometric),
                    )
                }

            }
        }
    }
}