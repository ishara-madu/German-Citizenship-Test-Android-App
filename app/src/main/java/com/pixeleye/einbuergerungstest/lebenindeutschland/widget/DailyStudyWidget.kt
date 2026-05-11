package com.pixeleye.einbuergerungstest.lebenindeutschland.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.pixeleye.einbuergerungstest.lebenindeutschland.MainActivity
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.AppDatabase
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.PreferenceManager
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.QuestionEntity
import androidx.glance.appwidget.CircularProgressIndicator
import java.util.Locale

class DailyStudyWidget : GlanceAppWidget() {

    companion object {
        private val MEDIUM = DpSize(250.dp, 100.dp)
        private val LARGE = DpSize(250.dp, 250.dp)
    }

    override val sizeMode = SizeMode.Responsive(setOf(MEDIUM, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val pref = PreferenceManager(context)
        
        val appLanguage = pref.getAppLanguage()
        val locale = when (appLanguage) {
            "English" -> Locale("en")
            "Turkish" -> Locale("tr")
            "Arabic" -> Locale("ar")
            "Persian" -> Locale("fa")
            else -> Locale.GERMAN
        }

        // Force selected language for the widget content
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        
        val onboardingCompleted = pref.isOnboardingCompleted()
        val dataInitialized = pref.isDataInitialized()
        
        if (!dataInitialized || !onboardingCompleted) {
            provideContent {
                LoadingState(localizedContext, !dataInitialized)
            }
            return
        }

        val selectedState = pref.getSelectedState() ?: "Bavaria"
        val generalQuestions = db.questionDao().getGeneralQuestionsOnce()
        val stateQuestions = db.questionDao().getQuestionsByState(selectedState)
        val allQuestions = (generalQuestions + stateQuestions).filter { it.imageResName.isNullOrBlank() }
        
        val streak = pref.getCurrentStreak()
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val dailyQuestion = if (allQuestions.isNotEmpty()) {
            allQuestions[dayOfYear % allQuestions.size]
        } else null
        
        val masteredIds = pref.getMasteredQuestionIds()
        val masteredCount = allQuestions.count { it.id.toString() in masteredIds }
        val progress = if (allQuestions.isNotEmpty()) masteredCount.toFloat() / allQuestions.size else 0f
        val levelNumber = ((progress * 9).toInt() + 1).coerceIn(1, 10)
        
        val levelResId = when(levelNumber) {
            1 -> R.string.level_beginner
            2 -> R.string.level_novice
            3 -> R.string.profile_learner
            4 -> R.string.level_student
            5 -> R.string.level_researcher
            6 -> R.string.level_expert
            7 -> R.string.level_advanced
            8 -> R.string.level_scholar
            9 -> R.string.level_master
            else -> R.string.level_citizen
        }
        val levelTitle = localizedContext.getString(levelResId)

        provideContent {
            val size = LocalSize.current
            WidgetMainContent(size, localizedContext, streak, levelTitle, dailyQuestion)
        }
    }

    @Composable
    private fun LoadingState(context: Context, isInitialDataLoad: Boolean) {
        val action = actionStartActivity<MainActivity>()
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(ImageProvider(R.drawable.widget_premium_bg))
                .padding(16.dp)
                .clickable(action),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = ColorProvider(Color(0xFF6366F1))
                )
                Spacer(modifier = GlanceModifier.height(12.dp))
                Text(
                    text = if (isInitialDataLoad) "Wird geladen..." else context.getString(R.string.widget_setup_message),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF1E293B)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }

    @Composable
    private fun WidgetMainContent(
        size: DpSize,
        context: Context,
        streak: Int,
        levelTitle: String,
        question: QuestionEntity?
    ) {
        val action = actionStartActivity<MainActivity>()
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(ImageProvider(R.drawable.widget_premium_bg))
                .padding(16.dp)
                .clickable(action)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Header Row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Row(
                        modifier = GlanceModifier.defaultWeight(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.fire),
                            contentDescription = null,
                            modifier = GlanceModifier.size(20.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = if (streak > 0) context.getString(R.string.widget_streak_format, streak) else context.getString(R.string.widget_start_studying),
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF1E293B)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    
                    Box(
                        modifier = GlanceModifier.defaultWeight(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = levelTitle,
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF6366F1)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(16.dp))
                
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = context.getString(R.string.widget_question_of_day),
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF64748B)),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = question?.questionText ?: "Daten werden geladen...",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF0F172A)),
                            fontSize = if (size.height >= LARGE.height) 24.sp else 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    if (question != null) {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        val correctAnswerText = when (question.correctAnswer) {
                            "A" -> question.optionA
                            "B" -> question.optionB
                            "C" -> question.optionC
                            "D" -> question.optionD
                            else -> question.correctAnswer
                        }
                        
                        Text(
                            text = correctAnswerText,
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF10B981)),
                                fontSize = if (size.height >= LARGE.height) 18.sp else 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())
                
                Text(
                    text = context.getString(R.string.widget_tap_to_open),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF94A3B8)),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier.fillMaxWidth()
                )
            }
        }
    }
}
