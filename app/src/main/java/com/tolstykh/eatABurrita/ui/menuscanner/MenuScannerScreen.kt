package com.tolstykh.eatABurrita.ui.menuscanner

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.tolstykh.eatABurrita.BuildConfig
import com.tolstykh.eatABurrita.data.MenuBurritoItem
import com.tolstykh.eatABurrita.ui.theme.CameraGreen
import com.tolstykh.eatABurrita.ui.theme.LightCameraGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.File

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MenuScannerScreen(
    viewModel: MenuScannerViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose { viewModel.cleanUpTempFiles() }
    }

    var showCameraRationale by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var pendingFile by remember { mutableStateOf<File?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingFile
        if (success && file != null) {
            viewModel.onPageAdded(file)
        } else {
            file?.delete()
        }
        pendingFile = null
    }

    val onAddPage: () -> Unit = {
        when {
            cameraPermissionState.status.isGranted -> {
                val file = viewModel.createMenuScanFile()
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                pendingFile = file
                try {
                    takePictureLauncher.launch(uri)
                } catch (_: Exception) {
                    file.delete()
                    pendingFile = null
                }
            }
            cameraPermissionState.status.shouldShowRationale -> showCameraRationale = true
            else -> cameraPermissionState.launchPermissionRequest()
        }
    }

    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(paddingValues),
        ) {
            when (val state = uiState) {
                is MenuScannerUiState.Empty -> {
                    EmptyStateContent(onAddPage = onAddPage)
                }

                is MenuScannerUiState.HasPages -> {
                    HasPagesContent(
                        pages = state.pages,
                        isScanning = false,
                        onAddPage = onAddPage,
                        onScan = { viewModel.startScan() },
                        onStartOver = { viewModel.startOver() },
                    )
                }

                is MenuScannerUiState.Scanning -> {
                    HasPagesContent(
                        pages = state.pages,
                        isScanning = true,
                        onAddPage = {},
                        onScan = {},
                        onStartOver = {},
                    )
                }

                is MenuScannerUiState.HasResults -> {
                    ResultsContent(
                        state = state,
                        onAddPage = onAddPage,
                        onScan = { viewModel.startScan() },
                        onStartOver = { viewModel.startOver() },
                        onSave = { showSaveDialog = true },
                    )
                }
            }
        }
    }

    if (showCameraRationale) {
        AlertDialog(
            onDismissRequest = { showCameraRationale = false },
            title = { Text("Camera needed") },
            text = { Text("Camera access is required to scan menu pages.") },
            confirmButton = {
                TextButton(onClick = {
                    showCameraRationale = false
                    cameraPermissionState.launchPermissionRequest()
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = { showCameraRationale = false }) { Text("Cancel") }
            },
        )
    }

    if (showSaveDialog) {
        SaveDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                viewModel.saveScan(name)
                showSaveDialog = false
            },
        )
    }
}

@Composable
private fun EmptyStateContent(onAddPage: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                text = "No menu? No intel.",
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Point your camera at a menu and we'll find every burrito hiding in there.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.55f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
            Spacer(Modifier.height(36.dp))
            ScanCameraButton(onClick = onAddPage)
        }
    }
}

@Composable
private fun HasPagesContent(
    pages: List<File>,
    isScanning: Boolean,
    onAddPage: () -> Unit,
    onScan: () -> Unit,
    onStartOver: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "${pages.size} / 4 pages",
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Box {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isScanning) 0.4f else 1f),
            ) {
                items(pages) { file ->
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
            if (isScanning) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        if (isScanning) {
            Text(
                text = "Scanning menu…",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = onStartOver,
                enabled = !isScanning,
            ) { Text("Start Over") }
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = onAddPage,
                enabled = !isScanning && pages.size < 4,
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Page")
            }
            Button(
                onClick = onScan,
                enabled = !isScanning,
            ) { Text("Scan Now") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultsContent(
    state: MenuScannerUiState.HasResults,
    onAddPage: () -> Unit,
    onScan: () -> Unit,
    onStartOver: () -> Unit,
    onSave: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Collapsed thumbnail strip
        if (state.pages.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                state.pages.take(4).forEach { file ->
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${state.pages.size} page${if (state.pages.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }

        if (state.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer),
            ) {
                Text(
                    text = state.error,
                    modifier = Modifier.padding(12.dp),
                    color = colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (state.items.isEmpty() && state.error == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No burritos found. Suspicious menu.",
                    color = colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.items) { item ->
                    MenuItemCard(item = item)
                }
            }
        }

        // Sticky bottom bar
        Surface(shadowElevation = 4.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onAddPage,
                        enabled = state.pages.size < 4,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Page")
                    }
                    Button(
                        onClick = onScan,
                        modifier = Modifier.weight(1f),
                    ) { Text("Re-scan") }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onStartOver,
                        modifier = Modifier.weight(1f),
                    ) { Text("Start Over") }
                    if (state.items.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                        ) { Text("Save") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MenuItemCard(item: MenuBurritoItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.price != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (item.ingredients.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    item.ingredients.take(5).forEach { ingredient ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = ingredient,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = colorScheme.surfaceVariant,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanCameraButton(onClick: () -> Unit) {
    val darkTheme = colorScheme.surface.luminance() < 0.5f
    val green = if (darkTheme) LightCameraGreen else CameraGreen
    val iconTint = if (darkTheme) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
    Button(
        onClick = onClick,
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
            contentDescription = "Scan menu",
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun SaveDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val context = LocalContext.current
    var restaurantName by rememberSaveable { mutableStateOf("") }
    var showPlacesSearch by remember { mutableStateOf(false) }
    val predictions = remember { mutableStateListOf<String>() }

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    LaunchedEffect(restaurantName, showPlacesSearch) {
        if (!showPlacesSearch || restaurantName.length < 2) {
            predictions.clear()
            return@LaunchedEffect
        }
        delay(300)
        try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(restaurantName)
                .build()
            val result = placesClient.findAutocompletePredictions(request).await()
            predictions.clear()
            predictions.addAll(result.autocompletePredictions.take(5).map {
                it.getPrimaryText(null).toString()
            })
        } catch (_: Exception) {
            predictions.clear()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save to Restaurant") },
        text = {
            Column {
                OutlinedTextField(
                    value = restaurantName,
                    onValueChange = { restaurantName = it },
                    label = { Text("Restaurant name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showPlacesSearch = !showPlacesSearch },
                ) {
                    Text(if (showPlacesSearch) "Hide search" else "Search with Places")
                }
                if (showPlacesSearch && predictions.isNotEmpty()) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        predictions.forEach { name ->
                            TextButton(
                                onClick = {
                                    restaurantName = name
                                    predictions.clear()
                                    showPlacesSearch = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(restaurantName) },
                enabled = restaurantName.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
