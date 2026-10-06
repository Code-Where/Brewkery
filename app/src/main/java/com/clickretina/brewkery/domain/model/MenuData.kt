package com.clickretina.brewkery.domain.model

data class MenuData(
    val meta: StoreMeta,
    val categories: List<Category>,
    val items: List<MenuItem>
)
