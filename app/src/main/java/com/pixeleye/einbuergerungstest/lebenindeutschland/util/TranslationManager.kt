package com.pixeleye.einbuergerungstest.lebenindeutschland.util

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationManager @Inject constructor() {

    private var currentTranslator: com.google.mlkit.nl.translate.Translator? = null
    private var currentTargetLang: String? = null

    private fun getTranslator(targetLanguage: String): com.google.mlkit.nl.translate.Translator {
        val mlKitLang = mapToMlKitLanguage(targetLanguage)
        
        if (currentTranslator != null && currentTargetLang == targetLanguage) {
            return currentTranslator!!
        }

        currentTranslator?.close()
        
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.GERMAN)
            .setTargetLanguage(mlKitLang)
            .build()
        
        currentTargetLang = targetLanguage
        currentTranslator = Translation.getClient(options)
        return currentTranslator!!
    }

    private fun mapToMlKitLanguage(language: String): String {
        return when (language.lowercase()) {
            "english" -> TranslateLanguage.ENGLISH
            "german" -> TranslateLanguage.ENGLISH // As requested: German -> English
            "turkish" -> TranslateLanguage.TURKISH
            "arabic" -> TranslateLanguage.ARABIC
            "persian" -> TranslateLanguage.PERSIAN
            else -> TranslateLanguage.ENGLISH
        }
    }

    suspend fun isModelDownloaded(targetLanguage: String): Boolean {
        val mlKitLang = mapToMlKitLanguage(targetLanguage)
        val modelManager = RemoteModelManager.getInstance()
        
        val germanModel = TranslateRemoteModel.Builder(TranslateLanguage.GERMAN).build()
        val targetModel = TranslateRemoteModel.Builder(mlKitLang).build()
        
        return try {
            val germanReady = modelManager.isModelDownloaded(germanModel).await()
            val targetReady = modelManager.isModelDownloaded(targetModel).await()
            germanReady && targetReady
        } catch (e: Exception) {
            false
        }
    }


    suspend fun downloadModelIfNeeded(targetLanguage: String): Boolean {
        return try {
            val mlKitLang = mapToMlKitLanguage(targetLanguage)
            val modelManager = RemoteModelManager.getInstance()
            val conditions = DownloadConditions.Builder().build()

            // Ensure German (Source) is available
            val germanModel = TranslateRemoteModel.Builder(TranslateLanguage.GERMAN).build()
            modelManager.download(germanModel, conditions).await()

            // Ensure Target is available
            val targetModel = TranslateRemoteModel.Builder(mlKitLang).build()
            modelManager.download(targetModel, conditions).await()

            true
        } catch (e: Exception) {
            false
        }
    }



    suspend fun translate(text: String, targetLanguage: String): String {
        if (targetLanguage.lowercase() == "german") {
            // If they want German translation of German content, we could just return original,
            // but the user specifically said: "app language english or germen when click on translate button translate to english"
            // So we use English as target. mapToMlKitLanguage already handles this.
        }

        return try {
            getTranslator(targetLanguage).translate(text).await()
        } catch (e: Exception) {
            text // Fallback to original
        }
    }
    
    fun close() {
        currentTranslator?.close()
    }
}

