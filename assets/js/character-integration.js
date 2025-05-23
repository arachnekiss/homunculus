/**
 * AnimeAI 캐릭터 통합 컨트롤러
 * 애니메이션 엔진, 얼굴 감지, 감정 분석을 통합합니다.
 */

class CharacterIntegrationController {
  constructor(options = {}) {
    this.options = {
      characterImageSelector: '#characterMainImage',  // 캐릭터 이미지 요소 선택자
      canvasContainerSelector: '#canvasContainer',    // 캔버스 컨테이너 선택자
      speechBubbleSelector: '#speechBubble',          // 말풍선 요소 선택자
      emotionBadgeSelector: '#emotionBadge',          // 감정 뱃지 요소 선택자
      width: 512,                                     // 캔버스 너비
      height: 512,                                    // 캔버스 높이
      useAdvancedAnimation: true,                     // 고급 애니메이션 사용 여부
      defaultEmotion: 'neutral',                      // 기본 감정
      apiEndpoint: '/api/emotion-analyze',            // 감정 분석 API 엔드포인트
      ...options
    };
    
    // 컴포넌트 참조
    this.animationEngine = null;
    this.faceDetector = null;
    this.emotionAnalyzer = null;
    
    // DOM 요소
    this.characterImage = null;
    this.canvas = null;
    this.canvasContainer = null;
    this.speechBubble = null;
    this.emotionBadge = null;
    
    // 상태
    this.state = {
      isInitialized: false,
      currentEmotion: this.options.defaultEmotion,
      characterImageLoaded: false
    };
    
    // 초기화
    this.initialize();
  }
  
  /**
   * 컨트롤러 초기화
   */
  async initialize() {
    // DOM 요소 초기화
    this.characterImage = document.querySelector(this.options.characterImageSelector);
    this.canvasContainer = document.querySelector(this.options.canvasContainerSelector);
    this.speechBubble = document.querySelector(this.options.speechBubbleSelector);
    this.emotionBadge = document.querySelector(this.options.emotionBadgeSelector);
    
    if (!this.characterImage) {
      console.error('캐릭터 이미지 요소를 찾을 수 없습니다:', this.options.characterImageSelector);
      return;
    }
    
    // 캔버스 생성
    this.createCanvas();
    
    // 얼굴 감지기 초기화
    this.faceDetector = new FaceDetector();
    
    // 감정 분석기 초기화
    this.emotionAnalyzer = new EmotionAnalyzer({
      apiEndpoint: this.options.apiEndpoint,
      enableTextAnalysis: true,
      enableImageAnalysis: false,  // 서버 측 이미지 분석은 비활성화 (로컬에서 처리)
      useCaching: true
    });
    
    // 이미지 로드 이벤트 처리
    if (this.characterImage.complete) {
      this.onCharacterImageLoaded();
    } else {
      this.characterImage.addEventListener('load', () => this.onCharacterImageLoaded());
    }
    
    // 초기화 완료
    this.state.isInitialized = true;
    console.log('캐릭터 통합 컨트롤러 초기화 완료');
  }
  
  /**
   * 캔버스 생성
   */
  createCanvas() {
    if (!this.canvasContainer) {
      console.error('캔버스 컨테이너를 찾을 수 없습니다:', this.options.canvasContainerSelector);
      return;
    }
    
    // 기존 캔버스 제거
    this.canvasContainer.innerHTML = '';
    
    // 캔버스 생성
    this.canvas = document.createElement('canvas');
    this.canvas.className = 'character-canvas';
    this.canvas.width = this.options.width;
    this.canvas.height = this.options.height;
    this.canvas.style.width = '100%';
    this.canvas.style.height = 'auto';
    this.canvas.style.display = 'block';
    
    // 컨테이너에 추가
    this.canvasContainer.appendChild(this.canvas);
  }
  
  /**
   * 캐릭터 이미지 로드 완료 처리
   */
  async onCharacterImageLoaded() {
    this.state.characterImageLoaded = true;
    
    // 이미지가 로드되면 얼굴 특징점 감지
    const features = await this.faceDetector.detectFaceFeatures(this.characterImage, {
      width: this.options.width,
      height: this.options.height
    });
    
    console.log('얼굴 특징점 감지 결과:', features);
    
    // 애니메이션 엔진 초기화
    if (this.options.useAdvancedAnimation) {
      // Canvas 기반 고급 애니메이션 엔진 사용
      this.animationEngine = new CanvasAnimationEngine({
        canvasElement: this.canvas,
        imageElement: this.characterImage,
        width: this.options.width,
        height: this.options.height,
        defaultEmotion: this.options.defaultEmotion
      });
    } else {
      // 기존 CSS 기반 애니메이션 엔진 사용
      this.animationEngine = new CharacterEngine({
        characterElement: this.characterImage,
        speechBubbleElement: this.speechBubble,
        emotionBadgeElement: this.emotionBadge,
        defaultEmotion: this.options.defaultEmotion
      });
      
      // 기본 이미지 설정
      const baseImageUrl = this.characterImage.src;
      this.animationEngine.setEmotionImages({
        'happy': baseImageUrl,
        'sad': baseImageUrl,
        'angry': baseImageUrl,
        'surprised': baseImageUrl,
        'embarrassed': baseImageUrl,
        'thoughtful': baseImageUrl,
        'excited': baseImageUrl,
        'nervous': baseImageUrl,
        'neutral': baseImageUrl
      });
    }
    
    // 원본 이미지 숨기기 (캔버스로 대체)
    if (this.options.useAdvancedAnimation) {
      this.characterImage.style.display = 'none';
    }
    
    console.log('캐릭터 애니메이션 엔진 초기화 완료');
  }
  
  /**
   * 텍스트 기반 감정 분석 및 적용
   * @param {string} text - 분석할 텍스트
   * @returns {Promise<Object>} 감정 분석 결과
   */
  async analyzeAndApplyTextEmotion(text) {
    if (!this.state.isInitialized || !this.emotionAnalyzer) {
      return null;
    }
    
    // 텍스트 감정 분석
    const emotionResult = await this.emotionAnalyzer.analyzeText(text);
    console.log('텍스트 감정 분석 결과:', emotionResult);
    
    // 감정 적용
    if (emotionResult && emotionResult.primary) {
      this.changeEmotion(emotionResult.primary);
    }
    
    return emotionResult;
  }
  
  /**
   * 캐릭터 감정 변경
   * @param {string} emotion - 변경할 감정
   */
  changeEmotion(emotion) {
    if (!this.state.isInitialized || !this.animationEngine) {
      return;
    }
    
    this.state.currentEmotion = emotion;
    this.animationEngine.changeEmotion(emotion);
  }
  
  /**
   * 메시지 처리 및 표시
   * @param {string} text - 표시할 메시지
   * @param {string} [emotion] - 지정할 감정 (없으면 자동 분석)
   * @returns {Promise<number>} 말하는 시간 (ms)
   */
  async speak(text, emotion = null) {
    if (!this.state.isInitialized || !this.animationEngine) {
      return 0;
    }
    
    // 감정이 지정되지 않았으면 자동 분석
    if (!emotion) {
      const emotionResult = await this.analyzeAndApplyTextEmotion(text);
      if (emotionResult) {
        emotion = emotionResult.primary;
      } else {
        emotion = this.state.currentEmotion;
      }
    }
    
    // 애니메이션 엔진을 통해 텍스트 표시
    return this.animationEngine.startSpeaking(text);
  }
  
  /**
   * 말하기 중단
   */
  stopSpeaking() {
    if (this.animationEngine) {
      this.animationEngine.stopSpeaking();
    }
  }
  
  /**
   * 감정 이름 한글 변환
   * @param {string} emotion - 영문 감정명
   * @returns {string} 한글 감정명
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
  
  /**
   * 리소스 해제
   */
  dispose() {
    if (this.animationEngine) {
      this.animationEngine.dispose();
      this.animationEngine = null;
    }
    
    this.state.isInitialized = false;
  }
}

// 글로벌 액세스를 위해 window 객체에 추가
window.CharacterIntegrationController = CharacterIntegrationController;