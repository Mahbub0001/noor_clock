package com.example.util

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

object GeminiQuotesClient {

    // --- Moshi Mapped Serialization Classes ---
    data class ContentPart(
        val text: String
    )

    data class ContentObject(
        val parts: List<ContentPart>
    )

    data class GenerateContentRequest(
        val contents: List<ContentObject>
    )

    data class Candidate(
        val content: ContentObject?
    )

    data class GenerateContentResponse(
        val candidates: List<Candidate>?
    )

    interface GeminiApiService {
        @POST("v1beta/models/gemini-3.5-flash:generateContent")
        suspend fun generateContent(
            @Query("key") apiKey: String,
            @Body request: GenerateContentRequest
        ): GenerateContentResponse
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateInspirationalQuote(weatherTemp: Double, weatherCond: String, activeTheme: String): String {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getFallbackQuote()
        }

        val prompt = """
            Write a 1-sentence or 2-sentence beautiful, highly inspiring and peaceful Islamic/Lifestyle morning motivation and mindfulness quote. 
            Context: The current weather is $weatherTemp°C and $weatherCond. The user's visual vibe is the peaceful $activeTheme pastel theme.
            Keep it deeply calming, spiritually uplifting, elegant, and positive. Avoid dry jargon, keep it poetic yet simple. Return ONLY the quote text and nothing else.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(ContentObject(parts = listOf(ContentPart(text = prompt))))
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: getFallbackQuote()
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackQuote()
        }
    }

    private fun getFallbackQuote(): String {
        val fallbacks = listOf(
            "Begin your day in the name of the One who split the dawn, lighting up your path with peace and purpose.",
            "Indeed, after every hardship comes ease. Let this morning's calm and fresh air be a witness to your constant heart.",
            "Your breath is a gift, this sunrise is a blessing. Step into your tasks with high hope and gentle sincerity.",
            "Trust the timing of your life. Every Fajr is a fresh canvas to align your deeds, studies, and worship.",
            "Peace is within the remembrance of the Divine, and success is built in the quiet, consistent discipline of today."
        )
        return fallbacks.random()
    }
}
