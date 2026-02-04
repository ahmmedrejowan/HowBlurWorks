package com.rejown.howblurworks.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelSize
import com.rejown.howblurworks.presentation.home.HomeScreen
import com.rejown.howblurworks.presentation.result.ResultScreen
import com.rejown.howblurworks.presentation.visualization.VisualizationScreen

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Visualization : Screen("visualization/{imageUri}/{blurType}/{kernelSize}") {
        fun createRoute(imageUri: String, blurType: BlurType, kernelSize: KernelSize): String {
            val encodedUri = Uri.encode(imageUri)
            return "visualization/$encodedUri/${blurType.name}/${kernelSize.name}"
        }
    }
    data object Result : Screen("result")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartVisualization = { imageUri, blurType, kernelSize ->
                    navController.navigate(
                        Screen.Visualization.createRoute(imageUri, blurType, kernelSize)
                    )
                }
            )
        }

        composable(
            route = Screen.Visualization.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("blurType") { type = NavType.StringType },
                navArgument("kernelSize") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val blurType = backStackEntry.arguments?.getString("blurType")
                ?.let { BlurType.valueOf(it) } ?: BlurType.GAUSSIAN
            val kernelSize = backStackEntry.arguments?.getString("kernelSize")
                ?.let { KernelSize.valueOf(it) } ?: KernelSize.SMALL

            VisualizationScreen(
                imageUri = Uri.decode(imageUri),
                blurType = blurType,
                kernelSize = kernelSize,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate(Screen.Result.route) }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                onNavigateBack = { navController.popBackStack() },
                onTryAgain = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}
