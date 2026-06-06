package com.tolstykh.eatABurrita.ui.stats

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tolstykh.eatABurrita.helpers.getAchievementShareMessage
import com.tolstykh.eatABurrita.ui.components.ConfettiCanvas

@Composable
fun AchievementUnlockedDialog(
    achievements: List<Achievement>,
    onDismiss: () -> Unit,
) {
    if (achievements.isEmpty()) return

    var currentIndex by remember(achievements) { mutableIntStateOf(0) }
    val achievement = achievements[currentIndex]
    val hasNext = currentIndex < achievements.lastIndex
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
            modifier = Modifier
                .width(300.dp)
                .wrapContentHeight(),
        ) {
            Box {
                // Content
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(achievement.emoji, fontSize = 48.sp)
                    Text(
                        text = "Achievement Unlocked!",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = achievement.name,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = colorScheme.onSurface.copy(alpha = 0.65f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = {
                            val text = getAchievementShareMessage(
                                achievement.emoji, achievement.name, achievement.description,
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, null))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share achievement")
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onDismiss) { Text("Close") }
                        if (hasNext) {
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { currentIndex++ }) {
                                val remaining = achievements.size - currentIndex - 1
                                Text("Next ($remaining more)")
                            }
                        }
                    }
                }
                // Confetti overlays the full dialog, falling from top to bottom
                ConfettiCanvas(
                    modifier = Modifier.matchParentSize(),
                    key = achievement.id,
                )
            }
        }
    }
}

