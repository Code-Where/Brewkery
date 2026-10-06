package com.clickretina.brewkery

import com.clickretina.brewkery.data.mapper.toDomain
import com.clickretina.brewkery.data.remote.dto.MenuResponseDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuDtoMappingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun realDataJson_deserializesAndMapsToDomainCorrectly() {
        val inputStream = javaClass.classLoader?.getResourceAsStream("data.json")
        assertNotNull("data.json resource must not be null", inputStream)

        val jsonString = inputStream!!.bufferedReader().use { it.readText() }
        val dto = json.decodeFromString<MenuResponseDto>(jsonString)
        val domain = dto.toDomain()

        // Verify meta
        assertEquals("Brewkery", domain.meta.app)
        assertEquals(2.50, domain.meta.deliveryFee, 0.001)
        assertEquals(8.0, domain.meta.taxRatePercent, 0.001)
        assertEquals("20 - 30 mins", domain.meta.estimatedDeliveryTime)

        // Verify categories
        assertEquals(3, domain.categories.size)
        assertTrue(domain.categories.any { it.id == "cat_hot_coffee" && it.name == "Hot Coffee" })
        assertTrue(domain.categories.any { it.id == "cat_cold_brews" && it.name == "Cold Brews" })
        assertTrue(domain.categories.any { it.id == "cat_bakery" && it.name == "Artisan Bakery" })

        // Verify items
        assertEquals(6, domain.items.size)

        val item1 = domain.items.find { it.id == 1 }
        assertNotNull(item1)
        assertEquals("Toasted Caramel Macchiato", item1!!.name)
        assertEquals(4.85, item1.basePrice, 0.001)
        assertEquals("BESTSELLER", item1.badge)
        assertEquals(3, item1.sizes.size)
        assertEquals(3, item1.sugarLevels.size)
        assertEquals(3, item1.milkOptions.size)

        val item6 = domain.items.find { it.id == 6 }
        assertNotNull(item6)
        assertEquals("Nitro Cherry Cascara", item6!!.name)
        assertEquals(5.60, item6.basePrice, 0.001)
        assertEquals(1, item6.sizes.size)
        assertEquals("sz_pint", item6.sizes[0].id)
    }
}
