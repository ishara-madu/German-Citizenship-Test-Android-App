package com.pixeleye.einbuergerungstest.lebenindeutschland.data.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "questions")
data class QuestionEntity(
    @SerializedName("id") @PrimaryKey val id: Int,
    @SerializedName("questionText") val questionText: String,
    @SerializedName("optionA") val optionA: String,
    @SerializedName("optionB") val optionB: String,
    @SerializedName("optionC") val optionC: String,
    @SerializedName("optionD") val optionD: String,
    @SerializedName("correctAnswer") val correctAnswer: String,
    @SerializedName("imageResName") val imageResName: String? = null,
    @SerializedName("stateSpecific") val stateSpecific: String? = null,
    @SerializedName("category") val category: String = "General",
    @SerializedName("isBookmarked") val isBookmarked: Boolean = false,
    @SerializedName("isMistake") val isMistake: Boolean = false
)

