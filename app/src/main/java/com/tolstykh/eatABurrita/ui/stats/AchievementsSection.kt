package com.tolstykh.eatABurrita.ui.stats

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AchievementsSection(achievements: List<Achievement>) {
    var previewing by remember { mutableStateOf<Achievement?>(null) }

    previewing?.let { achievement ->
        AchievementUnlockedDialog(
            achievements = listOf(achievement),
            onDismiss = { previewing = null },
        )
    }

    Text(
        "Achievements",
        style = MaterialTheme.typography.titleMedium,
        color = colorScheme.primary,
    )
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

    val rows = achievements.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        modifier = Modifier.weight(1f),
                        onClick = { previewing = achievement },
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val isUnlocked = achievement.isUnlocked
    val cardAlpha = if (isUnlocked) 1f else 0.45f
    val bgColor = if (isUnlocked) colorScheme.primaryContainer else colorScheme.surfaceVariant
    val border = if (isUnlocked) BorderStroke(1.5.dp, colorScheme.primary) else null

    Surface(
        modifier = if (isUnlocked) modifier.clickable(onClick = onClick) else modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor.copy(alpha = cardAlpha),
        tonalElevation = if (isUnlocked) 4.dp else 1.dp,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(achievement.emoji, fontSize = 28.sp)
                if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd),
                        tint = colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }
            Text(
                text = achievement.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isUnlocked) colorScheme.onPrimaryContainer
                else colorScheme.onSurface.copy(alpha = cardAlpha),
            )
            LinearProgressIndicator(
                progress = { achievement.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (isUnlocked) colorScheme.primary else colorScheme.outline,
                trackColor = colorScheme.surfaceVariant,
            )
            Text(
                text = "${achievement.currentValue} / ${achievement.target}",
                fontSize = 9.sp,
                color = colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}
