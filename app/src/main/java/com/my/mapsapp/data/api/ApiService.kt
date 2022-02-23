package com.my.mapsapp.data.api

import com.my.mapsapp.data.model.Location
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL="https://restcountries.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

interface ApiService {
    @GET("v2/lang/es")
    suspend fun getLocations(): Response<List<Location>>
}

object RetrofitBuilder {

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .build()
    }

    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}