package com.umermahar.biometriclogin.presentation.enable_biometric

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umermahar.biometriclogin.presentation.BiometricPromptManager
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.umermahar.biometriclogin.R
import com.umermahar.biometriclogin.presentation.enable_biometric.EnableBiometricAction.*
import com.umermahar.biometriclogin.presentation.login.LoginAction
import com.umermahar.biometriclogin.presentation.utils.ObserveAsEvents
import com.umermahar.biometriclogin.presentation.utils.goToBiometricSettings
import com.umermahar.biometriclogin.presentation.utils.showToast

@Composable
fun EnableBiometricScreen(
    biometricPromptManager: BiometricPromptManager,
    viewModel: EnableBiometricViewModel = koinViewModel(),
    popBackStack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.event) { event ->
        when(event) {
            is EnableBiometricEvent.ShowBiometricPrompt -> biometricPromptManager.showBiometricPromptToEnableAuth(
                title = context.getString(R.string.enable_biometric_login),
                description = context.getString(R.string.enable_biometric_login_desc),
                dataToEncrypt = event.data
            )

            EnableBiometricEvent.PopBackStack -> popBackStack()
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
                OnAuthenticationSucceeded(res.data)
            )
            BiometricPromptManager.BiometricResult.FeatureUnavailable -> context.showToast(
                context.getString(R.string.feature_unavailable)
            )
            BiometricPromptManager.BiometricResult.HardwareUnavailable -> context.showToast(
                context.getString(R.string.hardware_failed)
            )
            else -> {}
        }
    }

    EnableBiometricScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnableBiometricScreen(
    state: EnableBiometricState,
    onAction: (EnableBiometricAction) -> Unit,
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.enable_biometric))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onAction(OnBackButtonClicked)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
            )
        }
    ) { pd ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.email,
                onValueChange = {
                    onAction(OnEmailChanged(it))
                },
                label = {
                    Text(text = stringResource(id = R.string.email))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.password,
                onValueChange = {
                    onAction(OnPasswordChanged(it))
                },
                label = {
                    Text(text = stringResource(id = R.string.password))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onAction(OnNextClicked)
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onAction(OnNextClicked)
                },
                enabled = state.isNextButtonEnabled
            ) {
                Text(
                    text = stringResource(id = R.string.next),
                )
            }
        }
    }
}