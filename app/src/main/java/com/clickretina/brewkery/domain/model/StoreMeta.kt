package com.clickretina.brewkery.domain.model

data class StoreMeta(
    val app: String,
    val version: String,
    val tagline: String,
    val currency: String,
    val currencySymbol: String,
    val deliveryFee: Double,
    val taxRatePercent: Double,
    val estimatedDeliveryTime: String
)

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val itemCount: Int
)
