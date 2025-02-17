package com.example.tzpoc.viewmodel.mapping

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tzpoc.repository.TzRepository
import com.example.tzpoc.viewmodel.login.LoginViewModel

class MappingViewModelFactory(
    private val application: Application,
    private val tzRepository: TzRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MappingViewModel(application, tzRepository) as T
    }
}