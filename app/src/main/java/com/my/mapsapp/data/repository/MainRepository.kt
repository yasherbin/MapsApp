package com.my.mapsapp.data.repository

import com.my.mapsapp.data.api.ApiService

class MainRepository(private val apiService: ApiService) {

    suspend fun getLocations() = apiService.getLocations()
}