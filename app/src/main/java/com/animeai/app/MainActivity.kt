package com.animeai.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.animeai.app.ui.screens.CharacterCreationScreen
import com.animeai.app.ui.screens.CharacterScreen
import com.animeai.app.ui.theme.AnimeAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // 필요한 권한 설정
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    
    // 권한 요청 결과 처리
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // 모든 권한이 허용됨
        } else {
            // 일부 권한이 거부됨
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 권한 요청
        requestPermissionLauncher.launch(permissions)
        
        setContent {
            AnimeAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "character"
    ) {
        // 메인 캐릭터 화면
        composable("character") {
            val characterViewModel = hiltViewModel<com.animeai.app.viewmodel.CharacterViewModel>()
            
            CharacterScreen(
                viewModel = characterViewModel,
                onNavigateToCreation = {
                    navController.navigate("character_creation")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToStore = {
                    navController.navigate("store")
                }
            )
        }
        
        // 캐릭터 생성 화면
        composable("character_creation") {
            val creationViewModel = hiltViewModel<com.animeai.app.viewmodel.CharacterCreationViewModel>()
            
            CharacterCreationScreen(
                viewModel = creationViewModel,
                onNavigateBack = { 
                    navController.popBackStack() 
                },
                onCharacterCreated = {
                    navController.navigate("character") {
                        popUpTo("character_creation") { inclusive = true }
                    }
                }
            )
        }
        
        // 설정 화면 (아직 구현되지 않음)
        composable("settings") {
            // SettingsScreen(...)
        }
        
        // 스토어 화면 (아직 구현되지 않음)
        composable("store") {
            // StoreScreen(...)
        }
    }
}