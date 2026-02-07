package com.rejown.howblurworks.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rejown.howblurworks.domain.model.BlurIntensity
import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelSize
import com.rejown.howblurworks.presentation.home.HomeScreen
import com.rejown.howblurworks.presentation.result.ResultScreen
import com.rejown.howblurworks.presentation.settings.SettingsScreen
import com.rejown.howblurworks.presentation.visualization.VisualizationScreen

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Visualization : Screen("visualization/{imageUri}/{blurType}/{kernelSize}/{intensity}") {
        fun createRoute(imageUri: String, blurType: BlurType, kernelSize: KernelSize, intensity: BlurIntensity): String {
            val encodedUri = Uri.encode(imageUri)
            return "visualization/$encodedUri/${blurType.name}/${kernelSize.name}/${intensity.name}"
        }
    }
    data object Result : Screen("result")
    data object Settings : Screen("settings")
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
                onStartVisualization = { imageUri, blurType, kernelSize, intensity ->
                    navController.navigate(
                        Screen.Visualization.createRoute(imageUri, blurType, kernelSize, intensity)
                    )
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Visualization.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("blurType") { type = NavType.StringType },
                navArgument("kernelSize") { type = NavType.StringType },
                navArgument("intensity") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val blurType = backStackEntry.arguments?.getString("blurType")
                ?.let { BlurType.valueOf(it) } ?: BlurType.GAUSSIAN
            val kernelSize = backStackEntry.arguments?.getString("kernelSize")
                ?.let { KernelSize.valueOf(it) } ?: KernelSize.SIZE_3
            val intensity = backStackEntry.arguments?.getString("intensity")
                ?.let { BlurIntensity.valueOf(it) } ?: BlurIntensity.MEDIUM

            VisualizationScreen(
                imageUri = Uri.decode(imageUri),
                blurType = blurType,
                kernelSize = kernelSize,
                intensity = intensity,
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

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
