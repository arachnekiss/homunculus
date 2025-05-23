package com.animeai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.animeai.app.ui.screens.CharacterScreen
import com.animeai.app.ui.screens.SettingsScreen
import com.animeai.app.ui.screens.StoreScreen
import com.animeai.app.ui.theme.AnimeAITheme
import com.animeai.app.viewmodel.CharacterViewModel
import com.animeai.app.viewmodel.ConversationViewModel
import com.animeai.app.viewmodel.CreditViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimeAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // ViewModels
    val characterViewModel: CharacterViewModel = viewModel()
    val conversationViewModel: ConversationViewModel = viewModel()
    val creditViewModel: CreditViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = "character") {
        composable("character") {
            CharacterScreen(
                characterViewModel = characterViewModel,
                conversationViewModel = conversationViewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToStore = { navController.navigate("store") }
            )
        }
        composable("settings") {
            SettingsScreen(
                characterViewModel = characterViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("store") {
            StoreScreen(
                creditViewModel = creditViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
