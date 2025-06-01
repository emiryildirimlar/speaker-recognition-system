package com.example.speakerrecoginitonfinal2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.speakerrecoginitonfinal2.data.network.RetrofitInstance
import com.example.speakerrecoginitonfinal2.data.repository.SpeakerRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpeakerViewModel::class.java)) {
            val repository = SpeakerRepository(RetrofitInstance.api)
            return SpeakerViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}