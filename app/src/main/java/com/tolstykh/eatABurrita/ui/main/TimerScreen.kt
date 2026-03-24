package com.tolstykh.eatABurrita.ui.main

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tolstykh.eatABurrita.dateFromMilliseconds
import com.tolstykh.eatABurrita.formatDuration
import com.tolstykh.eatABurrita.helpers.getRandomMessageWithStats
import com.tolstykh.eatABurrita.helpers.statusBarHeight
import com.tolstykh.eatABurrita.location.hasLocationPermission
import kotlinx.coroutines.delay
import java.time.Instant

@Composable
fun TimerScreen(
    viewModel: TimeScreenViewModel = hiltViewModel(),
    onOpenMap: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val uiState by viewModel.timeScreenState.collectAsStateWithLifecycle()
    val locationPickerOpen by viewModel.locationPickerOpen.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentUserLocation.collectAsStateWithLifecycle()
    val dayLocationModal by viewModel.dayLocationModal.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState is TimeScreenViewModel.TimeScreenUIState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState is TimeScreenViewModel.TimeScreenUIState.Error) {
        return
    }

    val data = (uiState as TimeScreenViewModel.TimeScreenUIState.Success).data

    Box(modifier = Modifier.fillMaxSize()) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(vertical = 24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            AppTitle()
            TimeSinceLastBurrito(
                modifier = Modifier.padding(top = 32.dp),
                lastTimestamp = data.lastTimestamp,
            )
            TotalBurritos(modifier = Modifier.padding(8.dp), burritoCount = data.burritoCount)
            LastBurritoDate(modifier = Modifier.padding(8.dp), lastTimestamp = data.lastTimestamp)
            FavoritePlace(modifier = Modifier.padding(8.dp), placeName = data.favoritePlaceName)
            Spacer(modifier = Modifier.height(24.dp))
            BurritoConsumptionChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 24.dp),
                dailyCounts = data.dailyCounts,
                onDayClick = { dayIndex -> viewModel.onChartDayClicked(dayIndex) },
            )
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                MapButton(onClick = onOpenMap)
                EatButton(
                    onClick = viewModel::requestAddBurrito
                )
                Share(
                    context = LocalContext.current,
                    burritoCount = data.burritoCount,
                    lastTimestamp = data.lastTimestamp,
                    dailyCounts = data.dailyCounts,
                )
            }
        }
    }
    IconButton(
        onClick = onOpenSettings,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = statusBarHeight() + 8.dp, end = 8.dp)
    ) {
        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = colorScheme.onBackground)
    }
    } // end Box

    if (locationPickerOpen) {
        LocationPickerModal(
            currentLocation = currentLocation,
            hasLocationPermission = context.hasLocationPermission(),
            onConfirm = { name, lat, lng -> viewModel.confirmAddBurrito(name, lat, lng) },
            onCancel = { viewModel.cancelAddBurrito() },
            onDontShowAgain = { viewModel.disableLocationModal() },
        )
    }

    dayLocationModal?.let { dayData ->
        DayLocationModal(
            data = dayData,
            onDismiss = { viewModel.dismissDayLocationModal() },
        )
    }
}

@Composable
fun BurritoConsumptionChart(
    modifier: Modifier = Modifier,
    dailyCounts: List<Int>,
    onDayClick: ((Int) -> Unit)? = null,
) {
    val primaryColor = colorScheme.primary
    val barBackground = colorScheme.surfaceVariant

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(
                    if (onDayClick != null) {
                        Modifier.pointerInput(dailyCounts) {
                            detectTapGestures { offset ->
                                val barCount = dailyCounts.size
                                if (barCount == 0) return@detectTapGestures
                                val gap = 2.dp.toPx()
                                val barWidth = (size.width - gap * (barCount - 1)) / barCount
                                val rawIndex = (offset.x / (barWidth + gap)).toInt()
                                val clampedIndex = rawIndex.coerceIn(0, barCount - 1)
                                if (dailyCounts[clampedIndex] > 0) {
                                    onDayClick(clampedIndex)
                                }
                            }
                        }
                    } else Modifier
                )
        ) {
            val maxCount = dailyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
            val barCount = dailyCounts.size
            val gap = 2.dp.toPx()
            val barWidth = (size.width - gap * (barCount - 1)) / barCount

            dailyCounts.forEachIndexed { index, count ->
                val left = index * (barWidth + gap)
                val barHeight = (count.toFloat() / maxCount) * size.height

                drawRoundRect(
                    color = barBackground,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                )

                if (count > 0) {
                    val alpha = 0.4f + 0.6f * index / (barCount - 1).coerceAtLeast(1)
                    drawRoundRect(
                        color = primaryColor.copy(alpha = alpha),
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                    )
                }
            }
        }
        Text(
            text = "Last 30 days",
            fontSize = 11.sp,
            color = colorScheme.onSurface.copy(alpha = 0.45f),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
fun TimeSinceLastBurrito(
    modifier: Modifier = Modifier,
    lastTimestamp: Long = 0L,
) {
    var now by remember(lastTimestamp) { mutableLongStateOf(Instant.now().toEpochMilli()) }

    if (lastTimestamp == 0L) {
        Text(
            text = "0 days, 00:00:00",
            fontSize = 30.sp,
            lineHeight = 34.sp,
            modifier = modifier
        )
        return
    }

    LaunchedEffect(lastTimestamp) {
        while (true) {
            delay(1000L)
            now = Instant.now().toEpochMilli()
        }
    }

    Text(
        text = formatDuration(now - lastTimestamp),
        fontSize = 30.sp,
        lineHeight = 34.sp,
        modifier = modifier
    )
}

@Composable
fun AppTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Time Since Last Burrito",
        textAlign = TextAlign.Center,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        modifier = modifier
    )
}

@Composable
fun EatButton(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        shape = CircleShape,
        modifier = Modifier
            .size(144.dp)
            .padding(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary
        )
    ) {
        Text(text = "Eat!", fontSize = 24.sp)
    }
}

@Composable
fun Share(
    context: Context,
    burritoCount: Int = 0,
    lastTimestamp: Long = 0L,
    dailyCounts: List<Int> = emptyList(),
) {
    Button(
        onClick = {
            val text = getRandomMessageWithStats(burritoCount, lastTimestamp, dailyCounts)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)

            context.startActivity(shareIntent, null)
        },
        shape = CircleShape,
        modifier = Modifier.size(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.secondary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
        )
    }
}

@Composable
fun TotalBurritos(modifier: Modifier = Modifier, burritoCount: Int = 0) {
    Text(
        text = "Total burritos: $burritoCount",
        fontSize = 20.sp,
        lineHeight = 24.sp,
        modifier = modifier,
    )
}

@Composable
fun LastBurritoDate(modifier: Modifier = Modifier, lastTimestamp: Long = 0L) {
    if (lastTimestamp == 0L) {
        Text(
            text = "It's time to eat a burrito!",
            fontSize = 18.sp,
            lineHeight = 22.sp,
            modifier = modifier,
        )
        return
    }

    Text(
        text = "Last burrito: ${dateFromMilliseconds(lastTimestamp)}",
        fontSize = 18.sp,
        lineHeight = 22.sp,
        modifier = modifier,
    )
}

@Composable
fun FavoritePlace(modifier: Modifier = Modifier, placeName: String?) {
    if (placeName == null) return
    Text(
        text = "Favorite place: $placeName",
        fontSize = 18.sp,
        lineHeight = 22.sp,
        modifier = modifier,
    )
}

@Composable
fun MapButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.tertiary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = null,
        )
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun BurritoConsumptionChartPreview() {
    val fakeData = listOf(
        2, 1, 0, 3, 2, 1, 0, 2, 1, 4,
        2, 0, 1, 3, 2, 1, 0, 2, 1, 3,
        2, 1, 0, 1, 2, 0, 3, 2, 1, 2,
    )
    BurritoConsumptionChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 24.dp),
        dailyCounts = fakeData,
    )
}
