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

object WeatherClient {

    data class CurrentWeather(
        @Json(name = "temperature_2m") val temperature: Double,
        @Json(name = "weather_code") val weatherCode: Int
    )

    data class WeatherResponse(
        @Json(name = "latitude") val latitude: Double,
        @Json(name = "longitude") val longitude: Double,
        @Json(name = "current") val current: CurrentWeather?
    )

    interface OpenMeteoApiService {
        @GET("v1/forecast")
        suspend fun getCurrentWeather(
            @Query("latitude") lat: Double,
            @Query("longitude") lon: Double,
            @Query("current") current: String = "temperature_2m,weather_code"
        ): WeatherResponse
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val apiService: OpenMeteoApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoApiService::class.java)
    }

    suspend fun fetchWeather(latitude: Double, longitude: Double): Pair<Double, String> {
        return try {
            val response = apiService.getCurrentWeather(latitude, longitude)
            val temp = response.current?.temperature ?: 24.0
            val code = response.current?.weatherCode ?: 0
            val condition = mapWeatherCodeToCondition(code)
            Pair(temp, condition)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(25.0, "Sunny (Off-line)") // Safe aesthetic default if offline
        }
    }

    fun mapWeatherCodeToCondition(code: Int): String {
        return when (code) {
            0 -> "Sunny / Clear"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Light Drizzle"
            61, 63, 65 -> "Rainy / Showers"
            71, 73, 75 -> "Snowy"
            80, 81, 82 -> "Heavy Rain"
            95, 96, 99 -> "Thunderstorm"
            else -> "Calm Sunrise"
        }
    }
}
