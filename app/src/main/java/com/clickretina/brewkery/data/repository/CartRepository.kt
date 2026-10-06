package com.clickretina.brewkery.data.repository

import com.clickretina.brewkery.domain.model.CartLine
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.domain.model.Selection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartRepository {

    private val _cartLines = MutableStateFlow<List<CartLine>>(emptyList())
    val cartLines: StateFlow<List<CartLine>> = _cartLines.asStateFlow()

    fun addToCart(item: MenuItem, selection: Selection, quantity: Int) {
        if (quantity <= 0) return
        val newLine = CartLine(item, selection, quantity)
        val targetKey = newLine.key

        _cartLines.update { currentLines ->
            val existingIndex = currentLines.indexOfFirst { it.key == targetKey }
            if (existingIndex >= 0) {
                currentLines.mapIndexed { index, line ->
                    if (index == existingIndex) {
                        line.copy(quantity = line.quantity + quantity)
                    } else {
                        line
                    }
                }
            } else {
                currentLines + newLine
            }
        }
    }

    fun updateQuantity(key: String, delta: Int) {
        _cartLines.update { currentLines ->
            currentLines.mapNotNull { line ->
                if (line.key == key) {
                    val newQty = line.quantity + delta
                    if (newQty > 0) line.copy(quantity = newQty) else null
                } else {
                    line
                }
            }
        }
    }

    fun removeLine(key: String) {
        _cartLines.update { currentLines ->
            currentLines.filterNot { it.key == key }
        }
    }

    fun clearCart() {
        _cartLines.value = emptyList()
    }

    fun getTotalCount(): Int {
        return _cartLines.value.sumOf { it.quantity }
    }
}
