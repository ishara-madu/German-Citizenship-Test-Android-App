package com.pixeleye.einbuergerungstest.lebenindeutschland.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

object QuestionJsonParser {
    suspend fun parseQuestionsFromJson(context: Context): List<QuestionEntity> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("questions.json")
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<QuestionEntity>>() {}.type
                Gson().fromJson(reader, type)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
