package com.pixeleye.einbuergerungstest.lebenindeutschland.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.pixeleye.einbuergerungstest.lebenindeutschland.MainActivity
import androidx.glance.appwidget.appWidgetBackground
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DailyStudyWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val action = actionStartActivity<MainActivity>()
            
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(ImageProvider(R.drawable.widget_premium_bg))
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    // Top Section: Gamified Streak
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.fire),
                            contentDescription = null,
                            modifier = GlanceModifier.size(28.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = "15 Day Streak!",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF1E293B)),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily("Poppins")
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Progress Bar
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .height(8.dp)
                                .background(Color(0xFF10B981)),
                            content = {}
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Middle Section: The Question
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = "QUESTION OF THE DAY",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF64748B)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily("Poppins")
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "What is the capital of Germany?",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF0F172A)),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily("Poppins")
                            ),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Bottom Section: Action Button
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(ImageProvider(R.drawable.widget_button_bg))
                            .padding(vertical = 12.dp)
                            .clickable(action),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Answer in App",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily("Poppins")
                            )
                        )
                    }
                }
            }
        }
    }
}
