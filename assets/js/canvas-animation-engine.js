/**
 * AnimeAI Canvas 기반 애니메이션 엔진
 * 단일 이미지로부터 복잡한 애니메이션을 구현합니다.
 */

class CanvasAnimationEngine {
  constructor(options = {}) {
    this.options = {
      canvasElement: null,     // 캔버스 요소
      imageElement: null,      // 원본 이미지 요소
      width: 512,              // 캔버스 너비
      height: 512,             // 캔버스 높이
      fps: 60,                 // 프레임률
      defaultEmotion: 'neutral',
      ...options
    };
    
    this.state = {
      isInitialized: false,
      currentEmotion: this.options.defaultEmotion,
      isAnimating: false,
      isSpeaking: false,
      animationFrame: null,
      lastFrameTime: 0,
      frameCount: 0,
      baseImage: null,
      emotionTransition: {
        startTime: 0,
        duration: 500,
        fromEmotion: 'neutral',
        toEmotion: 'neutral',
        progress: 1
      },
      featurePoints: null,  // 얼굴 특징점
      breathCycle: 0
    };
    
    // 감정별 변형 매핑
    this.emotionTransforms = {
      neutral: { 
        eyeScale: 1.0, 
        mouthCurve: 0, 
        blushOpacity: 0, 
        eyebrowAngle: 0,
        headTilt: 0,
        brightness: 1.0,
        saturation: 1.0
      },
      happy: { 
        eyeScale: 0.8, 
        mouthCurve: 0.3, 
        blushOpacity: 0.2, 
        eyebrowAngle: 0.1,
        headTilt: 0.05,
        brightness: 1.1,
        saturation: 1.1
      },
      sad: { 
        eyeScale: 1.1, 
        mouthCurve: -0.2, 
        blushOpacity: 0, 
        eyebrowAngle: -0.15,
        headTilt: -0.03,
        brightness: 0.9,
        saturation: 0.85
      },
      angry: { 
        eyeScale: 0.85, 
        mouthCurve: -0.3, 
        blushOpacity: 0.1, 
        eyebrowAngle: -0.3,
        headTilt: -0.08,
        brightness: 1.05,
        saturation: 1.2
      },
      surprised: { 
        eyeScale: 1.3, 
        mouthCurve: 0.1, 
        blushOpacity: 0.15, 
        eyebrowAngle: 0.25,
        headTilt: 0.07,
        brightness: 1.1,
        saturation: 1.05
      },
      embarrassed: { 
        eyeScale: 0.9, 
        mouthCurve: 0.05, 
        blushOpacity: 0.5, 
        eyebrowAngle: 0.05,
        headTilt: -0.04,
        brightness: 1.05,
        saturation: 1.1
      },
      thoughtful: { 
        eyeScale: 0.95, 
        mouthCurve: -0.05, 
        blushOpacity: 0, 
        eyebrowAngle: 0.1,
        headTilt: 0.06,
        brightness: 0.95,
        saturation: 0.9
      },
      excited: { 
        eyeScale: 1.05, 
        mouthCurve: 0.4, 
        blushOpacity: 0.3, 
        eyebrowAngle: 0.2,
        headTilt: 0.1,
        brightness: 1.15,
        saturation: 1.2
      },
      nervous: { 
        eyeScale: 1.1, 
        mouthCurve: -0.1, 
        blushOpacity: 0.2, 
        eyebrowAngle: -0.1,
        headTilt: -0.06,
        brightness: 0.95,
        saturation: 0.95
      }
    };
    
    // 말하기 애니메이션 패턴
    this.speakingPatterns = {
      amplitude: 0.2,      // 입 움직임 진폭
      frequency: 0.15,     // 입 움직임 빈도
      randomness: 0.3      // 랜덤성 정도
    };
    
    this.initialize();
  }
  
  /**
   * 엔진 초기화
   */
  initialize() {
    if (!this.options.canvasElement || !this.options.imageElement) {
      console.error('캔버스와 이미지 요소가 필요합니다.');
      return;
    }
    
    const canvas = this.options.canvasElement;
    canvas.width = this.options.width;
    canvas.height = this.options.height;
    this.ctx = canvas.getContext('2d');
    
    // 원본 이미지 로드
    this.loadBaseImage(this.options.imageElement.src);
    
    this.state.isInitialized = true;
  }
  
  /**
   * 기본 이미지 로드
   */
  loadBaseImage(imageSrc) {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.crossOrigin = 'anonymous';
      
      img.onload = () => {
        this.state.baseImage = img;
        
        // 이미지 로드되면 특징점 자동 추출 시도
        this.detectFeaturePoints();
        
        // 애니메이션 시작
        this.startAnimation();
        
        resolve(img);
      };
      
      img.onerror = (err) => {
        console.error('이미지 로드 실패:', err);
        reject(err);
      };
      
      img.src = imageSrc;
    });
  }
  
  /**
   * 얼굴 특징점 자동 감지 (간단한 구현)
   */
  detectFeaturePoints() {
    // 실제로는 ML 모델이나 OpenCV를 사용하여 특징점 추출
    // 여기서는 이미지 크기를 기반으로 간단하게 예측
    
    const width = this.options.width;
    const height = this.options.height;
    
    // 기본 특징점 (나중에 ML 모델로 대체)
    this.state.featurePoints = {
      face: {
        center: { x: width * 0.5, y: height * 0.4 },
        width: width * 0.65,
        height: height * 0.75
      },
      eyes: {
        left: { x: width * 0.4, y: height * 0.38, width: width * 0.1, height: width * 0.05 },
        right: { x: width * 0.6, y: height * 0.38, width: width * 0.1, height: width * 0.05 }
      },
      eyebrows: {
        left: { x: width * 0.4, y: height * 0.35, width: width * 0.12, angle: 0 },
        right: { x: width * 0.6, y: height * 0.35, width: width * 0.12, angle: 0 }
      },
      mouth: {
        center: { x: width * 0.5, y: height * 0.52 },
        width: width * 0.25,
        height: height * 0.05
      },
      cheeks: {
        left: { x: width * 0.35, y: height * 0.45 },
        right: { x: width * 0.65, y: height * 0.45 }
      }
    };
  }
  
  /**
   * 애니메이션 시작
   */
  startAnimation() {
    if (!this.state.isInitialized || !this.state.baseImage) return;
    
    this.state.isAnimating = true;
    this.state.lastFrameTime = performance.now();
    this.animateFrame();
  }
  
  /**
   * 애니메이션 정지
   */
  stopAnimation() {
    this.state.isAnimating = false;
    if (this.state.animationFrame) {
      cancelAnimationFrame(this.state.animationFrame);
      this.state.animationFrame = null;
    }
  }
  
  /**
   * 프레임 애니메이션
   */
  animateFrame() {
    if (!this.state.isAnimating) return;
    
    const now = performance.now();
    const elapsed = now - this.state.lastFrameTime;
    
    // 프레임 레이트 제한
    if (elapsed > (1000 / this.options.fps)) {
      this.state.lastFrameTime = now;
      this.state.frameCount++;
      
      // 호흡 사이클 업데이트
      this.state.breathCycle = (this.state.breathCycle + 0.02) % (Math.PI * 2);
      
      // 감정 트랜지션 처리
      this.updateEmotionTransition();
      
      // 프레임 렌더링
      this.renderFrame();
    }
    
    this.state.animationFrame = requestAnimationFrame(this.animateFrame.bind(this));
  }
  
  /**
   * 감정 변경
   */
  changeEmotion(emotion) {
    if (!this.emotionTransforms[emotion]) {
      console.warn(`지원되지 않는 감정: ${emotion}, 기본값으로 대체`);
      emotion = this.options.defaultEmotion;
    }
    
    // 이전 상태에서 새 상태로 트랜지션 설정
    this.state.emotionTransition = {
      startTime: performance.now(),
      duration: 500, // 500ms 동안 전환
      fromEmotion: this.state.currentEmotion,
      toEmotion: emotion,
      progress: 0
    };
    
    this.state.currentEmotion = emotion;
  }
  
  /**
   * 감정 트랜지션 업데이트
   */
  updateEmotionTransition() {
    const transition = this.state.emotionTransition;
    const elapsed = performance.now() - transition.startTime;
    
    if (elapsed < transition.duration) {
      // 트랜지션 진행 중
      transition.progress = this.easeInOutCubic(Math.min(elapsed / transition.duration, 1));
    } else {
      // 트랜지션 완료
      transition.progress = 1;
    }
  }
  
  /**
   * 현재 감정 변형 값 계산
   */
  getCurrentTransform() {
    const transition = this.state.emotionTransition;
    
    if (transition.progress >= 1) {
      // 트랜지션 완료된 경우
      return this.emotionTransforms[this.state.currentEmotion];
    }
    
    // 트랜지션 중인 경우 - 두 감정 사이를 보간
    const fromTransform = this.emotionTransforms[transition.fromEmotion];
    const toTransform = this.emotionTransforms[transition.toEmotion];
    const progress = transition.progress;
    
    // 선형 보간
    const result = {};
    for (const key in fromTransform) {
      result[key] = fromTransform[key] + (toTransform[key] - fromTransform[key]) * progress;
    }
    
    return result;
  }
  
  /**
   * 말하기 시작
   */
  startSpeaking(text) {
    this.state.isSpeaking = true;
    this.state.speakingText = text;
    this.state.speakingStartTime = performance.now();
    
    // 텍스트 길이에 비례하여 말하는 시간 설정
    const duration = Math.max(2000, text.length * 100);
    
    // 말하기 종료 타이머 설정
    if (this.speakingTimer) clearTimeout(this.speakingTimer);
    this.speakingTimer = setTimeout(() => {
      this.stopSpeaking();
    }, duration);
    
    return duration;
  }
  
  /**
   * 말하기 중단
   */
  stopSpeaking() {
    this.state.isSpeaking = false;
    if (this.speakingTimer) {
      clearTimeout(this.speakingTimer);
      this.speakingTimer = null;
    }
  }
  
  /**
   * 현재 프레임 렌더링
   */
  renderFrame() {
    if (!this.state.baseImage || !this.state.featurePoints) return;
    
    const ctx = this.ctx;
    const canvas = this.options.canvasElement;
    const transform = this.getCurrentTransform();
    
    // 캔버스 지우기
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // 기본 이미지 그리기 전 상태 저장
    ctx.save();
    
    // 호흡 효과 및 머리 기울기
    const breathOffset = Math.sin(this.state.breathCycle) * 2;
    const headTilt = transform.headTilt;
    
    // 이미지 중심점을 기준으로 회전 및 호흡 애니메이션
    ctx.translate(canvas.width / 2, canvas.height / 2);
    ctx.rotate(headTilt);
    ctx.translate(0, breathOffset);
    
    // 말하기 애니메이션 - 미세한 흔들림
    if (this.state.isSpeaking) {
      const speakingTime = performance.now() - this.state.speakingStartTime;
      const speakMovement = Math.sin(speakingTime * this.speakingPatterns.frequency) * 
                           this.speakingPatterns.amplitude +
                           (Math.random() - 0.5) * this.speakingPatterns.randomness;
      
      ctx.translate(speakMovement, speakMovement * 0.5);
    }
    
    // 밝기 및 채도 조절을 위한 필터
    const brightnessPercent = Math.round(transform.brightness * 100);
    const saturationPercent = Math.round(transform.saturation * 100);
    ctx.filter = `brightness(${brightnessPercent}%) saturate(${saturationPercent}%)`;
    
    // 기본 이미지 그리기
    ctx.drawImage(
      this.state.baseImage,
      -this.state.baseImage.width / 2, 
      -this.state.baseImage.height / 2,
      this.state.baseImage.width,
      this.state.baseImage.height
    );
    
    // 상태 복원
    ctx.restore();
    
    // 얼굴 요소 오버레이 그리기 (말하는 입, 눈 애니메이션 등)
    this.renderFacialOverlays(ctx, transform);
  }
  
  /**
   * 얼굴 요소 오버레이 그리기
   */
  renderFacialOverlays(ctx, transform) {
    if (!this.state.featurePoints) return;
    
    // 말하기 애니메이션 & 입 곡선
    this.renderMouth(ctx, transform);
    
    // 블러시 효과 (감정에 따라)
    if (transform.blushOpacity > 0) {
      this.renderBlush(ctx, transform);
    }
    
    // 눈 애니메이션 (깜빡임, 크기 변화)
    this.renderEyes(ctx, transform);
    
    // 눈썹 각도 변화
    this.renderEyebrows(ctx, transform);
  }
  
  /**
   * 입 애니메이션 렌더링
   */
  renderMouth(ctx, transform) {
    const mouth = this.state.featurePoints.mouth;
    const mouthCurve = transform.mouthCurve;
    
    ctx.save();
    
    if (this.state.isSpeaking) {
      // 말하기 애니메이션 - 입 움직임
      const speakingTime = performance.now() - this.state.speakingStartTime;
      const openAmount = Math.abs(Math.sin(speakingTime * 0.01)) * 10 + 2;
      const mouthWidth = mouth.width * (0.8 + Math.sin(speakingTime * 0.008) * 0.2);
      
      ctx.fillStyle = 'rgba(0, 0, 0, 0.2)';
      ctx.beginPath();
      ctx.ellipse(
        mouth.center.x, 
        mouth.center.y, 
        mouthWidth / 2, 
        openAmount, 
        0, 0, Math.PI * 2
      );
      ctx.fill();
    } else {
      // 일반 입 모양 (곡선)
      const curveHeight = mouthCurve * 15;
      
      ctx.strokeStyle = 'rgba(0, 0, 0, 0.5)';
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(mouth.center.x - mouth.width / 2, mouth.center.y);
      ctx.quadraticCurveTo(
        mouth.center.x, 
        mouth.center.y + curveHeight, 
        mouth.center.x + mouth.width / 2, 
        mouth.center.y
      );
      ctx.stroke();
    }
    
    ctx.restore();
  }
  
  /**
   * 볼 붉은 효과 (블러시) 렌더링
   */
  renderBlush(ctx, transform) {
    const cheeks = this.state.featurePoints.cheeks;
    const blushOpacity = transform.blushOpacity;
    
    ctx.save();
    
    // 양쪽 볼에 블러시 효과 적용
    const drawBlush = (x, y, radius) => {
      const gradient = ctx.createRadialGradient(x, y, 0, x, y, radius);
      gradient.addColorStop(0, `rgba(255, 150, 150, ${blushOpacity})`);
      gradient.addColorStop(1, 'rgba(255, 150, 150, 0)');
      
      ctx.fillStyle = gradient;
      ctx.beginPath();
      ctx.arc(x, y, radius, 0, Math.PI * 2);
      ctx.fill();
    };
    
    drawBlush(cheeks.left.x, cheeks.left.y, 20);
    drawBlush(cheeks.right.x, cheeks.right.y, 20);
    
    ctx.restore();
  }
  
  /**
   * 눈 애니메이션 렌더링
   */
  renderEyes(ctx, transform) {
    const eyes = this.state.featurePoints.eyes;
    const eyeScale = transform.eyeScale;
    
    ctx.save();
    
    // 눈 깜빡임 애니메이션
    const blinkRate = 5000; // 5초에 한번 깜빡임
    const blinkDuration = 200; // 깜빡임 시간 (ms)
    const time = performance.now();
    const blinkPhase = time % blinkRate;
    const isBlinking = blinkPhase < blinkDuration;
    
    if (isBlinking) {
      // 깜빡이는 동안 눈을 감음
      const blinkProgress = blinkPhase / blinkDuration;
      const eyeHeight = Math.cos(blinkProgress * Math.PI) * eyes.left.height * eyeScale;
      
      ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
      
      // 왼쪽 눈
      ctx.beginPath();
      ctx.ellipse(
        eyes.left.x, 
        eyes.left.y, 
        eyes.left.width / 2 * eyeScale, 
        Math.max(1, eyeHeight),
        0, 0, Math.PI * 2
      );
      ctx.fill();
      
      // 오른쪽 눈
      ctx.beginPath();
      ctx.ellipse(
        eyes.right.x, 
        eyes.right.y, 
        eyes.right.width / 2 * eyeScale, 
        Math.max(1, eyeHeight),
        0, 0, Math.PI * 2
      );
      ctx.fill();
    } else {
      // 일반 눈 상태
      ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
      
      // 왼쪽 눈
      ctx.beginPath();
      ctx.ellipse(
        eyes.left.x, 
        eyes.left.y, 
        eyes.left.width / 2 * eyeScale, 
        eyes.left.height / 2 * eyeScale,
        0, 0, Math.PI * 2
      );
      ctx.fill();
      
      // 오른쪽 눈
      ctx.beginPath();
      ctx.ellipse(
        eyes.right.x, 
        eyes.right.y, 
        eyes.right.width / 2 * eyeScale, 
        eyes.right.height / 2 * eyeScale,
        0, 0, Math.PI * 2
      );
      ctx.fill();
      
      // 눈 반짝임 효과 (하이라이트)
      ctx.fillStyle = 'rgba(255, 255, 255, 0.7)';
      const highlightSize = eyes.left.width * 0.15;
      
      ctx.beginPath();
      ctx.arc(
        eyes.left.x + highlightSize, 
        eyes.left.y - highlightSize, 
        highlightSize,
        0, Math.PI * 2
      );
      ctx.fill();
      
      ctx.beginPath();
      ctx.arc(
        eyes.right.x + highlightSize, 
        eyes.right.y - highlightSize, 
        highlightSize,
        0, Math.PI * 2
      );
      ctx.fill();
    }
    
    ctx.restore();
  }
  
  /**
   * 눈썹 렌더링
   */
  renderEyebrows(ctx, transform) {
    const eyebrows = this.state.featurePoints.eyebrows;
    const eyebrowAngle = transform.eyebrowAngle;
    
    ctx.save();
    
    ctx.strokeStyle = 'rgba(0, 0, 0, 0.6)';
    ctx.lineWidth = 2;
    
    // 왼쪽 눈썹
    ctx.beginPath();
    ctx.moveTo(
      eyebrows.left.x - eyebrows.left.width / 2,
      eyebrows.left.y + eyebrowAngle * 10
    );
    ctx.lineTo(
      eyebrows.left.x + eyebrows.left.width / 2,
      eyebrows.left.y - eyebrowAngle * 10
    );
    ctx.stroke();
    
    // 오른쪽 눈썹
    ctx.beginPath();
    ctx.moveTo(
      eyebrows.right.x - eyebrows.right.width / 2,
      eyebrows.right.y - eyebrowAngle * 10
    );
    ctx.lineTo(
      eyebrows.right.x + eyebrows.right.width / 2,
      eyebrows.right.y + eyebrowAngle * 10
    );
    ctx.stroke();
    
    ctx.restore();
  }
  
  /**
   * 이징 함수 - 부드러운 애니메이션
   */
  easeInOutCubic(t) {
    return t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
  }
  
  /**
   * 인스턴스 정리
   */
  dispose() {
    this.stopAnimation();
    this.stopSpeaking();
    this.state.baseImage = null;
    this.ctx = null;
  }
}

// 글로벌 액세스를 위해 window 객체에 추가
window.CanvasAnimationEngine = CanvasAnimationEngine;