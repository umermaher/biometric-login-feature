package com.umermahar.biometriclogin.di

import com.umermahar.biometriclogin.data.UserLocalDataSource
import com.umermahar.biometriclogin.domain.UserDataSource
import com.umermahar.biometriclogin.presentation.MainViewModel
import com.umermahar.biometriclogin.presentation.enable_biometric.EnableBiometricViewModel
import com.umermahar.biometriclogin.presentation.login.LoginViewModel
import com.umermahar.biometriclogin.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    singleOf(::UserLocalDataSource).bind<UserDataSource>()

    viewModelOf(::EnableBiometricViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::MainViewModel)
}