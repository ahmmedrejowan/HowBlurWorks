package com.rejown.howblurworks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rejown.howblurworks.data.UserPreferencesRepository
import com.rejown.howblurworks.navigation.NavGraph
import com.rejown.howblurworks.ui.theme.HowBlurWorksTheme
import com.rejown.howblurworks.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesRepository = UserPreferencesRepository(applicationContext)

        setContent {
            val settings by preferencesRepository.settingsFlow.collectAsState(
                initial = com.rejown.howblurworks.data.AppSettings()
            )

            val themeMode = when (settings.themeMode) {
                0 -> ThemeMode.SYSTEM
                1 -> ThemeMode.LIGHT
                2 -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }

            HowBlurWorksTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
