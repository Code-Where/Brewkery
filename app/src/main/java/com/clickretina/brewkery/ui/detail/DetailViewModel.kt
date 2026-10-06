package com.clickretina.brewkery.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clickretina.brewkery.data.repository.CartRepository
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.domain.model.MilkOption
import com.clickretina.brewkery.domain.model.Selection
import com.clickretina.brewkery.domain.model.SizeOption
import com.clickretina.brewkery.domain.repository.MenuRepository
import com.clickretina.brewkery.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val item: MenuItem) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    private val itemId: Int,
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _selectedSize = MutableStateFlow<SizeOption?>(null)
    val selectedSize: StateFlow<SizeOption?> = _selectedSize.asStateFlow()

    private val _selectedMilk = MutableStateFlow<MilkOption?>(null)
    val selectedMilk: StateFlow<MilkOption?> = _selectedMilk.asStateFlow()

    private val _selectedSugar = MutableStateFlow<String?>(null)
    val selectedSugar: StateFlow<String?> = _selectedSugar.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    // Live calculated price: (base + sizeExtra + milkExtra) * qty
    val currentTotal: StateFlow<Double> = combine(
        _uiState,
        _selectedSize,
        _selectedMilk,
        _quantity
    ) { state, size, milk, qty ->
        if (state !is DetailUiState.Success) {
            0.0
        } else {
            val base = state.item.basePrice
            val sizeExtra = size?.extraPrice ?: 0.0
            val milkExtra = milk?.extraPrice ?: 0.0
            val unit = BigDecimal.valueOf(base)
                .add(BigDecimal.valueOf(sizeExtra))
                .add(BigDecimal.valueOf(milkExtra))
                .setScale(2, RoundingMode.HALF_UP)
            unit.multiply(BigDecimal.valueOf(qty.toLong()))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        loadItem()
    }

    fun loadItem() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            when (val result = menuRepository.getItem(itemId)) {
                is Resource.Success -> {
                    val item = result.data
                    _uiState.value = DetailUiState.Success(item)
                    // Auto-select first option in each group
                    if (_selectedSize.value == null && item.sizes.isNotEmpty()) {
                        _selectedSize.value = item.sizes.first()
                    }
                    if (_selectedMilk.value == null && item.milkOptions.isNotEmpty()) {
                        _selectedMilk.value = item.milkOptions.first()
                    }
                    if (_selectedSugar.value == null && item.sugarLevels.isNotEmpty()) {
                        _selectedSugar.value = item.sugarLevels.first()
                    }
                }
                is Resource.Error -> {
                    _uiState.value = DetailUiState.Error(result.message)
                }
                is Resource.Loading -> {
                    _uiState.value = DetailUiState.Loading
                }
            }
        }
    }

    fun selectSize(size: SizeOption) {
        _selectedSize.value = size
    }

    fun selectMilk(milk: MilkOption) {
        _selectedMilk.value = milk
    }

    fun selectSugar(sugar: String) {
        _selectedSugar.value = sugar
    }

    fun updateQuantity(qty: Int) {
        if (qty in 1..10) {
            _quantity.value = qty
        }
    }

    fun toggleFavorite() {
        _isFavorite.value = !_isFavorite.value
    }

    fun addToCart(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        val size = _selectedSize.value ?: state.item.sizes.firstOrNull() ?: SizeOption("default", "Standard", 0.0)
        val milk = _selectedMilk.value ?: state.item.milkOptions.firstOrNull() ?: MilkOption("default", "Standard", 0.0)
        val sugar = _selectedSugar.value ?: state.item.sugarLevels.firstOrNull() ?: "Standard"

        val selection = Selection(size = size, milk = milk, sugar = sugar)
        cartRepository.addToCart(state.item, selection, _quantity.value)

        viewModelScope.launch {
            _eventFlow.emit("Added ${_quantity.value}x ${state.item.name} to cart")
        }
        onSuccess()
    }

    companion object {
        fun provideFactory(
            itemId: Int,
            menuRepository: MenuRepository,
            cartRepository: CartRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailViewModel(itemId, menuRepository, cartRepository) as T
            }
        }
    }
}
