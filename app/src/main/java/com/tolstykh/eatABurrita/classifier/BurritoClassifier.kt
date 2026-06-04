package com.tolstykh.eatABurrita.classifier

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ClassificationOutcome {
    data class Success(val confidence: Float, val label: String) : ClassificationOutcome
    data object NoMatch : ClassificationOutcome
    data class Failure(val cause: Throwable) : ClassificationOutcome
}

@Singleton
class BurritoClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val BURRITO_LABELS = setOf(
            "Burrito", "Wrap", "Tortilla", "Mexican food", "Taco", "Tex-Mex",
        )
        private const val CONFIDENCE_THRESHOLD = 0.4f
    }

    suspend fun classify(photoUri: Uri): ClassificationOutcome {
        return try {
            val image = InputImage.fromFilePath(context, photoUri)
            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(CONFIDENCE_THRESHOLD)
                .build()
            val labeler = ImageLabeling.getClient(options)
            val labels = labeler.process(image).await()
            val best = labels
                .filter { label -> BURRITO_LABELS.any { it.equals(label.text, ignoreCase = true) } }
                .maxByOrNull { it.confidence }
            if (best != null) {
                ClassificationOutcome.Success(best.confidence, best.text)
            } else {
                ClassificationOutcome.NoMatch
            }
        } catch (e: Exception) {
            ClassificationOutcome.Failure(e)
        }
    }
}
