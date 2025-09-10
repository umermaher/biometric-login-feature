package com.umermahar.biometriclogin.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.umermahar.biometriclogin.presentation.ui.theme.BiometricLoginTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()

    val promptManager by lazy {
        BiometricPromptManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiometricLoginTheme {
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()

                isBiometricEnabled?.let { enabled ->
                    Navigation(enabled, promptManager)
                }
            }
        }
    }
}