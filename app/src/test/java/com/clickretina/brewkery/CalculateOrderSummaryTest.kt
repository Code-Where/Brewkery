package com.clickretina.brewkery

import com.clickretina.brewkery.domain.model.CartLine
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.domain.model.MilkOption
import com.clickretina.brewkery.domain.model.Selection
import com.clickretina.brewkery.domain.model.SizeOption
import com.clickretina.brewkery.domain.model.StoreMeta
import com.clickretina.brewkery.domain.usecase.CalculateOrderSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class CalculateOrderSummaryTest {

    private lateinit var calculateOrderSummary: CalculateOrderSummary
    private lateinit var storeMeta: StoreMeta

    private val sampleCoffee = MenuItem(
        id = 1,
        categoryId = "cat_hot_coffee",
        name = "Toasted Caramel Macchiato",
        tagline = "Double shot espresso with velvety oat milk & salted caramel",
        description = "Rich espresso roast layered with velvety steamed milk.",
        basePrice = 4.85,
        rating = 4.9,
        reviewCount = 312,
        prepTime = "5-7 mins",
        calories = 240,
        imageUrl = "",
        badge = "BESTSELLER",
        ingredients = listOf("Signature Espresso", "Steamed Oat Milk"),
        sizes = listOf(
            SizeOption("sz_small", "Tall (8 oz)", 0.0),
            SizeOption("sz_medium", "Grande (12 oz)", 0.65),
            SizeOption("sz_large", "Venti (16 oz)", 1.25)
        ),
        sugarLevels = listOf("0% Unsweetened", "50% Mild", "100% Standard Sweet"),
        milkOptions = listOf(
            MilkOption("m_oat", "Oat Milk (Barista Blend)", 0.0),
            MilkOption("m_almond", "Roasted Almond Milk", 0.50),
            MilkOption("m_whole", "Organic Whole Milk", 0.0)
        )
    )

    private val sampleCroissant = MenuItem(
        id = 3,
        categoryId = "cat_bakery",
        name = "Golden Normandy Croissant",
        tagline = "36-layer flakey pastry",
        description = "Baked fresh at sunrise.",
        basePrice = 3.90,
        rating = 4.95,
        reviewCount = 420,
        prepTime = "Warm in 2 mins",
        calories = 290,
        imageUrl = "",
        badge = "FRESHLY BAKED",
        ingredients = listOf("Normandy Butter", "Organic Flour"),
        sizes = listOf(
            SizeOption("sz_single", "Single Piece", 0.0),
            SizeOption("sz_pair", "Pair Box (2 Pcs)", 3.20)
        ),
        sugarLevels = listOf("Warm & Crisp (Recommended)", "Room Temperature"),
        milkOptions = listOf(
            MilkOption("top_butter", "Whipped Honey Butter", 0.50),
            MilkOption("top_plain", "Classic (No Spread)", 0.0)
        )
    )

    @Before
    fun setUp() {
        calculateOrderSummary = CalculateOrderSummary()
        storeMeta = StoreMeta(
            app = "Brewkery",
            version = "1.0.0",
            tagline = "Artisanal Coffee & Fresh Oven Bakes",
            currency = "USD",
            currencySymbol = "$",
            deliveryFee = 2.50,
            taxRatePercent = 8.0,
            estimatedDeliveryTime = "20 - 30 mins"
        )
    }

    @Test
    fun singleItem_grande_qty1_calculatesCorrectUnitPrice() {
        // Base 4.85 + Grande (+0.65) + Oat (0.00) = 5.50
        val selection = Selection(
            size = SizeOption("sz_medium", "Grande (12 oz)", 0.65),
            milk = MilkOption("m_oat", "Oat Milk (Barista Blend)", 0.00),
            sugar = "50% Mild"
        )
        val line = CartLine(sampleCoffee, selection, quantity = 1)

        assertEquals(5.50, line.unitPrice, 0.001)
        assertEquals(5.50, line.lineTotal, 0.001)
    }

    @Test
    fun prototypeCart_subtotal940_fee250_tax8percent_givesTotal1265() {
        // Line 1: Caramel Macchiato Grande = 5.50
        val line1 = CartLine(
            sampleCoffee,
            Selection(
                size = SizeOption("sz_medium", "Grande (12 oz)", 0.65),
                milk = MilkOption("m_oat", "Oat Milk (Barista Blend)", 0.00),
                sugar = "50% Mild"
            ),
            quantity = 1
        )
        // Line 2: Croissant Single + No spread = 3.90
        val line2 = CartLine(
            sampleCroissant,
            Selection(
                size = SizeOption("sz_single", "Single Piece", 0.00),
                milk = MilkOption("top_plain", "Classic (No Spread)", 0.00),
                sugar = "Warm & Crisp (Recommended)"
            ),
            quantity = 1
        )

        val summary = calculateOrderSummary(listOf(line1, line2), storeMeta)

        assertEquals(9.40, summary.subtotal, 0.001)
        assertEquals(2.50, summary.deliveryFee, 0.001)
        // 9.40 * 0.08 = 0.752 -> rounded HALF_UP to 0.75
        assertEquals(0.75, summary.tax, 0.001)
        // 9.40 + 2.50 + 0.75 = 12.65
        assertEquals(12.65, summary.total, 0.001)
    }

    @Test
    fun emptyCart_returnsZeroSubtotalDeliveryTaxAndTotal() {
        val summary = calculateOrderSummary(emptyList(), storeMeta)

        assertEquals(0.0, summary.subtotal, 0.001)
        assertEquals(0.0, summary.deliveryFee, 0.001)
        assertEquals(0.0, summary.tax, 0.001)
        assertEquals(0.0, summary.total, 0.001)
    }

    @Test
    fun sameItem_differentOptions_haveDistinctKeys() {
        val lineA = CartLine(
            sampleCoffee,
            Selection(
                size = SizeOption("sz_small", "Tall (8 oz)", 0.0),
                milk = MilkOption("m_oat", "Oat Milk", 0.0),
                sugar = "0% Unsweetened"
            ),
            quantity = 1
        )
        val lineB = CartLine(
            sampleCoffee,
            Selection(
                size = SizeOption("sz_medium", "Grande (12 oz)", 0.65),
                milk = MilkOption("m_oat", "Oat Milk", 0.0),
                sugar = "0% Unsweetened"
            ),
            quantity = 1
        )

        assertNotEquals(lineA.key, lineB.key)
    }

    @Test
    fun sameItem_sameOptions_haveIdenticalKey() {
        val lineA = CartLine(
            sampleCoffee,
            Selection(
                size = SizeOption("sz_medium", "Grande (12 oz)", 0.65),
                milk = MilkOption("m_oat", "Oat Milk", 0.0),
                sugar = "50% Mild"
            ),
            quantity = 1
        )
        val lineB = CartLine(
            sampleCoffee,
            Selection(
                size = SizeOption("sz_medium", "Grande (12 oz)", 0.65),
                milk = MilkOption("m_oat", "Oat Milk", 0.0),
                sugar = "50% Mild"
            ),
            quantity = 2
        )

        assertEquals(lineA.key, lineB.key)
    }

    @Test
    fun taxRounding_subtotal585_tax8percent_roundsTo047WithHalfUp() {
        // 5.85 * 0.08 = 0.468 -> HALF_UP gives 0.47
        val customMeta = storeMeta.copy(taxRatePercent = 8.0)
        val singleLine = CartLine(
            sampleCoffee.copy(basePrice = 5.85),
            Selection(
                size = SizeOption("sz_small", "Tall", 0.0),
                milk = MilkOption("m_oat", "Oat", 0.0),
                sugar = "Zero"
            ),
            quantity = 1
        )

        val summary = calculateOrderSummary(listOf(singleLine), customMeta)

        assertEquals(5.85, summary.subtotal, 0.001)
        assertEquals(0.47, summary.tax, 0.001)
        assertEquals(5.85 + 2.50 + 0.47, summary.total, 0.001)
    }
}
