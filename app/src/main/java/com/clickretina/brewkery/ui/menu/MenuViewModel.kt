package com.clickretina.brewkery.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clickretina.brewkery.data.repository.CartRepository
import com.clickretina.brewkery.data.repository.OrderRepository
import com.clickretina.brewkery.domain.model.CartLine
import com.clickretina.brewkery.domain.model.MenuData
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.domain.model.PlacedOrder
import com.clickretina.brewkery.domain.repository.MenuRepository
import com.clickretina.brewkery.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MenuUiState {
    data object Loading : MenuUiState
    data class Success(val data: MenuData) : MenuUiState
    data class Error(val message: String) : MenuUiState
}

class MenuViewModel(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("ALL")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val activeOrder: StateFlow<PlacedOrder?> = orderRepository.activeOrder

    val cartLines: StateFlow<List<CartLine>> = cartRepository.cartLines

    val cartCount: StateFlow<Int> = cartRepository.cartLines
        .map { lines -> lines.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal: StateFlow<Double> = cartRepository.cartLines
        .map { lines -> lines.sumOf { it.lineTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val filteredItems: StateFlow<List<MenuItem>> = combine(
        _uiState,
        _selectedCategoryId,
        _searchQuery
    ) { state, categoryId, query ->
        if (state !is MenuUiState.Success) {
            emptyList()
        } else {
            state.data.items.filter { item ->
                val matchesCategory = categoryId == "ALL" || item.categoryId == categoryId
                val matchesQuery = query.isBlank() ||
                        item.name.contains(query, ignoreCase = true) ||
                        item.tagline.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true) ||
                        item.ingredients.any { it.contains(query, ignoreCase = true) }
                matchesCategory && matchesQuery
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMenu()
    }

    fun loadMenu() {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            when (val result = menuRepository.getMenu()) {
                is Resource.Success -> {
                    _uiState.value = MenuUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _uiState.value = MenuUiState.Error(result.message)
                }
                is Resource.Loading -> {
                    _uiState.value = MenuUiState.Loading
                }
            }
        }
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    companion object {
        fun provideFactory(
            menuRepository: MenuRepository,
            cartRepository: CartRepository,
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MenuViewModel(menuRepository, cartRepository, orderRepository) as T
            }
        }
    }
}
