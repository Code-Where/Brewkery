package com.clickretina.brewkery

import com.clickretina.brewkery.data.repository.CartRepository
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.domain.model.MilkOption
import com.clickretina.brewkery.domain.model.Selection
import com.clickretina.brewkery.domain.model.SizeOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CartRepositoryTest {

    private lateinit var cartRepository: CartRepository

    private val sampleItem = MenuItem(
        id = 1,
        categoryId = "cat_hot_coffee",
        name = "Toasted Caramel Macchiato",
        tagline = "Double shot espresso",
        description = "Rich espresso roast",
        basePrice = 4.85,
        rating = 4.9,
        reviewCount = 312,
        prepTime = "5-7 mins",
        calories = 240,
        imageUrl = "",
        badge = "BESTSELLER",
        ingredients = listOf("Espresso"),
        sizes = listOf(SizeOption("sz_medium", "Grande (12 oz)", 0.65)),
        sugarLevels = listOf("50% Mild"),
        milkOptions = listOf(MilkOption("m_oat", "Oat Milk", 0.0))
    )

    @Before
    fun setUp() {
        cartRepository = CartRepository()
    }

    @Test
    fun addToCart_mergesQuantityWhenKeysMatch() {
        val selection = Selection(
            size = SizeOption("sz_medium", "Grande (12 oz)", 0.65),
            milk = MilkOption("m_oat", "Oat Milk", 0.0),
            sugar = "50% Mild"
        )

        cartRepository.addToCart(sampleItem, selection, quantity = 2)
        assertEquals(1, cartRepository.cartLines.value.size)
        assertEquals(2, cartRepository.cartLines.value[0].quantity)

        // Add 3 more of same item and options
        cartRepository.addToCart(sampleItem, selection, quantity = 3)
        assertEquals(1, cartRepository.cartLines.value.size)
        assertEquals(5, cartRepository.cartLines.value[0].quantity)
    }

    @Test
    fun addToCart_createsNewLinesWhenCustomizationsDiffer() {
        val selectionA = Selection(
            size = SizeOption("sz_small", "Tall", 0.0),
            milk = MilkOption("m_oat", "Oat Milk", 0.0),
            sugar = "0% Unsweetened"
        )
        val selectionB = Selection(
            size = SizeOption("sz_large", "Venti", 1.25),
            milk = MilkOption("m_almond", "Almond Milk", 0.50),
            sugar = "100% Standard Sweet"
        )

        cartRepository.addToCart(sampleItem, selectionA, quantity = 1)
        cartRepository.addToCart(sampleItem, selectionB, quantity = 1)

        assertEquals(2, cartRepository.cartLines.value.size)
    }

    @Test
    fun updateQuantity_decrementsAndRemovesLineWhenZero() {
        val selection = Selection(
            size = SizeOption("sz_medium", "Grande", 0.65),
            milk = MilkOption("m_oat", "Oat Milk", 0.0),
            sugar = "50% Mild"
        )
        cartRepository.addToCart(sampleItem, selection, quantity = 2)
        val key = cartRepository.cartLines.value[0].key

        // Decrement by 1
        cartRepository.updateQuantity(key, -1)
        assertEquals(1, cartRepository.cartLines.value[0].quantity)

        // Decrement to 0 -> removes line
        cartRepository.updateQuantity(key, -1)
        assertTrue(cartRepository.cartLines.value.isEmpty())
    }

    @Test
    fun clearCart_resetsCartToEmpty() {
        val selection = Selection(
            size = SizeOption("sz_medium", "Grande", 0.65),
            milk = MilkOption("m_oat", "Oat Milk", 0.0),
            sugar = "50% Mild"
        )
        cartRepository.addToCart(sampleItem, selection, quantity = 2)
        assertEquals(1, cartRepository.cartLines.value.size)

        cartRepository.clearCart()
        assertTrue(cartRepository.cartLines.value.isEmpty())
        assertEquals(0, cartRepository.getTotalCount())
    }
}
