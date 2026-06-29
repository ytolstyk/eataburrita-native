package com.tolstykh.eatABurrita.ui.menuscanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolstykh.eatABurrita.classifier.MenuScanner
import com.tolstykh.eatABurrita.classifier.MenuScanOutcome
import com.tolstykh.eatABurrita.data.MenuBurritoItem
import com.tolstykh.eatABurrita.data.MenuScan
import com.tolstykh.eatABurrita.data.MenuScanDao
import com.tolstykh.eatABurrita.data.toPipeString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

sealed interface MenuScannerUiState {
    data object Empty : MenuScannerUiState
    data class HasPages(val pages: List<File>) : MenuScannerUiState
    data class Scanning(
        val pages: List<File>,
        val previousItems: List<MenuBurritoItem>,
    ) : MenuScannerUiState
    data class HasResults(
        val pages: List<File>,
        val items: List<MenuBurritoItem>,
        val error: String? = null,
    ) : MenuScannerUiState
}

private fun MenuScannerUiState.pages(): List<File> = when (this) {
    is MenuScannerUiState.Empty -> emptyList()
    is MenuScannerUiState.HasPages -> pages
    is MenuScannerUiState.Scanning -> pages
    is MenuScannerUiState.HasResults -> pages
}

private fun MenuScannerUiState.items(): List<MenuBurritoItem> = when (this) {
    is MenuScannerUiState.HasResults -> items
    is MenuScannerUiState.Scanning -> previousItems
    else -> emptyList()
}

@HiltViewModel
class MenuScannerViewModel @Inject constructor(
    private val menuScanner: MenuScanner,
    private val menuScanDao: MenuScanDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuScannerUiState>(MenuScannerUiState.Empty)
    val uiState: StateFlow<MenuScannerUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    fun onPageAdded(file: File) {
        val current = _uiState.value
        if (current.pages().size >= 4) return
        viewModelScope.launch(Dispatchers.IO) {
            scaleFileInPlace(file)
            val newPages = current.pages() + file
            _uiState.update { state ->
                when (state) {
                    is MenuScannerUiState.Empty -> MenuScannerUiState.HasPages(newPages)
                    is MenuScannerUiState.HasPages -> MenuScannerUiState.HasPages(newPages)
                    is MenuScannerUiState.HasResults -> MenuScannerUiState.HasResults(newPages, state.items, state.error)
                    is MenuScannerUiState.Scanning -> state
                }
            }
        }
    }

    fun startScan() {
        val current = _uiState.value
        val pages = current.pages()
        if (pages.isEmpty()) return
        val previousItems = current.items()

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.value = MenuScannerUiState.Scanning(pages, previousItems)
            val bitmaps = withContext(Dispatchers.IO) {
                pages.mapNotNull { decodeSafely(it) }
            }
            val outcome = menuScanner.scan(bitmaps)
            if (_uiState.value !is MenuScannerUiState.Scanning) return@launch
            _uiState.value = when (outcome) {
                is MenuScanOutcome.Success -> MenuScannerUiState.HasResults(pages, outcome.items)
                is MenuScanOutcome.ApiError -> MenuScannerUiState.HasResults(
                    pages, previousItems,
                    "The burrito scanner stepped out for lunch. Give it a moment and try again.",
                )
                is MenuScanOutcome.Failure -> MenuScannerUiState.HasResults(
                    pages, previousItems,
                    "Something's off in the burrito matrix. Try scanning again.",
                )
            }
        }
    }

    fun startOver() {
        scanJob?.cancel()
        scanJob = null
        val files = _uiState.value.pages()
        _uiState.value = MenuScannerUiState.Empty
        viewModelScope.launch(Dispatchers.IO) { files.forEach { it.delete() } }
    }

    fun saveScan(restaurantName: String) {
        val items = (_uiState.value as? MenuScannerUiState.HasResults)?.items ?: return
        viewModelScope.launch {
            menuScanDao.insert(
                MenuScan(
                    restaurantName = restaurantName.trim(),
                    itemsJson = items.toPipeString(),
                )
            )
        }
    }

    fun cleanUpTempFiles() {
        val files = _uiState.value.pages()
        viewModelScope.launch(Dispatchers.IO) { files.forEach { it.delete() } }
    }

    override fun onCleared() {
        super.onCleared()
        _uiState.value.pages().forEach { it.delete() }
    }

    private fun scaleFileInPlace(file: File) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return
            val scaled = bitmap.scaleToMaxDim(1024)
            file.outputStream().use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 75, out)
            }
        } catch (_: Exception) {}
    }

    private fun decodeSafely(file: File): Bitmap? =
        try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: FileNotFoundException) {
            null
        } catch (_: Exception) {
            null
        }

    private fun Bitmap.scaleToMaxDim(maxDim: Int): Bitmap {
        if (width <= maxDim && height <= maxDim) return this
        val scale = maxDim.toFloat() / maxOf(width, height)
        return Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
    }

    fun createMenuScanFile(): File {
        val dir = File(context.cacheDir, "menu_scans").also { it.mkdirs() }
        return File(dir, "scan_${System.currentTimeMillis()}.jpg")
    }
}
