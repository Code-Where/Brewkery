package com.clickretina.brewkery.data.mapper

import com.clickretina.brewkery.data.remote.dto.CategoryDto
import com.clickretina.brewkery.data.remote.dto.ItemDto
import com.clickretina.brewkery.data.remote.dto.MenuResponseDto
import com.clickretina.brewkery.data.remote.dto.MilkOptionDto
import com.clickretina.brewkery.data.remote.dto.SizeOptionDto
import com.clickretina.brewkery.data.remote.dto.StoreMetaDto
import com.clickretina.brewkery.domain.model.Category
import com.clickretina.brewkery.domain.model.MenuData
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.domain.model.MilkOption
import com.clickretina.brewkery.domain.model.SizeOption
import com.clickretina.brewkery.domain.model.StoreMeta

fun StoreMetaDto.toDomain(): StoreMeta {
    return StoreMeta(
        app = app ?: "Brewkery",
        version = version ?: "1.0.0",
        tagline = tagline ?: "Artisanal Coffee & Fresh Oven Bakes",
        currency = currency ?: "USD",
        currencySymbol = currencySymbol ?: "$",
        deliveryFee = deliveryFee ?: 2.50,
        taxRatePercent = taxRatePercent ?: 8.0,
        estimatedDeliveryTime = estimatedDeliveryTime ?: "20 - 30 mins"
    )
}

fun CategoryDto.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        icon = icon ?: "",
        itemCount = itemCount ?: 0
    )
}

fun SizeOptionDto.toDomain(): SizeOption {
    return SizeOption(
        id = id,
        label = label,
        extraPrice = extraPrice ?: 0.0
    )
}

fun MilkOptionDto.toDomain(): MilkOption {
    return MilkOption(
        id = id,
        name = name,
        extraPrice = extraPrice ?: 0.0
    )
}

fun ItemDto.toDomain(): MenuItem {
    return MenuItem(
        id = id,
        categoryId = categoryId,
        name = name,
        tagline = tagline ?: "",
        description = description ?: "",
        basePrice = basePrice,
        rating = rating ?: 0.0,
        reviewCount = reviewCount ?: 0,
        prepTime = prepTime ?: "",
        calories = calories ?: 0,
        imageUrl = imageUrl ?: "",
        badge = badge,
        ingredients = ingredients,
        sizes = customizations?.sizes?.map { it.toDomain() } ?: emptyList(),
        sugarLevels = customizations?.sugarLevels ?: emptyList(),
        milkOptions = customizations?.milkOptions?.map { it.toDomain() } ?: emptyList()
    )
}

fun MenuResponseDto.toDomain(): MenuData {
    return MenuData(
        meta = meta.toDomain(),
        categories = categories.map { it.toDomain() },
        items = items.map { it.toDomain() }
    )
}
