package com.my.mapsapp.ui.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.my.mapsapp.data.model.Location
import com.my.mapsapp.data.repository.MainRepository
import kotlinx.coroutines.*

class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {
    val isError = MutableLiveData<Boolean>()
    val locationsList = MutableLiveData<List<Location>>()
    var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        onError()
    }
    val loading = MutableLiveData<Boolean>()

    fun getLocations() {
        isError.value=false
        loading.value=true
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = mainRepository.getLocations()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    locationsList.postValue(response.body())
                    loading.value = false
                } else {
                    onError()
                }
            }
        }
    }

    private fun onError() {
        loading.postValue(false)
        isError.postValue(true)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}