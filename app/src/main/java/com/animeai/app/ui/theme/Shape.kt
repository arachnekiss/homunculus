package com.animeai.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// 앱에서 사용할 UI 모양 정의
val Shapes = Shapes(
    // 작은 컴포넌트 (버튼, 텍스트 필드 등)
    small = RoundedCornerShape(12.dp),
    
    // 중간 크기 컴포넌트 (카드, 대화 버블 등)
    medium = RoundedCornerShape(16.dp),
    
    // 큰 컴포넌트 (모달, 다이얼로그 등)
    large = RoundedCornerShape(24.dp)
)