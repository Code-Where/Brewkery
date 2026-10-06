package com.clickretina.brewkery.ui.navigation

sealed class Screen(val route: String) {
    data object Menu : Screen("menu")
    data object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: Int) = "detail/$itemId"
    }
    data object Cart : Screen("cart")
    data object OrderStatus : Screen("status")
}
