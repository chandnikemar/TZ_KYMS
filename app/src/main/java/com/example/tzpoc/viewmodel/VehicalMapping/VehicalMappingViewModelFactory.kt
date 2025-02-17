package com.example.tzpoc.viewmodel.VehicalMapping

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tzpoc.repository.TzRepository


class VehicleMappingViewModelFactory(
    private val application: Application,
    private val tzRepository: TzRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VehicalMappingViewModel(application, tzRepository) as T
    }
}