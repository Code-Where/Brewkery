package com.clickretina.brewkery.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object PriceFormatter {
    fun formatPrice(amount: Double, currencySymbol: String = "$"): String {
        val bd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
        return String.format(Locale.US, "%s%.2f", currencySymbol, bd.toDouble())
    }

    fun roundPrice(amount: Double): Double {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}
