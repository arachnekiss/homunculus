package com.animeai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.Emotion
import com.animeai.app.ui.components.AnimatedBackground
import com.animeai.app.ui.components.AnimatedCharacterView
import com.animeai.app.ui.components.ChatInterface
import com.animeai.app.ui.components.EmotionIndicator
import com.animeai.app.ui.components.UsageIndicator
import com.animeai.app.viewmodel.CharacterViewModel

/**
 * Main screen for interacting with the anime character
 */
@Composable
fun CharacterScreen(
    viewModel: CharacterViewModel,
    onNavigateToCreation: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect state from the ViewModel
    val character by viewModel.currentCharacter.collectAsState()
    val emotion by viewModel.currentEmotion.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val remainingCredits by viewModel.remainingCredits.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // 카메라 대화상자 표시 상태
    var showCameraDialog by remember { mutableStateOf(false) }
    
    // 키보드 컨트롤러 (IME)
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // 스낵바 호스트 상태
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 캐릭터가 없으면 생성 화면으로 이동
    LaunchedEffect(character) {
        if (character == null) {
            onNavigateToCreation()
        }
    }
    
    // 에러 발생 시 스낵바 표시
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // 카메라 대화상자 표시
    if (showCameraDialog) {
        CameraDialog(
            onDismiss = { showCameraDialog = false },
            onCameraCapture = {
                // 카메라로 사진 촬영 (실제 구현에서는 카메라 권한 등 처리)
                viewModel.captureUserExpression()
            },
            onGallerySelect = {
                // 갤러리에서 사진 선택 (실제 구현에서는 갤러리 접근 처리)
                viewModel.captureUserExpression()
            },
            onEmotionSelected = { selectedEmotion ->
                // 직접 감정 선택
                viewModel.simulateUserEmotion(selectedEmotion)
            }
        )
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { /* 앱 이름 없음 */ },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),
                actions = {
                    // 설정 버튼
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "설정",
                            tint = Color.White
                        )
                    }
                    
                    // 스토어 버튼
                    IconButton(onClick = onNavigateToStore) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingCart,
                            contentDescription = "스토어",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2D3047),
                            Color(0xFF1B1B25)
                        )
                    )
                )
        ) {
            // Animated background elements (particles, etc.)
            AnimatedBackground(modifier = Modifier.fillMaxSize())
            
            // Character view (upper 65-70% of screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .align(Alignment.TopCenter)
            ) {
                // 로딩 중이면 프로그레스 표시
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Animated character
                character?.let { char ->
                    AnimatedCharacterView(
                        character = char,
                        currentEmotion = emotion,
                        isSpeaking = isSpeaking,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Emotion indicator in top-right corner
                EmotionIndicator(
                    emotion = emotion,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
                
                // Credits indicator in top-left
                UsageIndicator(
                    credits = remainingCredits,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )
            }
            
            // Chat interface (lower 30-35% of screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x001B1B25),
                                Color(0xFF1B1B25)
                            )
                        )
                    )
                    .imePadding() // 키보드가 표시될 때 패딩 조정
            ) {
                ChatInterface(
                    messages = messages,
                    onTextInput = { 
                        viewModel.sendTextMessage(it)
                        keyboardController?.hide() // 메시지 전송 후 키보드 숨김
                    },
                    onVoiceInput = viewModel::startVoiceRecording,
                    onCameraInput = { showCameraDialog = true }, // 카메라 버튼 클릭 시 대화상자 표시
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Character Screen Preview
@Preview(showBackground = true)
@Composable
fun CharacterScreenPreview() {
    AnimeAITheme {
        val previewViewModel = PreviewCharacterViewModel()
        CharacterScreen(
            viewModel = previewViewModel,
            onNavigateToCreation = {},
            onNavigateToSettings = {},
            onNavigateToStore = {}
        )
    }
}

// Preview용 ViewModel
class PreviewCharacterViewModel : CharacterViewModel(
    conversationService = FakeConversationService(),
    characterEngine = FakeCharacterEngine(),
    voiceService = FakeVoiceService(),
    creditService = FakeCreditService()
)