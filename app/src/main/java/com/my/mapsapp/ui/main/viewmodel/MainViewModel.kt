package com.my.mapsapp.ui.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.my.mapsapp.data.model.Location
import com.my.mapsapp.data.repository.MainRepository
import kotlinx.coroutines.*

class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val locationsList = MutableLiveData<List<Location>>()
    var job: Job? = null
    val loading = MutableLiveData<Boolean>()

    fun getLocations() {
        job = CoroutineScope(Dispatchers.IO).launch {
            val response = mainRepository.getLocations()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    locationsList.postValue(response.body())
                    loading.value = false
                } else {
                    errorMessage.postValue("Error : ${response.message()} ")
                    loading.postValue(false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}