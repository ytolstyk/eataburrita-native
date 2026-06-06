package com.tolstykh.eatABurrita.classifier

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ClassificationOutcome {
    data class Success(val isBurrito: Boolean, val confidence: Float, val comment: String) : ClassificationOutcome
    data class Failure(val cause: Throwable) : ClassificationOutcome
}

@Singleton
class BurritoClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val MODEL_FILE = "food_V1.tflite"

        // Matches against food_V1's ~2023 food labels; all lowercase for case-insensitive comparison.
        private val BURRITO_LABELS = setOf(
            "burrito", "taco", "tortilla", "wrap", "enchilada",
            "quesadilla", "fajita", "nachos", "chimichanga", "tostada",
            "tamale", "gordita", "huarache", "chalupa",
        )

        private val REJECT_COMMENTS = listOf(
            "Nice try.",
            "Not a burrito. We checked.",
            "Burrito not detected.",
            "The scanner says no.",
        )
    }

    private val classifier: ImageClassifier by lazy {
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setMaxResults(15)
            .setScoreThreshold(0.05f)
            .build()
        ImageClassifier.createFromFileAndOptions(context, MODEL_FILE, options)
    }

    suspend fun classify(photoUri: Uri): ClassificationOutcome {
        return try {
            val bitmap = withContext(Dispatchers.IO) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, photoUri)
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }

            val tensorImage = TensorImage.fromBitmap(bitmap)
            val results = withContext(Dispatchers.Default) {
                classifier.classify(tensorImage)
            }

            val best = results
                .flatMap { it.categories }
                .filter { cat ->
                    val label = cat.label.lowercase()
                    BURRITO_LABELS.any { label.contains(it) }
                }
                .maxByOrNull { it.score }

            if (best != null) {
                ClassificationOutcome.Success(true, best.score, commentFor(best.label, best.score))
            } else {
                ClassificationOutcome.Success(false, 0f, REJECT_COMMENTS.random())
            }
        } catch (e: Exception) {
            ClassificationOutcome.Failure(e)
        }
    }

    private fun commentFor(label: String, score: Float): String {
        val item = label.lowercase().replace("_", " ").trim()
        return when {
            score >= 0.85f -> "Undeniably a $item. No notes."
            score >= 0.60f -> "Solid $item detected. We'll count it."
            score >= 0.40f -> "Looks like a $item. Close enough."
            else -> "Might be a $item. We'll allow it."
        }
    }
}
