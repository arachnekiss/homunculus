package com.animeai.app.model

/**
 * 테스트 및 개발 중 사용할 샘플 데이터
 */
object MockData {
    // 테스트용 캐릭터
    val testCharacter = AnimeCharacter(
        id = "test_001",
        baseImageUrl = "https://via.placeholder.com/512/FFD6E0/000000?text=Anime+Character",
        expressions = mapOf(
            Emotion.NEUTRAL to "https://via.placeholder.com/512/FFD6E0/000000?text=Neutral",
            Emotion.HAPPY to "https://via.placeholder.com/512/FFECA9/000000?text=Happy",
            Emotion.SAD to "https://via.placeholder.com/512/A9D1FF/000000?text=Sad",
            Emotion.ANGRY to "https://via.placeholder.com/512/FFA9A9/000000?text=Angry",
            Emotion.SURPRISED to "https://via.placeholder.com/512/D6FFEC/000000?text=Surprised",
            Emotion.EMBARRASSED to "https://via.placeholder.com/512/FFB9D9/000000?text=Embarrassed"
        ),
        persona = CharacterPersona(
            name = "미유",
            personality = Personality.FRIENDLY,
            speechStyle = SpeechStyle.CASUAL,
            backgroundStory = "활발하고 친절한 학생으로, 취미는 그림 그리기와 독서입니다.",
            interests = listOf("애니메이션", "그림 그리기", "독서", "요리"),
            voiceSettings = VoiceSettings(pitch = 1.2f, speed = 1.0f)
        ),
        creator = "AI",
        createdAt = System.currentTimeMillis()
    )
    
    // 샘플 대화 메시지
    val sampleMessages = listOf(
        Message(
            content = "안녕하세요! 저는 미유라고 해요. 반가워요!",
            role = Role.Assistant,
            emotion = Emotion.HAPPY
        ),
        Message(
            content = "안녕! 만나서 반가워. 오늘 날씨가 어때?",
            role = Role.User
        ),
        Message(
            content = "오늘은 날씨가 정말 좋아요! 맑고 따뜻한 하루네요. 어떤 계획이 있으신가요?",
            role = Role.Assistant,
            emotion = Emotion.HAPPY
        )
    )
    
    // 샘플 크레딧 패키지
    val sampleCreditPackages = listOf(
        CreditPackage(
            id = "credits_10",
            name = "기본 패키지",
            credits = 10,
            price = 1000f,
            description = "일상 대화를 위한 기본 크레딧"
        ),
        CreditPackage(
            id = "credits_50",
            name = "인기 패키지",
            credits = 50,
            price = 4500f,
            description = "가장 인기있는 패키지, 10% 할인"
        ),
        CreditPackage(
            id = "credits_100", 
            name = "프리미엄 패키지",
            credits = 100,
            price = 8000f,
            description = "열렬한 사용자를 위한 20% 할인"
        )
    )
}