package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.ui.theme.VartaPressRed
import com.example.ui.theme.VartaSaffron

import androidx.compose.material3.OutlinedButton
import com.example.util.NewsNotificationManager

@Composable
fun NotificationSettingsDialog(
    isEnabled: Boolean,
    isDarkMode: Boolean,
    language: AppLanguage = AppLanguage.ENGLISH,
    onToggle: (Boolean) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: (() -> Unit)? = null,
    onSendTest: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isSystemEnabled = remember(isEnabled) { NewsNotificationManager.areSystemNotificationsEnabled(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggle(true)
        } else {
            NewsNotificationManager.openSystemNotificationSettings(context)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("notification_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (isEnabled) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "प्राथमिकताएं एवं अलर्ट" else "PRESS PREFERENCES & ALERTS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Language Selection Section (English vs Hindi)
                if (onToggleLanguage != null) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "भाषा / LANGUAGE" else "LANGUAGE / भाषा",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // English Option
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = if (language == AppLanguage.ENGLISH) VartaSaffron.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (language == AppLanguage.ENGLISH) 1.5.dp else 0.8.dp,
                                color = if (language == AppLanguage.ENGLISH) VartaSaffron else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { if (language != AppLanguage.ENGLISH) onToggleLanguage() }
                                .testTag("language_english_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "English",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (language == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    ),
                                    color = if (language == AppLanguage.ENGLISH) VartaSaffron else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Hindi Option
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = if (language == AppLanguage.HINDI) VartaSaffron.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (language == AppLanguage.HINDI) 1.5.dp else 0.8.dp,
                                color = if (language == AppLanguage.HINDI) VartaSaffron else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { if (language != AppLanguage.HINDI) onToggleLanguage() }
                                .testTag("language_hindi_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "हिन्दी (Hindi)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (language == AppLanguage.HINDI) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    ),
                                    color = if (language == AppLanguage.HINDI) VartaSaffron else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Theme Selection Options (Light vs Dark)
                Text(
                    text = "EDITION DISPLAY THEME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Light Day Broadsheet Option
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = if (!isDarkMode) VartaSaffron.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (!isDarkMode) 1.5.dp else 0.8.dp,
                            color = if (!isDarkMode) VartaSaffron else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (isDarkMode) onToggleTheme() }
                            .testTag("theme_light_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "☀️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Day Broadsheet (Light)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (!isDarkMode) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (!isDarkMode) VartaSaffron else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Dark Midnight Press Option
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = if (isDarkMode) VartaSaffron.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isDarkMode) 1.5.dp else 0.8.dp,
                            color = if (isDarkMode) VartaSaffron else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isDarkMode) onToggleTheme() }
                            .testTag("theme_dark_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🌙", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Midnight Press (Dark)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isDarkMode) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (isDarkMode) VartaSaffron else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Important News Notification Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Important & Breaking News Only",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Only urgent lead stories and critical national dispatches with high-resolution imagery will notify you. Regular wire feeds stay silent.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isEnabled && isSystemEnabled,
                        onCheckedChange = { willEnable ->
                            if (willEnable) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else if (!isSystemEnabled) {
                                    NewsNotificationManager.openSystemNotificationSettings(context)
                                }
                            }
                            onToggle(willEnable)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VartaSaffron,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("important_notifications_switch")
                    )
                }

                if (!isSystemEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, VartaPressRed.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                            .background(VartaPressRed.copy(alpha = 0.08f))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "System notifications disabled in Android Settings.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { NewsNotificationManager.openSystemNotificationSettings(context) },
                                shape = RoundedCornerShape(2.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = "SETTINGS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = VartaSaffron
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rich Image Preview Notice Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "❖ RICH EDITORIAL NOTIFICATIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = VartaPressRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Includes direct photo journalism expander, publication masthead source, and instant one-tap reader navigation.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            onSendTest()
                        },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEnabled) VartaSaffron else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isEnabled) Color(0xFF131211) else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_test_notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SEND SAMPLE BREAKING NEWS ALERT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
