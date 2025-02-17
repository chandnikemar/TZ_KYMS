    package com.example.tzpoc.viewmodel.vehicalJobDetail

    import android.app.Application
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import com.example.tzpoc.repository.TzRepository

    class VehicleJobViewModelFactory(
        private val application: Application,
        private val tzRepository: TzRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EntryViewModel(application, tzRepository) as T
        }
    }
