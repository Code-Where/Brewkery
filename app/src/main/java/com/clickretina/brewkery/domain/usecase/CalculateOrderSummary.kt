package com.clickretina.brewkery.domain.usecase

import com.clickretina.brewkery.domain.model.CartLine
import com.clickretina.brewkery.domain.model.OrderSummary
import com.clickretina.brewkery.domain.model.StoreMeta
import java.math.BigDecimal
import java.math.RoundingMode

class CalculateOrderSummary {
    operator fun invoke(cartLines: List<CartLine>, meta: StoreMeta?): OrderSummary {
        val taxRate = meta?.taxRatePercent ?: 8.0
        if (cartLines.isEmpty() || meta == null) {
            return OrderSummary(
                subtotal = 0.0,
                deliveryFee = 0.0,
                taxRatePercent = taxRate,
                tax = 0.0,
                total = 0.0
            )
        }

        var subtotalBd = BigDecimal.ZERO
        for (line in cartLines) {
            subtotalBd = subtotalBd.add(BigDecimal.valueOf(line.lineTotal))
        }
        subtotalBd = subtotalBd.setScale(2, RoundingMode.HALF_UP)

        val deliveryBd = BigDecimal.valueOf(meta.deliveryFee).setScale(2, RoundingMode.HALF_UP)

        // Tax on SUBTOTAL ONLY, rounded HALF_UP to 2 decimal places
        val taxBd = subtotalBd
            .multiply(BigDecimal.valueOf(meta.taxRatePercent))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)

        val totalBd = subtotalBd
            .add(deliveryBd)
            .add(taxBd)
            .setScale(2, RoundingMode.HALF_UP)

        return OrderSummary(
            subtotal = subtotalBd.toDouble(),
            deliveryFee = deliveryBd.toDouble(),
            taxRatePercent = meta.taxRatePercent,
            tax = taxBd.toDouble(),
            total = totalBd.toDouble()
        )
    }
}
