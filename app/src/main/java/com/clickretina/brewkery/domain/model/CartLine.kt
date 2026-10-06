package com.clickretina.brewkery.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class CartLine(
    val item: MenuItem,
    val selection: Selection,
    val quantity: Int
) {
    /**
     * Composite cart key: itemId + sizeId + milkId + sugar
     * As customization IDs repeat across products (e.g. sz_small, m_oat),
     * this guarantees distinct lines for different products or options.
     */
    val key: String
        get() = "${item.id}|${selection.size.id}|${selection.milk.id}|${selection.sugar}"

    val unitPrice: Double
        get() = BigDecimal.valueOf(item.basePrice)
            .add(BigDecimal.valueOf(selection.size.extraPrice))
            .add(BigDecimal.valueOf(selection.milk.extraPrice))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()

    val lineTotal: Double
        get() = BigDecimal.valueOf(unitPrice)
            .multiply(BigDecimal.valueOf(quantity.toLong()))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
}

data class OrderSummary(
    val subtotal: Double,
    val deliveryFee: Double,
    val taxRatePercent: Double,
    val tax: Double,
    val total: Double
)

enum class OrderStatus {
    PREPARING
}

data class PlacedOrder(
    val ticketId: String,
    val itemCount: Int,
    val total: Double,
    val status: OrderStatus = OrderStatus.PREPARING,
    val eta: String,
    val timestamp: Long = System.currentTimeMillis()
)
