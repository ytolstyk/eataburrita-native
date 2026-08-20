package com.tolstykh.eatABurrita.helpers

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/** [file] must be under a path declared in res/xml/file_paths.xml. */
fun Context.shareableUriFor(file: File): Uri =
    FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

fun copyToPhotoLog(context: Context, sourceUri: Uri): String? {
    return runCatching {
        val dir = File(context.filesDir, "burrito_photo_log").also { it.mkdirs() }
        val dest = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    }.getOrNull()
}

/** Deletes [uri] only if it's a cache file we created via [shareableUriFor] — never a gallery/media URI. */
fun Context.deleteOwnedCacheUri(uri: Uri?) {
    if (uri != null && uri.authority == "$packageName.fileprovider") {
        runCatching { contentResolver.delete(uri, null, null) }
    }
}
