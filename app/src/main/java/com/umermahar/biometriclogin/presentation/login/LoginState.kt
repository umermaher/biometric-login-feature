package com.umermahar.biometriclogin.presentation.login

import com.umermahar.biometriclogin.domain.CredentialsForLogin

data class LoginState(
    val credentials: CredentialsForLogin? = null,
)
