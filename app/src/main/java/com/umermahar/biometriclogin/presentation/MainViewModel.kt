package com.umermahar.biometriclogin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umermahar.biometriclogin.domain.UserDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MainViewModel(
    private val userDataSource: UserDataSource
): ViewModel() {

    private val _isBiometricEnabled = MutableStateFlow<Boolean?>(null)
    val isBiometricEnabled = _isBiometricEnabled
        .onStart {
            _isBiometricEnabled.update {
                userDataSource.isBiometricEnabled.first()
            }
        }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(), null)


}