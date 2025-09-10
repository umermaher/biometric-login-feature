package com.umermahar.biometriclogin.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.umermahar.biometriclogin.presentation.enable_biometric.EnableBiometricScreen
import com.umermahar.biometriclogin.presentation.login.LoginScreen
import com.umermahar.biometriclogin.presentation.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    isBiometricEnabled: Boolean,
    biometricPromptManager: BiometricPromptManager
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if(isBiometricEnabled) {
            Login
        } else Settings
    ) {
        composable<Login> {
            LoginScreen(
                biometricPromptManager = biometricPromptManager,
                navigateToSettingsScreen = {
                    navController.navigate(Settings) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<Settings> {
            SettingsScreen(
                biometricPromptManager = biometricPromptManager,
                navigate = { route, isNavigateOnLogout ->
                    navController.navigate(route) {
                        if (isNavigateOnLogout) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable<EnableBiometric> {
            EnableBiometricScreen(
                biometricPromptManager = biometricPromptManager,
                popBackStack = {
                    navController.popBackStack()
                }
            )
        }
    }

}

@Serializable
object Login

@Serializable
object EnableBiometric

@Serializable
object Settings