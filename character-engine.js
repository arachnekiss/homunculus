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
    if (this.options.characterElement && this.emotionImageMap[emotion]) {
      // 부드러운 전환 효과
      this.options.characterElement.style.transition = `opacity ${this.options.emotionTransitionDuration / 1000}s ease`;
      this.options.characterElement.style.opacity = 0;
      
      setTimeout(() => {
        this.options.characterElement.src = this.emotionImageMap[emotion];
        this.options.characterElement.style.opacity = 1;
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
  }
  
  /**
   * 말하기 애니메이션 중단
   */
  stopSpeakingAnimation() {
    if (!this.options.characterElement) return;
    
    this.state.isSpeaking = false;
    this.options.characterElement.classList.remove('speaking');
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