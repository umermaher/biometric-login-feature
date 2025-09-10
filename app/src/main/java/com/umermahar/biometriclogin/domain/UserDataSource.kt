package com.umermahar.biometriclogin.domain

import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    val isBiometricEnabled: Flow<Boolean>
    suspend fun disableBiometric()
    suspend fun saveEncryptedCredentials(data: Credentials)
    suspend fun getEncryptedCredentials(): Credentials?
}