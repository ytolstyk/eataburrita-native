package com.tolstykh.eatABurrita.data

data class MenuBurritoItem(
    val name: String,
    val ingredients: List<String>,
    val price: String?,
)

fun List<MenuBurritoItem>.toPipeString(): String =
    joinToString("\n") { "${it.name}|${it.price ?: ""}|${it.ingredients.joinToString(",")}" }

fun String.toMenuBurritoItems(): List<MenuBurritoItem> =
    lines().filter { it.contains("|") }.mapNotNull { line ->
        val parts = line.split("|", limit = 3)
        if (parts.isEmpty()) return@mapNotNull null
        MenuBurritoItem(
            name = parts[0].trim(),
            price = parts.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() },
            ingredients = parts.getOrNull(2)
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList(),
        )
    }
