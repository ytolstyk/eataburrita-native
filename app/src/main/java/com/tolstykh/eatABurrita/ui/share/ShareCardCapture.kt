package com.tolstykh.eatABurrita.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewParent
import android.view.Window
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Walks up from [view] to find the window of the [androidx.compose.ui.window.Dialog] it belongs
 * to. [view] must come from `LocalView.current` read *inside* the Dialog's content lambda —
 * reading it outside resolves to the host Activity's own view, which has no
 * [DialogWindowProvider] ancestor.
 */
internal fun findDialogWindow(view: View): Window? {
    var parent: ViewParent? = view.parent
    while (parent != null) {
        if (parent is DialogWindowProvider) return parent.window
        parent = parent.parent
    }
    return null
}

internal suspend fun captureRegion(view: View, boundsF: RectF): Bitmap {
    val window = findDialogWindow(view)
        ?: error("captureRegion: no DialogWindowProvider above $view — was LocalView.current read outside the Dialog content lambda?")
    val bounds = Rect(
        boundsF.left.toInt(),
        boundsF.top.toInt(),
        boundsF.right.toInt(),
        boundsF.bottom.toInt(),
    )
    val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
    return suspendCancellableCoroutine { cont ->
        PixelCopy.request(
            window,
            bounds,
            bitmap,
            { result ->
                if (result == PixelCopy.SUCCESS) {
                    cont.resume(bitmap)
                } else {
                    cont.resumeWithException(Exception("PixelCopy failed: $result"))
                }
            },
            Handler(Looper.getMainLooper()),
        )
    }
}

internal suspend fun shareCardBitmap(context: Context, bitmap: Bitmap) {
    val uri = withContext(Dispatchers.IO) {
        val shareDir = File(context.cacheDir, "share").also { it.mkdirs() }
        shareDir.listFiles()?.forEach { it.delete() }
        val file = File(shareDir, "burrito_card_${UUID.randomUUID()}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
