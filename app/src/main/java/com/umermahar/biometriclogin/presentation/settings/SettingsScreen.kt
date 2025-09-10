package com.umermahar.biometriclogin.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umermahar.biometriclogin.R
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavOptionsBuilder
import com.umermahar.biometriclogin.presentation.BiometricPromptManager
import com.umermahar.biometriclogin.presentation.enable_biometric.EnableBiometricAction.OnAuthenticationSucceeded
import com.umermahar.biometriclogin.presentation.login.LoginAction.OnLoginSucceeded
import com.umermahar.biometriclogin.presentation.utils.ObserveAsEvents
import com.umermahar.biometriclogin.presentation.utils.goToBiometricSettings
import com.umermahar.biometriclogin.presentation.utils.showToast
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    biometricPromptManager: BiometricPromptManager,
    viewModel: SettingsViewModel = koinViewModel(),
    navigate: (route: Any, isNavigateOnLogout: Boolean) -> Unit,
) {

    val context = LocalContext.current

    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when(event) {
            is SettingsEvent.Navigate -> {
                navigate(event.route, event.isNavigateOnLogout)
            }
        }
    }

    ObserveAsEvents(biometricPromptManager.promptResult) { res ->
        when(res) {
            BiometricPromptManager.BiometricResult.AuthenticationNotSet -> context.goToBiometricSettings()
            BiometricPromptManager.BiometricResult.FeatureUnavailable -> context.showToast(
                context.getString(R.string.feature_unavailable)
            )
            BiometricPromptManager.BiometricResult.HardwareUnavailable -> context.showToast(
                context.getString(R.string.hardware_failed)
            )
            else -> {}
        }
    }

    SettingsScreen(
        isBiometricEnabled = isBiometricEnabled,
        onAction = { action ->
            if(action is SettingsAction.OnEnableBiometric && action.shouldEnable) {
                // can authenticate will check as well as show the appropriate message
                if(biometricPromptManager.canAuthenticate()) {
                    viewModel.onAction(action)
                }
            } else viewModel.onAction(action)
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    isBiometricEnabled: Boolean,
    onAction: (SettingsAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.main))
                },
                actions = {
                    if(isBiometricEnabled) {
                        IconButton(
                            onClick = {
                                onAction(SettingsAction.OnLogoutButtonClicked)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PowerSettingsNew,
                                contentDescription = stringResource(id = R.string.logout)
                            )
                        }
                    }
                }
            )
        },
    ) { pd ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pd)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "This Screen is just for demonstration as Settings Screen where user can enable/disable biometric login",
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    onAction(SettingsAction.OnEnableBiometric(!isBiometricEnabled))
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.enable_biometric))

                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = {
                            onAction(SettingsAction.OnEnableBiometric(it))
                        }
                    )
                }
            }
        }
    }
}