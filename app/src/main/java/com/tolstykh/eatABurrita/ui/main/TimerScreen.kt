package com.tolstykh.eatABurrita.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.TextMeasurer
import com.tolstykh.eatABurrita.ui.theme.CameraGreen
import com.tolstykh.eatABurrita.ui.theme.LightCameraGreen
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tolstykh.eatABurrita.dateFromMilliseconds
import com.tolstykh.eatABurrita.formatDuration
import com.tolstykh.eatABurrita.helpers.statusBarHeight
import com.tolstykh.eatABurrita.ui.share.BurritoShareCardDialog
import com.tolstykh.eatABurrita.location.hasLocationPermission
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import java.io.File
import java.time.Instant

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TimerScreen(
    viewModel: TimeScreenViewModel = hiltViewModel(),
    onOpenMap: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenRecipes: () -> Unit,
) {
    val uiState by viewModel.timeScreenState.collectAsStateWithLifecycle()
    val locationPickerOpen by viewModel.locationPickerOpen.collectAsStateWithLifecycle()
    val sizePickerOpen by viewModel.sizePickerOpen.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentUserLocation.collectAsStateWithLifecycle()
    val dayLocationModal by viewModel.dayLocationModal.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationPermissionAsked by viewModel.notificationPermissionAsked.collectAsStateWithLifecycle()
    val notificationPermissionState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    LaunchedEffect(notificationPermissionAsked) {
        if (!notificationPermissionAsked && !notificationPermissionState.status.isGranted) {
            notificationPermissionState.launchPermissionRequest()
            viewModel.markNotificationPermissionAsked()
        }
    }
    val verdictState by viewModel.verdictState.collectAsStateWithLifecycle()
    val celebrationState by viewModel.celebrationState.collectAsStateWithLifecycle()
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showCameraRationale by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = photoUri
        if (success && uri != null) {
            viewModel.classifyPhoto(uri)
        } else {
            photoUri = null
        }
    }
    val cleanupPhoto: () -> Unit = {
        photoUri?.let { uri ->
            runCatching { context.contentResolver.delete(uri, null, null) }
            photoUri = null
        }
    }
    val onCameraClick: () -> Unit = {
        when {
            cameraPermissionState.status.isGranted -> {
                val uri = createPhotoUri(context)
                photoUri = uri
                try {
                    takePictureLauncher.launch(uri)
                } catch (_: Exception) {
                    photoUri = null
                }
            }
            cameraPermissionState.status.shouldShowRationale -> showCameraRationale = true
            else -> cameraPermissionState.launchPermissionRequest()
        }
    }
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = context.hasLocationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) viewModel.refreshLocation()
    }

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
    var showShareCard by remember { mutableStateOf(false) }

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
                onTick = viewModel::onTimerTick,
            )
            TotalBurritos(modifier = Modifier.padding(8.dp), burritoCount = data.burritoCount)
            LastBurritoDate(modifier = Modifier.padding(8.dp), lastTimestamp = data.lastTimestamp)
            FavoritePlace(
                modifier = Modifier.padding(8.dp),
                placeName = data.favoritePlaceName,
                lat = data.favoritePlaceLat,
                lng = data.favoritePlaceLng,
            )
            Spacer(modifier = Modifier.height(16.dp))
            val todayFact = remember { BURRITO_FACTS[LocalDate.now().dayOfYear % BURRITO_FACTS.size] }
            BurritoFactCard(fact = todayFact)
            Spacer(modifier = Modifier.height(12.dp))
            BurritoConsumptionChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 24.dp),
                dailyCounts = data.dailyCounts,
                onDayClick = { dayIndex -> viewModel.onChartDayClicked(dayIndex) },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "See more stats →",
                color = colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { onOpenStats() }
                    .padding(vertical = 4.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            val cell = 80.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-20).dp),
            ) {
                // Row 1: Eat at center — zIndex(1f) so it draws above Map/Share when overlapping
                Row(modifier = Modifier.zIndex(1f)) {
                    Box(modifier = Modifier.size(cell))
                    Box(modifier = Modifier.size(cell), contentAlignment = Alignment.Center) {
                        EatButton(onClick = viewModel::requestAddBurrito)
                    }
                    Box(modifier = Modifier.size(cell))
                }
                // Row 2: Map at left, Share at right
                Row {
                    Box(modifier = Modifier.size(cell), contentAlignment = Alignment.Center) {
                        MapButton(onClick = onOpenMap)
                    }
                    Box(modifier = Modifier.size(cell))
                    Box(modifier = Modifier.size(cell), contentAlignment = Alignment.Center) {
                        ShareCardButton(onClick = { showShareCard = true })
                    }
                }
                // Row 3: Camera at center
                Row {
                    Box(modifier = Modifier.size(cell))
                    Box(modifier = Modifier.size(cell).offset(y = (-12).dp), contentAlignment = Alignment.Center) {
                        CameraButton(
                            onClick = onCameraClick,
                            enabled = verdictState is TimeScreenViewModel.BurritoVerdictState.None,
                        )
                    }
                    Box(modifier = Modifier.size(cell))
                }
            }
        }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = statusBarHeight() + 8.dp, end = 8.dp)
    ) {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colorScheme.onBackground)
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Map") },
                leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                onClick = { menuExpanded = false; onOpenMap() },
            )
            DropdownMenuItem(
                text = { Text("Stats") },
                leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                onClick = { menuExpanded = false; onOpenStats() },
            )
            DropdownMenuItem(
                text = { Text("Recipes") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                onClick = { menuExpanded = false; onOpenRecipes() },
            )
            DropdownMenuItem(
                text = { Text("Settings") },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                onClick = { menuExpanded = false; onOpenSettings() },
            )
        }
    }
        if (celebrationState is TimeScreenViewModel.CelebrationState.EmojiRain) {
            val rainKey = remember(celebrationState) { System.nanoTime().toString() }
            com.tolstykh.eatABurrita.ui.components.EmojiRainCanvas(
                modifier = Modifier.fillMaxSize(),
                key = rainKey,
            )
            LaunchedEffect(celebrationState) {
                delay(3500L)
                viewModel.dismissCelebration()
            }
        }
        if (celebrationState is TimeScreenViewModel.CelebrationState.StreakMilestone) {
            val milestoneState = celebrationState as TimeScreenViewModel.CelebrationState.StreakMilestone
            com.tolstykh.eatABurrita.ui.components.StreakMilestoneOverlay(
                days = milestoneState.days,
                onDismiss = viewModel::dismissCelebration,
            )
        }
    } // end Box

    if (locationPickerOpen) {
        LocationPickerModal(
            currentLocation = currentLocation,
            hasLocationPermission = hasLocationPermission,
            onConfirm = { name, lat, lng -> viewModel.confirmAddBurrito(name, lat, lng) },
            onCancel = { viewModel.cancelAddBurrito() },
            onDontShowAgain = { viewModel.disableLocationModal() },
        )
    }

    if (sizePickerOpen) {
        SizePickerModal(
            onConfirm = { kcal -> viewModel.onSizeConfirmed(kcal) },
            onSkip = { viewModel.onSizeSkipped() },
        )
    }

    dayLocationModal?.let { dayData ->
        DayLocationModal(
            data = dayData,
            onDismiss = { viewModel.dismissDayLocationModal() },
        )
    }

    if (verdictState !is TimeScreenViewModel.BurritoVerdictState.None) {
        BurritoVerdictDialog(
            verdictState = verdictState,
            photoUri = photoUri,
            onAddEntry = { cleanupPhoto(); viewModel.dismissVerdict(); viewModel.requestAddBurrito() },
            onDismiss = { cleanupPhoto(); viewModel.dismissVerdict() },
        )
    }

    if (showCameraRationale) {
        CameraRationaleDialog(
            onDismiss = { showCameraRationale = false },
            onAllow = {
                showCameraRationale = false
                cameraPermissionState.launchPermissionRequest()
            },
        )
    }

    if (showShareCard) {
        BurritoShareCardDialog(
            data = data,
            onDismiss = { showShareCard = false },
        )
    }
}

@Composable
fun BurritoFactCard(fact: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(
            visible = !expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Text(
                text = "See a random burrito fact →",
                color = colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(vertical = 4.dp),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.primaryContainer,
                tonalElevation = 2.dp,
                onClick = { expanded = false },
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Burrito Fact",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = fact,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BurritoConsumptionChart(
    modifier: Modifier = Modifier,
    dailyCounts: List<Int>,
    onDayClick: ((Int) -> Unit)? = null,
    textMeasurer: TextMeasurer? = null,
) {
    val primaryColor = colorScheme.primary
    val barBackground = colorScheme.surfaceVariant
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(
                    if (onDayClick != null || textMeasurer != null) {
                        Modifier.pointerInput(dailyCounts) {
                            detectTapGestures { offset ->
                                val barCount = dailyCounts.size
                                if (barCount == 0) return@detectTapGestures
                                val gap = 2.dp.toPx()
                                val barWidth = (size.width - gap * (barCount - 1)) / barCount
                                val rawIndex = (offset.x / (barWidth + gap)).toInt()
                                val clampedIndex = rawIndex.coerceIn(0, barCount - 1)
                                if (textMeasurer != null) {
                                    selectedIndex = if (selectedIndex == clampedIndex) null else clampedIndex
                                }
                                if (dailyCounts[clampedIndex] > 0) {
                                    onDayClick?.invoke(clampedIndex)
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

            if (textMeasurer != null) {
                selectedIndex?.let { idx ->
                    val count = dailyCounts[idx]
                    val left = idx * (barWidth + gap)
                    val barHeight = (count.toFloat() / maxCount) * size.height
                    val barCenter = left + barWidth / 2f
                    val radius = 18.dp.toPx()
                    val cy = (size.height - barHeight - radius - 4.dp.toPx()).coerceAtLeast(radius)
                    val alpha = 0.4f + 0.6f * idx / (barCount - 1).coerceAtLeast(1)
                    drawCircle(color = primaryColor.copy(alpha = alpha), radius = radius, center = Offset(barCenter, cy))
                    val layout = textMeasurer.measure(
                        text = count.toString(),
                        style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    )
                    drawText(layout, topLeft = Offset(barCenter - layout.size.width / 2f, cy - layout.size.height / 2f))
                }
            }
        }
    }
}

@Composable
fun TimeSinceLastBurrito(
    modifier: Modifier = Modifier,
    lastTimestamp: Long = 0L,
    onTick: () -> Unit = {},
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
            onTick()
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
        modifier = Modifier.requiredSize(110.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary
        )
    ) {
        Text(text = "Eat!", fontSize = 22.sp)
    }
}

@Composable
fun ShareCardButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
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
fun FavoritePlace(modifier: Modifier = Modifier, placeName: String?, lat: Double?, lng: Double?) {
    if (placeName == null) return
    val context = LocalContext.current
    val canNavigate = lat != null && lng != null
    Row(modifier = modifier) {
        Text(
            text = "Favorite place: ",
            fontSize = 18.sp,
            lineHeight = 22.sp,
        )
        Text(
            text = placeName,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            color = colorScheme.tertiary,
            modifier = if (canNavigate) Modifier.clickable {
                val uri = "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(placeName)})".toUri()
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } else Modifier,
        )
    }
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

@Composable
private fun CameraButton(onClick: () -> Unit, enabled: Boolean = true) {
    val darkTheme = isSystemInDarkTheme()
    val green = if (darkTheme) LightCameraGreen else CameraGreen
    val iconTint = if (darkTheme) Color.Black else Color.White
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = Modifier.size(70.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = green,
            contentColor = iconTint,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Scan burrito",
        )
    }
}

@Composable
private fun CameraRationaleDialog(onDismiss: () -> Unit, onAllow: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Camera needed", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "The camera is used to verify your burrito before logging it.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Not now") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAllow) { Text("Allow") }
                }
            }
        }
    }
}

private fun createPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "burrito_photos").also { it.mkdirs() }
    val file = File(dir, "classify_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
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
