package com.tolstykh.eatABurrita.helpers

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

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
