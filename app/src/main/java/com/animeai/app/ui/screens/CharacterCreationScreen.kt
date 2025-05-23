package com.animeai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animeai.app.model.AnimeStyle
import com.animeai.app.model.Personality
import com.animeai.app.model.SpeechStyle
import com.animeai.app.ui.components.AnimatedBackground
import com.animeai.app.viewmodel.CharacterCreationViewModel

/**
 * Screen for creating a new anime character
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    viewModel: CharacterCreationViewModel,
    onNavigateBack: () -> Unit,
    onCharacterCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect state from ViewModel
    val isGenerating by viewModel.isGenerating.collectAsState()
    val cost by viewModel.generationCost.collectAsState()
    val remaining by viewModel.remainingCredits.collectAsState()
    
    var prompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(AnimeStyle.MODERN) }
    var selectedPersonality by remember { mutableStateOf(Personality.FRIENDLY) }
    var selectedSpeechStyle by remember { mutableStateOf(SpeechStyle.CASUAL) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새로운 AI 친구 만들기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2D3047),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
            // Animated background
            AnimatedBackground(modifier = Modifier.fillMaxSize())
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Character description input
                Text(
                    text = "캐릭터 설명",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("예: 분홍색 머리에 고양이 귀를 가진 활발한 소녀") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0x552D3047),
                        focusedContainerColor = Color(0x552D3047),
                        unfocusedIndicatorColor = Color(0xFF5762D5),
                        focusedIndicatorColor = Color(0xFF8E6FE4)
                    )
                )
                
                // Style selection
                Text(
                    text = "애니메이션 스타일",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
                
                AnimeStyleSelector(
                    selectedStyle = selectedStyle,
                    onStyleSelected = { selectedStyle = it }
                )
                
                // Personality selection
                Text(
                    text = "성격",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )
                
                PersonalitySelector(
                    selectedPersonality = selectedPersonality,
                    onPersonalitySelected = { selectedPersonality = it }
                )
                
                // Speech style selection
                Text(
                    text = "대화 스타일",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )
                
                SpeechStyleSelector(
                    selectedSpeechStyle = selectedSpeechStyle,
                    onSpeechStyleSelected = { selectedSpeechStyle = it }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Generate button
                Button(
                    onClick = { 
                        viewModel.generateCharacter(
                            prompt = prompt,
                            style = selectedStyle,
                            personality = selectedPersonality,
                            speechStyle = selectedSpeechStyle
                        )
                        onCharacterCreated()
                    },
                    enabled = !isGenerating && prompt.isNotBlank() && remaining >= cost,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = if (isGenerating) "생성 중..." else "캐릭터 생성 ($cost 크레딧)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Credit warning if insufficient
                if (remaining < cost) {
                    Text(
                        text = "크레딧이 부족합니다. 크레딧을 추가로 구매해주세요.",
                        color = Color(0xFFF44336),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AnimeStyleSelector(
    selectedStyle: AnimeStyle,
    onStyleSelected: (AnimeStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AnimeStyle.values().forEach { style ->
            StyleItem(
                style = style,
                isSelected = style == selectedStyle,
                onSelect = { onStyleSelected(style) }
            )
        }
    }
}

@Composable
fun StyleItem(
    style: AnimeStyle,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF5762D5) else Color(0x552D3047)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = style.displayName,
                color = Color.White,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF5762D5)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalitySelector(
    selectedPersonality: Personality,
    onPersonalitySelected: (Personality) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Personality.values().take(4).forEach { personality ->
                PersonalityChip(
                    personality = personality,
                    isSelected = personality == selectedPersonality,
                    onSelect = { onPersonalitySelected(personality) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Personality.values().drop(4).forEach { personality ->
                PersonalityChip(
                    personality = personality,
                    isSelected = personality == selectedPersonality,
                    onSelect = { onPersonalitySelected(personality) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PersonalityChip(
    personality: Personality,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0xFF5762D5) else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF5762D5) else Color(0xFF5762D5),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = personality.displayName,
            color = if (isSelected) Color.White else Color(0xFFCCCCCC),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SpeechStyleSelector(
    selectedSpeechStyle: SpeechStyle,
    onSpeechStyleSelected: (SpeechStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            SpeechStyle.values().take(4).forEach { style ->
                SpeechStyleChip(
                    speechStyle = style,
                    isSelected = style == selectedSpeechStyle,
                    onSelect = { onSpeechStyleSelected(style) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            SpeechStyle.values().drop(4).take(3).forEach { style ->
                SpeechStyleChip(
                    speechStyle = style,
                    isSelected = style == selectedSpeechStyle,
                    onSelect = { onSpeechStyleSelected(style) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SpeechStyleChip(
    speechStyle: SpeechStyle,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0xFF8E6FE4) else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF8E6FE4) else Color(0xFF8E6FE4),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = speechStyle.displayName,
            color = if (isSelected) Color.White else Color(0xFFCCCCCC),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}