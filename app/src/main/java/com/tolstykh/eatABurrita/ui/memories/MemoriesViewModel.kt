package com.tolstykh.eatABurrita.ui.memories

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolstykh.eatABurrita.data.BurritoDao
import com.tolstykh.eatABurrita.data.BurritoEntry
import com.tolstykh.eatABurrita.data.GalleryEntry
import com.tolstykh.eatABurrita.ui.gallery.rasterizeSprite
import com.tolstykh.eatABurrita.ui.gallery.spriteBackground
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MemoriesViewModel @Inject constructor(
    private val dao: BurritoDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val entries: StateFlow<List<BurritoEntry>> = dao.getEntriesWithPhoto()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEntries: StateFlow<List<GalleryEntry>> = dao.getAllForGallery()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sprite caches keyed by timestamp — outlive LazyGrid item composition slots
    private val bitmapCache = mutableMapOf<Long, ImageBitmap>()
    private val bgCache = mutableMapOf<Long, Color>()

    fun bitmapFor(timestamp: Long): ImageBitmap =
        bitmapCache.getOrPut(timestamp) { rasterizeSprite(timestamp) }

    fun backgroundFor(timestamp: Long): Color =
        bgCache.getOrPut(timestamp) { spriteBackground(timestamp) }

    // Validates that the path is inside the app's photo directory before use.
    fun safePhotoFile(path: String): File? = runCatching {
        val file = File(path).canonicalFile
        val allowed = File(context.filesDir, "burrito_photo_log").canonicalFile
        if (file.path.startsWith(allowed.path + File.separator)) file else null
    }.getOrNull()

    fun deletePhoto(entry: BurritoEntry) {
        viewModelScope.launch {
            entry.photoPath?.let { path -> safePhotoFile(path)?.delete() }
            dao.update(entry.copy(photoPath = null))
        }
    }
}
