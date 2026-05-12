package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SnackbarType {
    SUCCESS, ERROR, INFO, WARNING
}

data class SnackbarMessage(
    val title: String = "",
    val message: String = "",
    val messageResId: Int? = null,
    val type: SnackbarType,
    val id: Long = System.currentTimeMillis()
)

object SnackbarManager {
    private val _message = MutableStateFlow<SnackbarMessage?>(null)
    val message: StateFlow<SnackbarMessage?> = _message.asStateFlow()

    fun showMessage(title: String = "", message: String = "", messageResId: Int? = null, type: SnackbarType = SnackbarType.INFO) {
        _message.value = SnackbarMessage(title, message, messageResId, type)
    }

    fun showSuccess(title: String = "", message: String = "", messageResId: Int? = null) {
        showMessage(title, message, messageResId, SnackbarType.SUCCESS)
    }

    fun showError(title: String = "", message: String = "", messageResId: Int? = null) {
        showMessage(title, message, messageResId, SnackbarType.ERROR)
    }

    fun showInfo(title: String = "", message: String = "", messageResId: Int? = null) {
        showMessage(title, message, messageResId, SnackbarType.INFO)
    }

    fun showWarning(title: String = "", message: String = "", messageResId: Int? = null) {
        showMessage(title, message, messageResId, SnackbarType.WARNING)
    }

    fun dismiss() {
        _message.value = null
    }
}

@Composable
fun GlobalSnackbarOverlay(modifier: Modifier = Modifier) {
    val message by SnackbarManager.message.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                initialOffsetY = { -it - 50 },
                animationSpec = tween(durationMillis = 400)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(
                targetOffsetY = { -it - 50 },
                animationSpec = tween(durationMillis = 350)
            ) + fadeOut(animationSpec = tween(durationMillis = 250))
        ) {
            message?.let { msg ->
                // Auto dismiss timer
                LaunchedEffect(msg.id) {
                    delay(4000)
                    SnackbarManager.dismiss()
                }

                val containerColor = when (msg.type) {
                    SnackbarType.SUCCESS -> Color(0xFF059669) // Emerald
                    SnackbarType.ERROR -> Color(0xFFDC2626) // Crimson
                    SnackbarType.INFO -> Color(0xFF2563EB) // Royal Blue
                    SnackbarType.WARNING -> Color(0xFFD97706) // Amber
                }

                val icon: ImageVector = when (msg.type) {
                    SnackbarType.SUCCESS -> Icons.Rounded.CheckCircle
                    SnackbarType.ERROR -> Icons.Rounded.Error
                    SnackbarType.INFO -> Icons.Rounded.Info
                    SnackbarType.WARNING -> Icons.Rounded.Warning
                }

                val localizedTitle = when (msg.type) {
                    SnackbarType.SUCCESS -> androidx.compose.ui.res.stringResource(id = com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.snackbar_success_title)
                    SnackbarType.ERROR -> androidx.compose.ui.res.stringResource(id = com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.snackbar_error_title)
                    SnackbarType.INFO -> androidx.compose.ui.res.stringResource(id = com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.snackbar_info_title)
                    SnackbarType.WARNING -> androidx.compose.ui.res.stringResource(id = com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.snackbar_warning_title)
                }
                val finalTitle = msg.title.takeIf { it.isNotEmpty() } ?: localizedTitle

                val finalMessage = msg.messageResId?.let { 
                    androidx.compose.ui.res.stringResource(id = it) 
                } ?: msg.message

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = containerColor.copy(alpha = 0.5f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = containerColor
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = finalTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = finalMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = { SnackbarManager.dismiss() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Schließen",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
