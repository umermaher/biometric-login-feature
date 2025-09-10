package com.umermahar.biometriclogin.data

import android.content.Context
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.umermahar.biometriclogin.data.UserLocalDataSource.PreferencesKeys.APP_PREFS
import com.umermahar.biometriclogin.domain.Credentials
import com.umermahar.biometriclogin.domain.UserDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserLocalDataSource(
    private val context: Context,
): UserDataSource {

    private val Context.credentialsDataStore by dataStore(
        fileName = PreferencesKeys.CREDENTIALS_PREFERENCES_NAME,
        serializer = CredentialsPreferencesSerializer
    )

    val Context.preferenceDataStore by preferencesDataStore(name = APP_PREFS)

    override val isBiometricEnabled = context.preferenceDataStore.data
        .map { pref ->
            pref[PreferencesKeys.IS_BIOMETRIC_ENABLED] == true
        }

    override suspend fun disableBiometric() {
        context.preferenceDataStore.edit { pref ->
            pref[PreferencesKeys.IS_BIOMETRIC_ENABLED] = false
        }
        context.credentialsDataStore.updateData {
            CredentialsPreferences()
        }
    }

    override suspend fun saveEncryptedCredentials(credentials: Credentials) {
        context.preferenceDataStore.edit { pref ->
            pref[PreferencesKeys.IS_BIOMETRIC_ENABLED] = true
        }
        context.credentialsDataStore.updateData {
            CredentialsPreferences(
                email = credentials.email,
                encryptedPassword = credentials.encryptedPassword
            )
        }
    }

    override suspend fun getEncryptedCredentials(): Credentials? {
        return context.credentialsDataStore.data.first().toCredentialsDomain()
    }

    private object PreferencesKeys {
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        const val CREDENTIALS_PREFERENCES_NAME = "credential_preferences"
        const val APP_PREFS = "app_preferences"
    }
}