package com.clickretina.brewkery.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.domain.model.MilkOption
import com.clickretina.brewkery.domain.model.SizeOption
import com.clickretina.brewkery.ui.components.BadgePill
import com.clickretina.brewkery.ui.components.ErrorView
import com.clickretina.brewkery.ui.components.LoadingView
import com.clickretina.brewkery.ui.components.OptionSelectionRow
import com.clickretina.brewkery.ui.components.QuantityStepper
import com.clickretina.brewkery.ui.theme.AmberAccent
import com.clickretina.brewkery.ui.theme.CremaBackground
import com.clickretina.brewkery.ui.theme.CremaBorder
import com.clickretina.brewkery.ui.theme.CremaCard
import com.clickretina.brewkery.ui.theme.CremaSurface
import com.clickretina.brewkery.ui.theme.EspressoBlack
import com.clickretina.brewkery.ui.theme.Terracotta
import com.clickretina.brewkery.ui.theme.TextMuted
import com.clickretina.brewkery.ui.theme.TextPrimary
import com.clickretina.brewkery.ui.theme.TextSecondary
import com.clickretina.brewkery.util.PriceFormatter

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedSize by viewModel.selectedSize.collectAsStateWithLifecycle()
    val selectedMilk by viewModel.selectedMilk.collectAsStateWithLifecycle()
    val selectedSugar by viewModel.selectedSugar.collectAsStateWithLifecycle()
    val quantity by viewModel.quantity.collectAsStateWithLifecycle()
    val currentTotal by viewModel.currentTotal.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CremaBackground,
        bottomBar = {
            if (uiState is DetailUiState.Success) {
                DetailBottomBar(
                    totalPrice = currentTotal,
                    onAddToCart = {
                        viewModel.addToCart(onSuccess = onNavigateToCart)
                    },
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
                is DetailUiState.Loading -> {
                    LoadingView(message = "Brewing item details...")
                }
                is DetailUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadItem() }
                    )
                }
                is DetailUiState.Success -> {
                    DetailContent(
                        item = state.item,
                        selectedSize = selectedSize,
                        selectedMilk = selectedMilk,
                        selectedSugar = selectedSugar,
                        quantity = quantity,
                        isFavorite = isFavorite,
                        onSelectSize = { viewModel.selectSize(it) },
                        onSelectMilk = { viewModel.selectMilk(it) },
                        onSelectSugar = { viewModel.selectSugar(it) },
                        onQuantityChange = { viewModel.updateQuantity(it) },
                        onToggleFavorite = { viewModel.toggleFavorite() },
                        onBackClick = onNavigateBack
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    item: MenuItem,
    selectedSize: SizeOption?,
    selectedMilk: MilkOption?,
    selectedSugar: String?,
    quantity: Int,
    isFavorite: Boolean,
    onSelectSize: (SizeOption) -> Unit,
    onSelectMilk: (MilkOption) -> Unit,
    onSelectSugar: (String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val isBakery = item.categoryId == "cat_bakery"
    val milkSectionTitle = if (isBakery) "Spreads & Toppings" else "Milk Options"
    val sugarSectionTitle = if (isBakery) "Serving Temperature" else "Sugar Level"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CremaSurface)
                    .border(1.dp, CremaBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = EspressoBlack,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Item Customizer",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = EspressoBlack,
                letterSpacing = 0.5.sp
            )

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CremaSurface)
                    .border(1.dp, CremaBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Terracotta else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Hero Product Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CremaCard)
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (!item.badge.isNullOrBlank()) {
                Surface(
                    color = EspressoBlack.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = item.badge,
                        color = AmberAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Title and Tagline
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            if (item.tagline.isNotBlank()) {
                Text(
                    text = item.tagline,
                    fontSize = 13.sp,
                    color = Terracotta,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Key Metadata Pills (Rating, Prep Time, Calories)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetaInfoChip(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(14.dp)
                    )
                },
                text = "%.2f (%d)".format(item.rating, item.reviewCount)
            )

            if (item.prepTime.isNotBlank()) {
                MetaInfoChip(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    text = item.prepTime
                )
            }

            if (item.calories > 0) {
                MetaInfoChip(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Terracotta,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    text = "${item.calories} kcal"
                )
            }
        }

        // Description
        if (item.description.isNotBlank()) {
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }

        // Key Ingredients
        if (item.ingredients.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Key Ingredients",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.ingredients.forEach { ingredient ->
                        Surface(
                            color = CremaCard,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CremaBorder)
                        ) {
                            Text(
                                text = ingredient,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = CremaBorder.copy(alpha = 0.7f))

        // Customization 1: Size Selection
        if (item.sizes.isNotEmpty()) {
            OptionSelectionRow(
                title = "Choose Size",
                options = item.sizes,
                selectedOption = selectedSize,
                onOptionSelected = onSelectSize,
                optionLabel = { it.label },
                optionPriceHint = { opt ->
                    if (opt.extraPrice > 0.0) "+$%.2f".format(opt.extraPrice) else null
                }
            )
        }

        // Customization 2: Milk Options or Spreads
        if (item.milkOptions.isNotEmpty()) {
            OptionSelectionRow(
                title = milkSectionTitle,
                options = item.milkOptions,
                selectedOption = selectedMilk,
                onOptionSelected = onSelectMilk,
                optionLabel = { it.name },
                optionPriceHint = { opt ->
                    if (opt.extraPrice > 0.0) "+$%.2f".format(opt.extraPrice) else null
                }
            )
        }

        // Customization 3: Sugar Level or Serving
        if (item.sugarLevels.isNotEmpty()) {
            OptionSelectionRow(
                title = sugarSectionTitle,
                options = item.sugarLevels,
                selectedOption = selectedSugar,
                onOptionSelected = onSelectSugar,
                optionLabel = { it },
                optionPriceHint = { null }
            )
        }

        // Quantity Stepper Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Quantity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Select number of servings",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            QuantityStepper(
                quantity = quantity,
                onQuantityChange = onQuantityChange
            )
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun MetaInfoChip(
    icon: @Composable () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CremaSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CremaBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun DetailBottomBar(
    totalPrice: Double,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onAddToCart,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EspressoBlack),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Add to Cart",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "•",
                color = CremaBorder,
                fontSize = 15.sp
            )
            Text(
                text = PriceFormatter.formatPrice(totalPrice),
                color = AmberAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
