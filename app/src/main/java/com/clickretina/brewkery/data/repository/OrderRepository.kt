package com.clickretina.brewkery.data.repository

import com.clickretina.brewkery.domain.model.PlacedOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderRepository {

    private val _activeOrder = MutableStateFlow<PlacedOrder?>(null)
    val activeOrder: StateFlow<PlacedOrder?> = _activeOrder.asStateFlow()

    fun savePlacedOrder(order: PlacedOrder) {
        _activeOrder.value = order
    }

    fun clearActiveOrder() {
        _activeOrder.value = null
    }
}
