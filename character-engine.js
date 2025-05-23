/**
 * AnimeAI Character Animation Engine
 * 캐릭터 표정과 움직임을 제어하는 엔진입니다.
 */

class CharacterEngine {
  constructor(options = {}) {
    this.options = {
      characterElement: null,
      speechBubbleElement: null,
      emotionBadgeElement: null,
      defaultEmotion: 'neutral',
      emotions: [
        'happy', 'sad', 'angry', 'surprised', 
        'embarrassed', 'thoughtful', 'excited', 'nervous', 'neutral'
      ],
      speakingAnimationDuration: 300,
      emotionTransitionDuration: 500,
      ...options
    };
    
    // 캐릭터 상태
    this.state = {
      currentEmotion: this.options.defaultEmotion,
      isSpeaking: false,
      animationTimerId: null,
      emotionImages: {},
      baseSpeakingFrames: 6,
      loaded: false
    };
    
    // 감정별 이미지 URL 매핑
    this.emotionImageMap = {};
    
    // 기본 이미지를 미리 로드하면 로딩 상태 표시 가능
    this.preloadImages();
  }
  
  /**
   * 이미지 URL 설정
   * @param {string} emotion - 감정 상태
   * @param {string} imageUrl - 해당 감정의 이미지 URL
   */
  setEmotionImage(emotion, imageUrl) {
    if (!this.options.emotions.includes(emotion)) {
      console.warn(`알 수 없는 감정: ${emotion}`);
      return;
    }
    
    this.emotionImageMap[emotion] = imageUrl;
    
    // 이미지 미리 로드
    const img = new Image();
    img.src = imageUrl;
    img.onload = () => {
      this.state.emotionImages[emotion] = img;
      console.log(`이미지 로드됨: ${emotion}`);
      
      // 모든 이미지가 로드되었는지 확인
      if (Object.keys(this.state.emotionImages).length >= this.options.emotions.length) {
        this.state.loaded = true;
        console.log('모든 이미지 로드 완료');
      }
    };
  }
  
  /**
   * 모든 감정에 대해 이미지 URL 일괄 설정
   * @param {Object} emotionMap - 감정과 URL 매핑 객체
   */
  setEmotionImages(emotionMap) {
    for (const [emotion, url] of Object.entries(emotionMap)) {
      this.setEmotionImage(emotion, url);
    }
  }
  
  /**
   * 감정 이미지 미리 로드
   */
  preloadImages() {
    // 초기에는 기본 감정 이미지만 설정
    if (this.options.characterElement) {
      const defaultImageUrl = this.options.characterElement.src;
      this.setEmotionImage('neutral', defaultImageUrl);
      
      // 기본 이미지를 다른 감정에도 임시 할당
      this.options.emotions.forEach(emotion => {
        if (emotion !== 'neutral') {
          this.setEmotionImage(emotion, defaultImageUrl);
        }
      });
    }
  }
  
  /**
   * 캐릭터 감정 변경
   * @param {string} emotion - 변경할 감정
   */
  changeEmotion(emotion) {
    if (!this.options.emotions.includes(emotion)) {
      console.warn(`지원되지 않는 감정: ${emotion}, 기본값을 사용합니다.`);
      emotion = this.options.defaultEmotion;
    }
    
    // 감정 뱃지 업데이트
    if (this.options.emotionBadgeElement) {
      this.options.emotionBadgeElement.textContent = this.translateEmotion(emotion);
      
      // 중립적 감정이 아닐 때만 표시
      if (emotion !== 'neutral') {
        this.options.emotionBadgeElement.style.display = 'block';
      } else {
        this.options.emotionBadgeElement.style.display = 'none';
      }
    }
    
    // 캐릭터 이미지 변경
    if (this.options.characterElement) {
      // 부드러운 전환 효과
      this.options.characterElement.style.transition = `all ${this.options.emotionTransitionDuration / 1000}s ease`;
      this.options.characterElement.style.opacity = 0.7;
      
      setTimeout(() => {
        // 감정에 따른 이미지 필터 적용 (한 이미지로 여러 감정 표현)
        const filter = this.getEmotionFilter(emotion);
        this.options.characterElement.style.filter = filter;
        
        // 감정에 따른 CSS 애니메이션 적용
        this.options.characterElement.classList.remove('emotion-happy', 'emotion-sad', 'emotion-angry', 'emotion-surprised');
        if (emotion !== 'neutral') {
          this.options.characterElement.classList.add(`emotion-${emotion}`);
        }
        
        // 화면에 다시 표시
        this.options.characterElement.style.opacity = 1;
        
        // 서로 다른 감정에 따라 이미지를 약간 기울이거나 위치 조정 (미세한 움직임)
        switch(emotion) {
          case 'happy':
            this.options.characterElement.style.transform = 'translateY(-5px) rotate(1deg)';
            break;
          case 'sad':
            this.options.characterElement.style.transform = 'translateY(3px) rotate(-1deg)';
            break;
          case 'angry':
            this.options.characterElement.style.transform = 'translateY(-2px) rotate(-2deg)';
            break;
          case 'surprised':
            this.options.characterElement.style.transform = 'translateY(-8px) scale(1.02)';
            break;
          case 'embarrassed':
            this.options.characterElement.style.transform = 'translateY(0) rotate(1.5deg)';
            break;
          case 'thoughtful':
            this.options.characterElement.style.transform = 'translateY(2px) rotate(-0.5deg)';
            break;
          case 'excited':
            this.options.characterElement.style.transform = 'translateY(-6px) rotate(2deg) scale(1.03)';
            break;
          case 'nervous':
            this.options.characterElement.style.transform = 'translateY(1px) rotate(-1deg)';
            break;
          default:
            this.options.characterElement.style.transform = 'translateY(0) rotate(0)';
        }
      }, this.options.emotionTransitionDuration / 2);
    }
    
    this.state.currentEmotion = emotion;
  }
  
  /**
   * 대화 시작 - 말풍선과 애니메이션 표시
   * @param {string} text - 대화 내용
   * @param {string} emotion - 감정
   */
  speak(text, emotion = null) {
    // 감정이 지정되면 감정 변경
    if (emotion) {
      this.changeEmotion(emotion);
    }
    
    // 말풍선 표시
    if (this.options.speechBubbleElement) {
      const speechTextElement = this.options.speechBubbleElement.querySelector('.speech-text') || this.options.speechBubbleElement;
      
      if (speechTextElement) {
        speechTextElement.textContent = text;
      }
      
      this.options.speechBubbleElement.classList.add('visible');
    }
    
    // 말하기 애니메이션 시작
    this.startSpeakingAnimation();
    
    // 텍스트 길이에 비례하여 말하는 시간 설정
    const speakingDuration = text.length * 100 + 2000;
    
    // 이전 타이머가 있으면 제거
    if (this.state.animationTimerId) {
      clearTimeout(this.state.animationTimerId);
    }
    
    // 말하기 종료 타이머 설정
    this.state.animationTimerId = setTimeout(() => {
      this.stopSpeaking();
    }, speakingDuration);
    
    return speakingDuration;
  }
  
  /**
   * 말하기 중단
   */
  stopSpeaking() {
    // 말풍선 숨기기
    if (this.options.speechBubbleElement) {
      this.options.speechBubbleElement.classList.remove('visible');
    }
    
    // 애니메이션 중단
    this.stopSpeakingAnimation();
    
    // 타이머 초기화
    if (this.state.animationTimerId) {
      clearTimeout(this.state.animationTimerId);
      this.state.animationTimerId = null;
    }
  }
  
  /**
   * 말하기 애니메이션 시작
   */
  startSpeakingAnimation() {
    if (!this.options.characterElement) return;
    
    this.state.isSpeaking = true;
    this.options.characterElement.classList.add('speaking');
    
    // 말하기 애니메이션을 위한 미세한 움직임 추가
    let tiltCounter = 0;
    
    // 이전 애니메이션 타이머가 있으면 정리
    if (this.speakingAnimTimer) {
      clearInterval(this.speakingAnimTimer);
    }
    
    // 미세한 움직임을 위한 타이머 설정
    this.speakingAnimTimer = setInterval(() => {
      if (!this.state.isSpeaking) {
        clearInterval(this.speakingAnimTimer);
        return;
      }
      
      tiltCounter++;
      const currentTilt = Math.sin(tiltCounter * 0.2) * 0.5;
      const currentPos = Math.sin(tiltCounter * 0.3) * 2;
      
      // 현재 감정에 따른 기본 변형에 미세한 움직임 추가
      let baseTransform = 'translateY(0) rotate(0)';
      switch(this.state.currentEmotion) {
        case 'happy': 
          baseTransform = 'translateY(-5px) rotate(1deg)';
          break;
        case 'sad': 
          baseTransform = 'translateY(3px) rotate(-1deg)';
          break;
        case 'angry': 
          baseTransform = 'translateY(-2px) rotate(-2deg)';
          break;
        case 'surprised': 
          baseTransform = 'translateY(-8px) scale(1.02)';
          break;
        case 'embarrassed': 
          baseTransform = 'translateY(0) rotate(1.5deg)';
          break;
        // 다른 감정들에 대한 케이스 생략
      }
      
      // 기본 변형에 미세한 움직임 추가
      this.options.characterElement.style.transform = `${baseTransform} translateY(${currentPos}px) rotate(${currentTilt}deg)`;
    }, 50);
  }
  
  /**
   * 말하기 애니메이션 중단
   */
  stopSpeakingAnimation() {
    if (!this.options.characterElement) return;
    
    this.state.isSpeaking = false;
    this.options.characterElement.classList.remove('speaking');
    
    // 애니메이션 타이머 정리
    if (this.speakingAnimTimer) {
      clearInterval(this.speakingAnimTimer);
      this.speakingAnimTimer = null;
    }
    
    // 원래 감정 상태로 복원
    this.changeEmotion(this.state.currentEmotion);
  }
  
  /**
   * 고급 캐릭터 움직임 설정
   * @param {string} movementType - 움직임 유형 (예: 'wave', 'bow', 'nod')
   */
  animate(movementType) {
    if (!this.options.characterElement) return;
    
    // 기존 애니메이션 클래스 제거
    const animations = ['wave', 'bow', 'nod', 'jump'];
    animations.forEach(anim => {
      this.options.characterElement.classList.remove(anim);
    });
    
    // 새 애니메이션 적용
    if (animations.includes(movementType)) {
      this.options.characterElement.classList.add(movementType);
      
      // 애니메이션 종료 후 클래스 제거
      setTimeout(() => {
        this.options.characterElement.classList.remove(movementType);
      }, 1000);
    }
  }
  
  /**
   * 초기화 - 모든 상태 리셋
   */
  reset() {
    this.stopSpeaking();
    this.changeEmotion(this.options.defaultEmotion);
  }
  
  /**
   * 이미지 CSS 필터 생성 (캐릭터 감정 시각화)
   * @param {string} emotion - 감정
   * @returns {string} - CSS 필터 문자열
   */
  getEmotionFilter(emotion) {
    const filters = {
      'happy': 'brightness(1.1) saturate(1.2)',
      'sad': 'brightness(0.9) saturate(0.8) contrast(0.95)',
      'angry': 'brightness(1.1) saturate(1.3) contrast(1.1) hue-rotate(-5deg)',
      'surprised': 'brightness(1.15) contrast(1.05)',
      'neutral': 'brightness(1) saturate(1)',
      'embarrassed': 'brightness(1.05) saturate(1.1) hue-rotate(5deg)',
      'thoughtful': 'brightness(0.95) saturate(0.9) contrast(1.05)',
      'excited': 'brightness(1.2) saturate(1.3) contrast(1.1)',
      'nervous': 'brightness(0.9) saturate(0.95) contrast(1.05)'
    };
    
    return filters[emotion] || filters['neutral'];
  }
  
  /**
   * 감정 한글 번역
   * @param {string} emotion - 영문 감정명
   * @returns {string} - 한글 감정명
   */
  translateEmotion(emotion) {
    const emotions = {
      'happy': '행복😊',
      'sad': '슬픔😢',
      'angry': '화남😠',
      'surprised': '놀람😲',
      'neutral': '보통😐',
      'embarrassed': '당황😳',
      'thoughtful': '생각 중🤔',
      'excited': '신남😄',
      'nervous': '긴장😰'
    };
    
    return emotions[emotion] || emotion;
  }
}

// 글로벌 액세스를 위해 윈도우 객체에 추가
window.CharacterEngine = CharacterEngine;