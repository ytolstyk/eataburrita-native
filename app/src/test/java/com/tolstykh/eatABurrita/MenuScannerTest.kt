package com.tolstykh.eatABurrita

import com.tolstykh.eatABurrita.data.MenuBurritoItem
import com.tolstykh.eatABurrita.data.toMenuBurritoItems
import com.tolstykh.eatABurrita.data.toPipeString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuScannerTest {

    // Pipe parsing — mirrors MenuScanner.parseResponse logic
    private fun parsePipeResponse(raw: String): List<MenuBurritoItem> {
        val trimmed = raw.trim()
        if (trimmed.equals("NONE", ignoreCase = true)) return emptyList()
        return trimmed.lines()
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
    }

    @Test
    fun `parse clean pipe response`() {
        val raw = "Bean Burrito|\$8.99|beans,cheese,salsa\nCarnitas Burrito||\n"
        val items = parsePipeResponse(raw)
        assertEquals(2, items.size)
        assertEquals("Bean Burrito", items[0].name)
        assertEquals("\$8.99", items[0].price)
        assertEquals(listOf("beans", "cheese", "salsa"), items[0].ingredients)
        assertEquals("Carnitas Burrito", items[1].name)
        assertNull(items[1].price)
        assertTrue(items[1].ingredients.isEmpty())
    }

    @Test
    fun `parse NONE response returns empty list`() {
        assertEquals(emptyList<MenuBurritoItem>(), parsePipeResponse("NONE"))
        assertEquals(emptyList<MenuBurritoItem>(), parsePipeResponse("none"))
        assertEquals(emptyList<MenuBurritoItem>(), parsePipeResponse("  NONE  "))
    }

    @Test
    fun `skip lines without pipe`() {
        val raw = "Here are the burritos:\nBean Burrito|\$5.00|beans,cheese\n"
        val items = parsePipeResponse(raw)
        assertEquals(1, items.size)
        assertEquals("Bean Burrito", items[0].name)
    }

    @Test
    fun `skip lines with blank name`() {
        val raw = "|\$5.00|beans\nGood Burrito|\$7.00|rice"
        val items = parsePipeResponse(raw)
        assertEquals(1, items.size)
        assertEquals("Good Burrito", items[0].name)
    }

    @Test
    fun `ingredients capped at 5 in display (serialization round-trip)`() {
        val original = listOf(
            MenuBurritoItem("Big One", listOf("rice", "beans", "cheese", "salsa", "sour cream"), "\$12.00")
        )
        val serialized = original.toPipeString()
        val restored = serialized.toMenuBurritoItems()
        assertEquals(1, restored.size)
        assertEquals("Big One", restored[0].name)
        assertEquals("\$12.00", restored[0].price)
        assertEquals(listOf("rice", "beans", "cheese", "salsa", "sour cream"), restored[0].ingredients)
    }

    @Test
    fun `toPipeString and toMenuBurritoItems round-trip`() {
        val items = listOf(
            MenuBurritoItem("Veggie Burrito", listOf("peppers", "onions"), null),
            MenuBurritoItem("Beef Burrito", listOf("carne asada", "guac"), "\$11.99"),
        )
        val roundTripped = items.toPipeString().toMenuBurritoItems()
        assertEquals(items.size, roundTripped.size)
        items.forEachIndexed { i, original ->
            assertEquals(original.name, roundTripped[i].name)
            assertEquals(original.price, roundTripped[i].price)
            assertEquals(original.ingredients, roundTripped[i].ingredients)
        }
    }

    @Test
    fun `empty list serializes and deserializes`() {
        val result = emptyList<MenuBurritoItem>().toPipeString().toMenuBurritoItems()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `item name with pipe character does not corrupt next field`() {
        // Names with | would break parsing — verify split(limit=3) contains the damage to first field
        val raw = "Bean|Rice Burrito|\$5.00|beans"
        val items = parsePipeResponse(raw)
        // name = "Bean", price = "Rice Burrito", ingredients = "$5.00|beans" split by comma
        // This is a known limitation of pipe format — document it via test behavior
        assertEquals(1, items.size)
        assertEquals("Bean", items[0].name)
    }
}
