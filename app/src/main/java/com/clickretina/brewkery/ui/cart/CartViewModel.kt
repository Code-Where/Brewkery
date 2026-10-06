package com.clickretina.brewkery.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clickretina.brewkery.data.repository.CartRepository
import com.clickretina.brewkery.data.repository.OrderRepository
import com.clickretina.brewkery.domain.model.CartLine
import com.clickretina.brewkery.domain.model.OrderStatus
import com.clickretina.brewkery.domain.model.OrderSummary
import com.clickretina.brewkery.domain.model.PlacedOrder
import com.clickretina.brewkery.domain.model.StoreMeta
import com.clickretina.brewkery.domain.repository.MenuRepository
import com.clickretina.brewkery.domain.usecase.CalculateOrderSummary
import com.clickretina.brewkery.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class CartViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val menuRepository: MenuRepository,
    private val calculateOrderSummary: CalculateOrderSummary
) : ViewModel() {

    private val _storeMeta = MutableStateFlow<StoreMeta?>(null)
    val storeMeta: StateFlow<StoreMeta?> = _storeMeta.asStateFlow()

    val cartLines: StateFlow<List<CartLine>> = cartRepository.cartLines

    val orderSummary: StateFlow<OrderSummary> = combine(
        cartRepository.cartLines,
        _storeMeta
    ) { lines, meta ->
        calculateOrderSummary(lines, meta)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        OrderSummary(0.0, 0.0, 8.0, 0.0, 0.0)
    )

    init {
        loadStoreMeta()
    }

    private fun loadStoreMeta() {
        viewModelScope.launch {
            when (val result = menuRepository.getMenu()) {
                is Resource.Success -> {
                    _storeMeta.value = result.data.meta
                }
                else -> {
                    // Fallback to default meta
                    _storeMeta.value = StoreMeta(
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
            }
        }
    }

    fun updateQuantity(key: String, delta: Int) {
        cartRepository.updateQuantity(key, delta)
    }

    fun removeLine(key: String) {
        cartRepository.removeLine(key)
    }

    fun clearCart() {
        cartRepository.clearCart()
    }

    /**
     * Places the order per PRD §5.4:
     * 1. Generates #BK- + 5 random digits
     * 2. Snapshots order (item count, total, eta) BEFORE clearing cart
     * 3. Clears cart
     * 4. Returns placed order for navigation
     */
    fun placeOrder(): PlacedOrder? {
        val lines = cartRepository.cartLines.value
        if (lines.isEmpty()) return null

        val summary = orderSummary.value
        val totalCount = lines.sumOf { it.quantity }
        val randomNum = Random.nextInt(10000, 99999)
        val ticketId = "#BK-$randomNum"
        val eta = _storeMeta.value?.estimatedDeliveryTime ?: "20 - 30 mins"

        val placedOrder = PlacedOrder(
            ticketId = ticketId,
            itemCount = totalCount,
            total = summary.total,
            status = OrderStatus.PREPARING,
            eta = eta
        )

        // Snapshot to OrderRepository
        orderRepository.savePlacedOrder(placedOrder)

        // Clear cart AFTER snapshot
        cartRepository.clearCart()

        return placedOrder
    }

    companion object {
        fun provideFactory(
            cartRepository: CartRepository,
            orderRepository: OrderRepository,
            menuRepository: MenuRepository,
            calculateOrderSummary: CalculateOrderSummary
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CartViewModel(
                    cartRepository,
                    orderRepository,
                    menuRepository,
                    calculateOrderSummary
                ) as T
            }
        }
    }
}
