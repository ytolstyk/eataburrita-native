package com.tolstykh.eatABurrita.ui.memories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tolstykh.eatABurrita.data.BurritoEntry
import com.tolstykh.eatABurrita.data.GalleryEntry
import com.tolstykh.eatABurrita.dateFromMilliseconds
import com.tolstykh.eatABurrita.ui.components.EmptyState
import com.tolstykh.eatABurrita.ui.gallery.PixelBurritoSprite
import com.tolstykh.eatABurrita.ui.gallery.PixelBurritoTile
import java.io.File

private data class SelectedSprite(val entry: GalleryEntry, val burritoNumber: Int)

@Composable
fun MemoriesScreen(
    onBack: () -> Unit,
    viewModel: MemoriesViewModel = hiltViewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()
    var selectedEntry by remember { mutableStateOf<BurritoEntry?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedSprite by remember { mutableStateOf<SelectedSprite?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Memories", style = MaterialTheme.typography.headlineMedium)
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Photos") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Gallery") })
            }

            if (selectedTab == 0) {
                PhotosTab(
                    entries = entries,
                    safeFile = { path -> path?.let { viewModel.safePhotoFile(it) } },
                    onSelect = { selectedEntry = it },
                )
            } else {
                GalleryTab(
                    allEntries = allEntries,
                    bitmapFor = viewModel::bitmapFor,
                    backgroundFor = viewModel::backgroundFor,
                    onSelect = { entry, number -> selectedSprite = SelectedSprite(entry, number) },
                )
            }
        }
    }

    selectedEntry?.let { entry ->
        PhotoDetailDialog(
            entry = entry,
            safeFile = entry.photoPath?.let { viewModel.safePhotoFile(it) },
            onDelete = { showDeleteConfirm = true },
            onDismiss = { selectedEntry = null },
        )
    }

    selectedSprite?.let { sprite ->
        SpriteDetailDialog(
            sprite = sprite,
            bitmap = viewModel.bitmapFor(sprite.entry.timestamp),
            background = viewModel.backgroundFor(sprite.entry.timestamp),
            onDismiss = { selectedSprite = null },
        )
    }

    if (showDeleteConfirm) {
        DeletePhotoConfirmDialog(
            onConfirm = {
                selectedEntry?.let { viewModel.deletePhoto(it) }
                showDeleteConfirm = false
                selectedEntry = null
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@Composable
private fun PhotosTab(
    entries: List<BurritoEntry>,
    safeFile: (String?) -> File?,
    onSelect: (BurritoEntry) -> Unit,
) {
    if (entries.isEmpty()) {
        EmptyState(title = "No photos yet", body = "Add a photo the next time you log a burrito.")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(entries) { entry ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSelect(entry) },
                ) {
                    AsyncImage(
                        model = safeFile(entry.photoPath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryTab(
    allEntries: List<GalleryEntry>,
    bitmapFor: (Long) -> ImageBitmap,
    backgroundFor: (Long) -> Color,
    onSelect: (GalleryEntry, Int) -> Unit,
) {
    if (allEntries.isEmpty()) {
        EmptyState(title = "No burritos yet", body = "Log your first burrito to see it here.")
    } else {
        val totalCount = allEntries.size
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // allEntries is DESC (newest first); oldest entry gets #1
            itemsIndexed(allEntries) { index, entry ->
                val burritoNumber = totalCount - index
                PixelBurritoTile(
                    bitmap = bitmapFor(entry.timestamp),
                    background = backgroundFor(entry.timestamp),
                    label = "#$burritoNumber",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(entry, burritoNumber) },
                )
            }
        }
    }
}

@Composable
private fun PhotoDetailDialog(
    entry: BurritoEntry,
    safeFile: File?,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp)) {
                AsyncImage(
                    model = safeFile,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(12.dp)),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = dateFromMilliseconds(entry.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface,
                )
                if (entry.locationName != null) {
                    Text(
                        text = entry.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = colorScheme.error)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text("Delete photo", color = colorScheme.error)
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun SpriteDetailDialog(
    sprite: SelectedSprite,
    bitmap: ImageBitmap,
    background: Color,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = colorScheme.surface) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(background))
                    PixelBurritoSprite(
                        bitmap = bitmap,
                        modifier = Modifier.size(100.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Burrito #${sprite.burritoNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateFromMilliseconds(sprite.entry.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface,
                )
                if (sprite.entry.locationName != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = sprite.entry.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                if (sprite.entry.calories != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${sprite.entry.calories} cal",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

@Composable
private fun DeletePhotoConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove photo?") },
        text = { Text("The burrito entry will be kept. Only the photo will be deleted.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
