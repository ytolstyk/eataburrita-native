package com.tolstykh.eatABurrita.classifier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.tolstykh.eatABurrita.BuildConfig
import com.tolstykh.eatABurrita.data.MenuBurritoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed interface MenuScanOutcome {
    data class Success(val items: List<MenuBurritoItem>) : MenuScanOutcome
    data class ApiError(val message: String) : MenuScanOutcome
    data object Failure : MenuScanOutcome
}

@Singleton
class MenuScanner @Inject constructor() {

    private companion object {
        const val MAX_DIM = 1024
        const val JPEG_QUALITY = 75
        val PROMPT = """
            You are a burrito menu expert. Analyze all of these menu page images carefully.
            Find EVERY burrito item listed anywhere across all pages.
            Deduplicate: if the same item appears on multiple pages, list it only once.
            For each burrito, return: name, up to 5 key ingredients, and price (null if not visible).

            Reply with ONE LINE per burrito in this exact format:
            NAME|PRICE|INGREDIENT1,INGREDIENT2,INGREDIENT3
            - NAME: burrito name exactly as written on the menu
            - PRICE: price as shown (e.g. ${'$'}8.99), or leave blank if not visible
            - INGREDIENTS: up to 5 key ingredients separated by commas

            Example:
            Bean & Cheese Burrito|${'$'}7.99|pinto beans,cheddar cheese,salsa
            Carnitas Burrito||carnitas,rice,black beans,guacamole

            If no burritos are found anywhere on the menu, reply with exactly: NONE
            No headers, no explanation, no markdown — just the lines.
        """.trimIndent()
    }

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash-lite",
            apiKey = BuildConfig.GEMINI_API_KEY,
        )
    }

    suspend fun scan(bitmaps: List<Bitmap>): MenuScanOutcome {
        if (bitmaps.isEmpty()) return MenuScanOutcome.Success(emptyList())
        return try {
            val compressed = withContext(Dispatchers.IO) {
                bitmaps.map { it.compressForScan() }
            }
            val response = withContext(Dispatchers.IO) {
                model.generateContent(
                    content {
                        compressed.forEach { image(it) }
                        text(PROMPT)
                    }
                )
            }
            parseResponse(response.text ?: "")
        } catch (e: Exception) {
            MenuScanOutcome.ApiError(e.message ?: "Unknown error")
        }
    }

    private fun parseResponse(raw: String): MenuScanOutcome {
        val trimmed = raw.trim()
        if (trimmed.equals("NONE", ignoreCase = true)) {
            return MenuScanOutcome.Success(emptyList())
        }
        val items = trimmed.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.contains("|") }
            .mapNotNull { line ->
                val parts = line.split("|", limit = 3)
                if (parts.isEmpty()) return@mapNotNull null
                MenuBurritoItem(
                    name = parts[0].trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null,
                    price = parts.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() },
                    ingredients = parts.getOrNull(2)
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?: emptyList(),
                )
            }
        return MenuScanOutcome.Success(items)
    }

    private fun Bitmap.compressForScan(): Bitmap {
        val scaled = scaleToMaxDim(MAX_DIM)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        val bytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun Bitmap.scaleToMaxDim(maxDim: Int): Bitmap {
        if (width <= maxDim && height <= maxDim) return this
        val scale = maxDim.toFloat() / maxOf(width, height)
        return Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
    }
}
