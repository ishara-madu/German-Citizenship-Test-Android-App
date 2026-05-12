package com.pixeleye.einbuergerungstest.lebenindeutschland.data.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val imageResName: String? = null,
    val stateSpecific: String? = null,
    val category: String = "General",
    val isBookmarked: Boolean = false,
    val isMistake: Boolean = false
)

