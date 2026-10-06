package com.clickretina.brewkery.ui.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.clickretina.brewkery.domain.model.CartLine
import com.clickretina.brewkery.domain.model.OrderSummary
import com.clickretina.brewkery.domain.model.StoreMeta
import com.clickretina.brewkery.ui.components.PriceText
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
fun CartScreen(
    viewModel: CartViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartLines by viewModel.cartLines.collectAsStateWithLifecycle()
    val orderSummary by viewModel.orderSummary.collectAsStateWithLifecycle()
    val storeMeta by viewModel.storeMeta.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CremaBackground,
        bottomBar = {
            if (cartLines.isNotEmpty()) {
                CartBottomBar(
                    totalPrice = orderSummary.total,
                    onPlaceOrder = {
                        val placed = viewModel.placeOrder()
                        if (placed != null) {
                            onNavigateToStatus()
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            CartHeader(
                itemCount = cartLines.sumOf { it.quantity },
                onBackClick = onNavigateBack,
                onClearClick = { viewModel.clearCart() }
            )

            if (cartLines.isEmpty()) {
                EmptyCartView(onBrowseClick = onNavigateToMenu)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(cartLines, key = { it.key }) { line ->
                        CartLineCard(
                            line = line,
                            onQuantityChange = { delta ->
                                viewModel.updateQuantity(line.key, delta)
                            }
                        )
                    }

                    item {
                        BillSummaryCard(
                            summary = orderSummary,
                            meta = storeMeta
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CartHeader(
    itemCount: Int,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Cart & Checkout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = EspressoBlack
            )
            if (itemCount > 0) {
                Text(
                    text = "$itemCount items in basket",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        if (itemCount > 0) {
            TextButton(onClick = onClearClick) {
                Text(
                    text = "Clear",
                    color = Terracotta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Spacer(modifier = Modifier.size(38.dp))
        }
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CremaSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CremaBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            AsyncImage(
                model = line.item.imageUrl,
                contentDescription = line.item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CremaCard)
            )

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = line.item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                val optionsSummary = buildList {
                    add(line.selection.size.label)
                    if (line.selection.milk.name.isNotBlank()) add(line.selection.milk.name)
                    if (line.selection.sugar.isNotBlank()) add(line.selection.sugar)
                }.joinToString(" • ")

                Text(
                    text = optionsSummary,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )

                PriceText(
                    price = line.lineTotal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Stepper
            QuantityStepper(
                quantity = line.quantity,
                onQuantityChange = { newQty ->
                    val delta = newQty - line.quantity
                    onQuantityChange(delta)
                },
                min = 0 // Allow reducing to 0 to remove
            )
        }
    }
}

@Composable
private fun BillSummaryCard(
    summary: OrderSummary,
    meta: StoreMeta?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CremaSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CremaBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Bill Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = EspressoBlack
            )

            BillSummaryRow(
                label = "Subtotal",
                value = PriceFormatter.formatPrice(summary.subtotal)
            )

            BillSummaryRow(
                label = "Delivery Fee",
                value = PriceFormatter.formatPrice(summary.deliveryFee)
            )

            val taxPercentStr = meta?.taxRatePercent ?: summary.taxRatePercent
            BillSummaryRow(
                label = "Est. Tax (${taxPercentStr}%)",
                value = PriceFormatter.formatPrice(summary.tax)
            )

            HorizontalDivider(color = CremaBorder.copy(alpha = 0.8f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Payable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = PriceFormatter.formatPrice(summary.total),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Terracotta
                )
            }
        }
    }
}

@Composable
private fun BillSummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyCartView(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CremaCard)
                    .border(1.dp, CremaBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveShoppingCart,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your in-memory cart is empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Explore our fresh artisan roasts and bakery treats.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onBrowseClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EspressoBlack)
            ) {
                Text(
                    text = "Browse Menu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CartBottomBar(
    totalPrice: Double,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onPlaceOrder,
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
                text = "Place Order Now",
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
