package com.clickretina.brewkery.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.clickretina.brewkery.domain.model.Category
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.domain.model.PlacedOrder
import com.clickretina.brewkery.domain.model.StoreMeta
import com.clickretina.brewkery.ui.components.BadgePill
import com.clickretina.brewkery.ui.components.ErrorView
import com.clickretina.brewkery.ui.components.LoadingView
import com.clickretina.brewkery.ui.components.PriceText
import com.clickretina.brewkery.ui.theme.AmberAccent
import com.clickretina.brewkery.ui.theme.CremaBackground
import com.clickretina.brewkery.ui.theme.CremaBorder
import com.clickretina.brewkery.ui.theme.CremaCard
import com.clickretina.brewkery.ui.theme.CremaSurface
import com.clickretina.brewkery.ui.theme.EmeraldAccent
import com.clickretina.brewkery.ui.theme.EmeraldDark
import com.clickretina.brewkery.ui.theme.EmeraldLight
import com.clickretina.brewkery.ui.theme.EspressoBlack
import com.clickretina.brewkery.ui.theme.EspressoDark
import com.clickretina.brewkery.ui.theme.Terracotta
import com.clickretina.brewkery.ui.theme.TextMuted
import com.clickretina.brewkery.ui.theme.TextPrimary
import com.clickretina.brewkery.ui.theme.TextSecondary

@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val activeOrder by viewModel.activeOrder.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CremaBackground,
        bottomBar = {
            AnimatedVisibility(
                visible = cartCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                StickyCartBar(
                    cartCount = cartCount,
                    cartSubtotal = cartSubtotal,
                    onClick = onNavigateToCart,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is MenuUiState.Loading -> {
                    LoadingView()
                }
                is MenuUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadMenu() }
                    )
                }
                is MenuUiState.Success -> {
                    MenuContent(
                        meta = state.data.meta,
                        categories = state.data.categories,
                        items = filteredItems,
                        selectedCategoryId = selectedCategory,
                        searchQuery = searchQuery,
                        cartCount = cartCount,
                        activeOrder = activeOrder,
                        onCategorySelect = { viewModel.selectCategory(it) },
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onItemClick = onNavigateToDetail,
                        onCartClick = onNavigateToCart,
                        onActiveOrderClick = onNavigateToStatus
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuContent(
    meta: StoreMeta,
    categories: List<Category>,
    items: List<MenuItem>,
    selectedCategoryId: String,
    searchQuery: String,
    cartCount: Int,
    activeOrder: PlacedOrder?,
    onCategorySelect: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onActiveOrderClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Header with Shop Branding & Live Cart Button
        item {
            MenuHeader(
                cartCount = cartCount,
                onCartClick = onCartClick
            )
        }

        // 2. Dynamic Store Info or Active Order Card
        item {
            StoreInfoBanner(
                meta = meta,
                activeOrder = activeOrder,
                onBannerClick = {
                    if (activeOrder != null) {
                        onActiveOrderClick()
                    }
                }
            )
        }

        // 3. Search Bar
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange
            )
        }

        // 4. Categories Row
        item {
            CategoryChipsRow(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelect = onCategorySelect
            )
        }

        // 5. Item List or Empty State
        if (items.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No items found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your category or search query",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(items, key = { it.id }) { item ->
                MenuItemCard(
                    item = item,
                    currencySymbol = meta.currencySymbol,
                    onClick = { onItemClick(item.id) }
                )
            }
        }

        // Spacing to ensure bottom items aren't occluded by sticky bar
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun MenuHeader(
    cartCount: Int,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(EspressoBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BK",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Column {
                Text(
                    text = "Fresh Roast & Bakes",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Text(
                    text = "Brewkery Artisans",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }
        }

        // Header Cart Button with live counter badge
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(CremaSurface)
                .border(1.dp, CremaBorder, CircleShape)
                .clickable(onClick = onCartClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "View Cart",
                tint = EspressoBlack,
                modifier = Modifier.size(18.dp)
            )
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Terracotta),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (cartCount > 99) "99+" else cartCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreInfoBanner(
    meta: StoreMeta,
    activeOrder: PlacedOrder?,
    onBannerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveOrder = activeOrder != null
    val backgroundBrush = if (hasActiveOrder) {
        Brush.horizontalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFFAEEE5), Color(0xFFF7EBE1)))
    }
    val borderColor = if (hasActiveOrder) EmeraldAccent else CremaBorder

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = hasActiveOrder, onClick = onBannerClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .background(backgroundBrush)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CremaSurface)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (hasActiveOrder) "🟢" else "🛵",
                        fontSize = 16.sp
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (hasActiveOrder) "ACTIVE ORDER" else "STORE INFO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (hasActiveOrder) EmeraldDark else Terracotta
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (hasActiveOrder) EmeraldDark else Terracotta)
                        )
                    }
                    Text(
                        text = if (hasActiveOrder) {
                            "Order ${activeOrder?.ticketId ?: ""}"
                        } else {
                            "Delivery in ${meta.estimatedDeliveryTime}"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (hasActiveOrder) {
                            "Preparing • Arriving in ${activeOrder?.eta ?: meta.estimatedDeliveryTime}"
                        } else {
                            "${meta.currencySymbol}%.2f flat fee".format(meta.deliveryFee)
                        },
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                color = if (hasActiveOrder) EmeraldDark else CremaSurface,
                shape = RoundedCornerShape(8.dp),
                border = if (hasActiveOrder) null else androidx.compose.foundation.BorderStroke(1.dp, CremaBorder)
            ) {
                Text(
                    text = if (hasActiveOrder) "Track" else "Open",
                    color = if (hasActiveOrder) Color.White else TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search roast, cold brew, pastry...",
                color = TextSecondary,
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CremaSurface,
            unfocusedContainerColor = CremaSurface,
            disabledContainerColor = CremaSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CremaBorder, RoundedCornerShape(14.dp))
    )
}

@Composable
private fun CategoryChipsRow(
    categories: List<Category>,
    selectedCategoryId: String,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All Items" Chip
        item {
            val isSelected = selectedCategoryId == "ALL"
            CategoryChip(
                label = "All Items",
                isSelected = isSelected,
                onClick = { onCategorySelect("ALL") }
            )
        }

        items(categories, key = { it.id }) { cat ->
            val isSelected = selectedCategoryId == cat.id
            val label = if (cat.icon.isNotBlank()) "${cat.icon} ${cat.name}" else cat.name
            CategoryChip(
                label = label,
                isSelected = isSelected,
                onClick = { onCategorySelect(cat.id) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) EspressoBlack else CremaSurface
    val border = if (isSelected) EspressoBlack else CremaBorder
    val textColor = if (isSelected) Color.White else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CremaSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CremaBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CremaCard)
            )

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (!item.badge.isNullOrBlank()) {
                    BadgePill(text = item.badge)
                }

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = AmberAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "%.2f".format(item.rating),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "(${item.reviewCount})",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriceText(
                        price = item.basePrice,
                        currencySymbol = currencySymbol,
                        fontSize = 14.sp
                    )

                    Surface(
                        color = Terracotta,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+ Customize",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StickyCartBar(
    cartCount: Int,
    cartSubtotal: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EspressoBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Terracotta),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cartCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column {
                    Text(
                        text = "View Your Cart",
                        color = CremaBorder,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$%.2f".format(cartSubtotal),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Proceed to Checkout",
                    color = AmberAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
