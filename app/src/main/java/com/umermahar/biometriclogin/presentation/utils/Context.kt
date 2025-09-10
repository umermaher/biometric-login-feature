package com.umermahar.biometriclogin.presentation.utils

import android.content.Context
import android.widget.Toast
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager

fun Context.showToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.goToBiometricSettings() {
    val intent = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            // Android 11+ (API 30+)
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BiometricManager.Authenticators.BIOMETRIC_STRONG)
            }
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
            // Android 9 and 10 (API 28–29)
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        else -> {
            // Fallback for older devices
            Intent(Settings.ACTION_SETTINGS)
        }
    }
    this.startActivity(intent)
}
