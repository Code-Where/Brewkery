package com.clickretina.brewkery.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.clickretina.brewkery.di.AppContainer
import com.clickretina.brewkery.ui.cart.CartScreen
import com.clickretina.brewkery.ui.cart.CartViewModel
import com.clickretina.brewkery.ui.detail.DetailScreen
import com.clickretina.brewkery.ui.detail.DetailViewModel
import com.clickretina.brewkery.ui.menu.MenuScreen
import com.clickretina.brewkery.ui.menu.MenuViewModel
import com.clickretina.brewkery.ui.status.OrderStatusScreen

@Composable
fun BrewkeryNavGraph(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Menu.route,
        modifier = modifier
    ) {
        // Screen 1: Menu
        composable(
            route = Screen.Menu.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            val menuViewModel: MenuViewModel = viewModel(
                factory = MenuViewModel.provideFactory(
                    menuRepository = container.menuRepository,
                    cartRepository = container.cartRepository,
                    orderRepository = container.orderRepository
                )
            )

            MenuScreen(
                viewModel = menuViewModel,
                onNavigateToDetail = { itemId ->
                    navController.navigate(Screen.Detail.createRoute(itemId))
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                onNavigateToStatus = {
                    navController.navigate(Screen.OrderStatus.route)
                }
            )
        }

        // Screen 2: Item Detail
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.IntType
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 1
            val detailViewModel: DetailViewModel = viewModel(
                key = "detail_$itemId",
                factory = DetailViewModel.provideFactory(
                    itemId = itemId,
                    menuRepository = container.menuRepository,
                    cartRepository = container.cartRepository
                )
            )

            DetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }

        // Screen 3: Cart
        composable(
            route = Screen.Cart.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            }
        ) {
            val cartViewModel: CartViewModel = viewModel(
                factory = CartViewModel.provideFactory(
                    cartRepository = container.cartRepository,
                    orderRepository = container.orderRepository,
                    menuRepository = container.menuRepository,
                    calculateOrderSummary = container.calculateOrderSummary
                )
            )

            CartScreen(
                viewModel = cartViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMenu = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = false }
                    }
                },
                onNavigateToStatus = {
                    navController.navigate(Screen.OrderStatus.route) {
                        popUpTo(Screen.Menu.route) { inclusive = false }
                    }
                }
            )
        }

        // Screen 4: Order Status
        composable(
            route = Screen.OrderStatus.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            }
        ) {
            OrderStatusScreen(
                orderRepository = container.orderRepository,
                onBackToMenu = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
