package com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote

import androidx.annotation.Keep
import com.google.gson.Gson
import com.pixeleye.einbuergerungstest.lebenindeutschland.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object AiExplanationService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private const val API_URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL = "llama-3.3-70b-versatile"

    suspend fun generateExplanation(
        question: String,
        options: List<String>,
        correctAnswer: String,
        language: String
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY
        
        val systemPrompt = """
            You are an expert, friendly German Citizenship teacher. 
            The student is studying for the "Einbürgerungstest".
            Explain why the correct answer is right and briefly why others might be wrong if relevant.
            Respond strictly in the following language: $language.
        """.trimIndent()

        val optionsText = options.joinToString("\n") { "- $it" }
        val userPrompt = """
            Question: $question
            
            Options:
            $optionsText
            
            Correct Answer: $correctAnswer
        """.trimIndent()


        val requestBodyMap = mapOf(
            "model" to MODEL,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userPrompt)
            ),
            "temperature" to 0.7
        )

        val requestBodyJson = gson.toJson(requestBodyMap)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val groqResponse = gson.fromJson(responseBody, GroqResponse::class.java)
                groqResponse.choices.firstOrNull()?.message?.content?.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Keep
    private data class GroqResponse(
        val choices: List<Choice>
    )

    @Keep
    private data class Choice(
        val message: Message
    )

    @Keep
    private data class Message(
        val content: String
    )
}
