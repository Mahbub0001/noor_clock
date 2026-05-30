package com.example.util

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

object LocationGeocodingClient {

    data class GeocodingResult(
        @Json(name = "id") val id: Long?,
        @Json(name = "name") val name: String,
        @Json(name = "latitude") val latitude: Double,
        @Json(name = "longitude") val longitude: Double,
        @Json(name = "country") val country: String?,
        @Json(name = "admin1") val admin1: String?
    )

    data class GeocodingResponse(
        @Json(name = "results") val results: List<GeocodingResult>?
    )

    interface GeocodingApiService {
        @GET("v1/search")
        suspend fun searchLocations(
            @Query("name") name: String,
            @Query("count") count: Int = 10,
            @Query("language") language: String = "en",
            @Query("format") format: String = "json"
        ): GeocodingResponse
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val apiService: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeocodingApiService::class.java)
    }

    suspend fun searchLocation(query: String): List<GeocodingResult> {
        if (query.trim().length < 2) return emptyList()
        return try {
            val response = apiService.searchLocations(query)
            response.results ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
