package com.tolstykh.eatABurrita.classifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.tolstykh.eatABurrita.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ClassificationOutcome {
    data class Success(val isBurrito: Boolean, val confidence: Float, val comment: String) : ClassificationOutcome
    data class RateLimited(val secondsRemaining: Int) : ClassificationOutcome
    data object ApiError : ClassificationOutcome
    data class Failure(val cause: Throwable) : ClassificationOutcome
}

@Singleton
class BurritoClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val COOLDOWN_MS = 30_000L
        private const val MAX_DIM = 512
        private val PROMPT = """
            Supreme Burrito Oracle: is this a burrito?
            Reply on ONE line using this exact format:
            YES|0.95|Your witty remark here
            or: NO|0.0|Your witty remark here
            Confidence is a number between 0.0 and 1.0. Remark must be under 80 chars. Be dramatic.
        """.trimIndent()
    }

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash-lite",
            apiKey = BuildConfig.GEMINI_API_KEY,
        )
    }

    private var lastCallEpochMs = 0L

    suspend fun classify(photoUri: Uri): ClassificationOutcome {
        val now = System.currentTimeMillis()
        val elapsed = now - lastCallEpochMs
        if (elapsed < COOLDOWN_MS) {
            val secondsLeft = ((COOLDOWN_MS - elapsed) / 1000L + 1).toInt()
            return ClassificationOutcome.RateLimited(secondsLeft)
        }
        lastCallEpochMs = now

        return try {
            val bitmap = withContext(Dispatchers.IO) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, photoUri)
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }.scaleToMaxDim(MAX_DIM)
            }

            val response = withContext(Dispatchers.IO) {
                model.generateContent(
                    content {
                        image(bitmap)
                        text(PROMPT)
                    }
                )
            }

            parseResponse(response.text ?: "")
        } catch (e: Exception) {
            ClassificationOutcome.ApiError
        }
    }

    private fun parseResponse(raw: String): ClassificationOutcome {
        val line = raw.trim().lines().firstOrNull { it.contains("|") }
            ?: return ClassificationOutcome.ApiError
        val parts = line.split("|")
        if (parts.size < 3) return ClassificationOutcome.ApiError
        val isBurrito = parts[0].trim().uppercase().startsWith("YES")
        val confidence = parts[1].trim().toFloatOrNull() ?: if (isBurrito) 0.8f else 0f
        val comment = parts[2].trim().removeSurrounding("<", ">")
        return ClassificationOutcome.Success(isBurrito, confidence, comment)
    }
}

private fun Bitmap.scaleToMaxDim(maxDim: Int): Bitmap {
    if (width <= maxDim && height <= maxDim) return this
    val scale = maxDim.toFloat() / maxOf(width, height)
    return Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
}
