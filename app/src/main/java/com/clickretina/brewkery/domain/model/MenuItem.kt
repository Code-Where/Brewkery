package com.clickretina.brewkery.domain.model

data class SizeOption(
    val id: String,
    val label: String,
    val extraPrice: Double = 0.0
)

data class MilkOption(
    val id: String,
    val name: String,
    val extraPrice: Double = 0.0
)

data class Selection(
    val size: SizeOption,
    val milk: MilkOption,
    val sugar: String
)

data class MenuItem(
    val id: Int,
    val categoryId: String,
    val name: String,
    val tagline: String,
    val description: String,
    val basePrice: Double,
    val rating: Double,
    val reviewCount: Int,
    val prepTime: String,
    val calories: Int,
    val imageUrl: String,
    val badge: String?,
    val ingredients: List<String>,
    val sizes: List<SizeOption>,
    val sugarLevels: List<String>,
    val milkOptions: List<MilkOption>
)
