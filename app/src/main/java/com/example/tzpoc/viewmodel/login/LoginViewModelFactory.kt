package com.example.tzpoc.viewmodel.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.tzpoc.repository.TzRepository


class LoginViewModelFactory(
    private val application: Application,
    private val tzRepository: TzRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(application, tzRepository) as T
    }
}