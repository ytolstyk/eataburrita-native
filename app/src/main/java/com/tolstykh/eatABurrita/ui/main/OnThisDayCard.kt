package com.tolstykh.eatABurrita.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun OnThisDayCard(
    data: OnThisDayData,
    onDismiss: () -> Unit,
    onShowWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.tertiaryContainer,
        tonalElevation = 2.dp,
        onClick = onShowWeek,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🕰️ On This Day, ${data.yearAgoDate.year}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onTertiaryContainer,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onTertiaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to see that week →",
                    style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                    color = colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
fun OnThisDayWeekModal(
    data: OnThisDayData,
    onDismiss: () -> Unit,
) {
    val entriesByDay = remember(data.weekEntries) {
        val zone = ZoneId.systemDefault()
        data.weekEntries.groupBy {
            Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate()
        }
    }
    val monthLabel = remember(data.yearAgoDate) {
        data.yearAgoDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }
    val dayFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp).heightIn(max = 520.dp)) {
                Text(
                    text = "This week in $monthLabel ${data.yearAgoDate.year}",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "${data.weekCount} burrito${if (data.weekCount == 1) "" else "s"} that week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                    itemsIndexed(data.weekDays) { _, day ->
                        val dayEntries = entriesByDay[day] ?: emptyList()
                        OnThisDayDayRow(
                            day = day,
                            dayFormatter = dayFormatter,
                            entries = dayEntries,
                            isHighlighted = day == data.yearAgoDate,
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = colorScheme.outlineVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun OnThisDayDayRow(
    day: LocalDate,
    dayFormatter: DateTimeFormatter,
    entries: List<WeekEntryUi>,
    isHighlighted: Boolean,
) {
    val bgColor = if (isHighlighted) colorScheme.primaryContainer else Color.Transparent
    Surface(color = bgColor, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.format(dayFormatter),
                    style = if (isHighlighted)
                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.bodyMedium,
                    color = if (isHighlighted) colorScheme.onPrimaryContainer else colorScheme.onSurface,
                )
                entries.forEach { entry ->
                    Text(
                        text = "🌯${entry.locationName?.let { " $it" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isHighlighted) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (entries.isNotEmpty()) {
                Text(
                    text = "${entries.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isHighlighted) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
