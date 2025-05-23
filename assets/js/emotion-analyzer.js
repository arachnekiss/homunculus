/**
 * AnimeAI 멀티모달 감정 분석 시스템
 * 텍스트, 이미지, 음성에서 감정을 분석합니다.
 */

class EmotionAnalyzer {
  constructor(options = {}) {
    this.options = {
      apiEndpoint: '/api/emotion-analyze',
      enableTextAnalysis: true,
      enableImageAnalysis: true,
      enableAudioAnalysis: false,
      useCaching: true,
      ...options
    };
    
    this.cache = new Map();
    this.supportedEmotions = [
      'neutral', 'happy', 'sad', 'angry', 
      'surprised', 'excited', 'embarrassed', 
      'thoughtful', 'nervous'
    ];
  }
  
  /**
   * 텍스트 기반 감정 분석
   * @param {string} text - 분석할 텍스트
   * @returns {Promise<Object>} 감정 분석 결과
   */
  async analyzeText(text) {
    if (!text || text.trim().length === 0) {
      return this.getDefaultEmotionResult();
    }
    
    // 캐시 확인
    if (this.options.useCaching) {
      const cacheKey = `text:${text}`;
      if (this.cache.has(cacheKey)) {
        return this.cache.get(cacheKey);
      }
    }
    
    try {
      // 서버에 감정 분석 요청
      const response = await fetch(this.options.apiEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          type: 'text',
          data: text
        })
      });
      
      if (!response.ok) {
        throw new Error(`분석 요청 실패: ${response.status}`);
      }
      
      const result = await response.json();
      
      // 캐시에 저장
      if (this.options.useCaching) {
        const cacheKey = `text:${text}`;
        this.cache.set(cacheKey, result);
      }
      
      return result;
    } catch (error) {
      console.error('텍스트 감정 분석 오류:', error);
      
      // 오류 시 규칙 기반 간단 감정 분석 (폴백)
      return this.analyzeTextWithRules(text);
    }
  }
  
  /**
   * 규칙 기반 간단 텍스트 감정 분석 (서버 연결 실패 시)
   * @param {string} text - 분석할 텍스트
   * @returns {Object} 감정 분석 결과
   */
  analyzeTextWithRules(text) {
    const lowerText = text.toLowerCase();
    
    // 간단한 키워드 매핑 (실제로는 더 복잡한 NLP가 필요)
    const emotionKeywords = {
      happy: ['행복', '기쁨', '좋아', '웃', '즐거', '신나', '좋은', '기뻐', '행운', '웃긴', 'ㅋㅋ', 'ㅎㅎ'],
      sad: ['슬픔', '슬퍼', '우울', '눈물', '아쉽', '안타깝', '슬프', '속상', '안타까', '눈물', '미안'],
      angry: ['화남', '짜증', '화가', '열받', '짜증', '화나', '분노', '화를', '싫어', '못마땅'],
      surprised: ['놀람', '깜짝', '헉', '놀랐', '놀라', '믿기지', '뜻밖', '예상', '상상', '와우'],
      excited: ['설렘', '두근', '신난', '와아', '최고', '멋진', '완벽', '대박', '환상적', '굉장'],
      embarrassed: ['부끄', '민망', '창피', '쑥스', '당황', '쫄', '에구', '앗', '쪽팔'],
      thoughtful: ['생각', '고민', '음..', '글쎄', '이해', '그렇군', '흠', '사실', '아마도', '결국'],
      nervous: ['긴장', '불안', '걱정', '두려', '조마조마', '떨리', '겁나', '조심', '위험']
    };
    
    // 감정별 점수 계산
    const scores = {};
    let maxScore = 0;
    let primaryEmotion = 'neutral';
    
    for (const [emotion, keywords] of Object.entries(emotionKeywords)) {
      scores[emotion] = 0;
      
      keywords.forEach(keyword => {
        // 키워드가 포함된 횟수만큼 점수 추가
        const regex = new RegExp(keyword, 'g');
        const matches = lowerText.match(regex);
        
        if (matches) {
          scores[emotion] += matches.length;
        }
      });
      
      // 가장 높은 점수의 감정 추적
      if (scores[emotion] > maxScore) {
        maxScore = scores[emotion];
        primaryEmotion = emotion;
      }
    }
    
    // 최소 점수 임계값 (이 값보다 낮으면 neutral)
    const scoreThreshold = 1;
    
    if (maxScore < scoreThreshold) {
      primaryEmotion = 'neutral';
    }
    
    // 보조 감정 결정 (가장 높은 점수의 70% 이상인 감정들)
    const secondaryEmotions = [];
    const secondaryThreshold = maxScore * 0.7;
    
    for (const [emotion, score] of Object.entries(scores)) {
      if (emotion !== primaryEmotion && score >= secondaryThreshold) {
        secondaryEmotions.push(emotion);
      }
    }
    
    // 신뢰도는 키워드 일치 수 기반
    const confidence = Math.min(0.5 + (maxScore * 0.1), 0.9);
    
    return {
      primary: primaryEmotion,
      confidence: confidence,
      intensity: confidence * 0.8,
      secondary_emotions: secondaryEmotions
    };
  }
  
  /**
   * 이미지 기반 감정 분석
   * @param {HTMLImageElement|HTMLCanvasElement|string} image - 이미지 요소 또는 데이터 URL
   * @returns {Promise<Object>} 감정 분석 결과
   */
  async analyzeImage(image) {
    if (!this.options.enableImageAnalysis) {
      return this.getDefaultEmotionResult();
    }
    
    // 이미지를 데이터 URL로 변환
    let imageData;
    
    if (typeof image === 'string' && image.startsWith('data:')) {
      imageData = image;
    } else if (image instanceof HTMLImageElement || image instanceof HTMLCanvasElement) {
      // 캔버스에 이미지 그리고 데이터 URL 추출
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      
      canvas.width = image.width || 300;
      canvas.height = image.height || 300;
      
      ctx.drawImage(image, 0, 0, canvas.width, canvas.height);
      imageData = canvas.toDataURL('image/jpeg', 0.8);
    } else {
      return this.getDefaultEmotionResult();
    }
    
    // 캐시 확인
    if (this.options.useCaching) {
      const cacheKey = `image:${imageData.slice(0, 100)}`;
      if (this.cache.has(cacheKey)) {
        return this.cache.get(cacheKey);
      }
    }
    
    try {
      // 서버에 이미지 감정 분석 요청
      const response = await fetch(this.options.apiEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          type: 'image',
          data: imageData
        })
      });
      
      if (!response.ok) {
        throw new Error(`이미지 분석 요청 실패: ${response.status}`);
      }
      
      const result = await response.json();
      
      // 캐시에 저장
      if (this.options.useCaching) {
        const cacheKey = `image:${imageData.slice(0, 100)}`;
        this.cache.set(cacheKey, result);
      }
      
      return result;
    } catch (error) {
      console.error('이미지 감정 분석 오류:', error);
      return this.getDefaultEmotionResult();
    }
  }
  
  /**
   * 오디오 기반 감정 분석
   * @param {Blob|AudioBuffer|string} audio - 오디오 데이터
   * @returns {Promise<Object>} 감정 분석 결과
   */
  async analyzeAudio(audio) {
    if (!this.options.enableAudioAnalysis) {
      return this.getDefaultEmotionResult();
    }
    
    // 오디오 데이터 변환 (실제 구현에서 필요)
    let audioData;
    
    try {
      // 서버에 오디오 감정 분석 요청
      const response = await fetch(this.options.apiEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          type: 'audio',
          data: audioData
        })
      });
      
      if (!response.ok) {
        throw new Error(`오디오 분석 요청 실패: ${response.status}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('오디오 감정 분석 오류:', error);
      return this.getDefaultEmotionResult();
    }
  }
  
  /**
   * 멀티모달 감정 분석 (여러 입력 통합)
   * @param {Object} data - 분석할 데이터 객체
   * @param {string} [data.text] - 텍스트 데이터
   * @param {HTMLImageElement|string} [data.image] - 이미지 데이터
   * @param {Blob|AudioBuffer|string} [data.audio] - 오디오 데이터
   * @returns {Promise<Object>} 통합된 감정 분석 결과
   */
  async analyzeMultimodal(data) {
    const results = [];
    
    // 텍스트 분석
    if (data.text && this.options.enableTextAnalysis) {
      const textResult = await this.analyzeText(data.text);
      results.push({
        type: 'text',
        weight: 0.6,
        result: textResult
      });
    }
    
    // 이미지 분석
    if (data.image && this.options.enableImageAnalysis) {
      const imageResult = await this.analyzeImage(data.image);
      results.push({
        type: 'image',
        weight: 0.7,
        result: imageResult
      });
    }
    
    // 오디오 분석
    if (data.audio && this.options.enableAudioAnalysis) {
      const audioResult = await this.analyzeAudio(data.audio);
      results.push({
        type: 'audio',
        weight: 0.5,
        result: audioResult
      });
    }
    
    // 결과가 없으면 기본값 반환
    if (results.length === 0) {
      return this.getDefaultEmotionResult();
    }
    
    // 단일 결과면 바로 반환
    if (results.length === 1) {
      return results[0].result;
    }
    
    // 여러 결과 통합
    return this.combineResults(results);
  }
  
  /**
   * 여러 분석 결과 통합
   * @param {Array<Object>} results - 분석 결과 배열
   * @returns {Object} 통합된 감정 분석 결과
   */
  combineResults(results) {
    // 감정별 가중 점수 계산
    const emotionScores = {};
    this.supportedEmotions.forEach(emotion => {
      emotionScores[emotion] = 0;
    });
    
    let totalWeight = 0;
    
    // 각 결과의 가중치를 적용하여 점수 합산
    results.forEach(result => {
      const primaryEmotion = result.result.primary;
      const confidence = result.result.confidence || 0.5;
      const weight = result.weight || 1.0;
      
      emotionScores[primaryEmotion] += confidence * weight;
      
      // 보조 감정들도 낮은 가중치로 추가
      if (result.result.secondary_emotions) {
        result.result.secondary_emotions.forEach(secondaryEmotion => {
          emotionScores[secondaryEmotion] += (confidence * 0.5) * weight;
        });
      }
      
      totalWeight += weight;
    });
    
    // 최고 점수 감정 찾기
    let maxScore = 0;
    let primaryEmotion = 'neutral';
    
    for (const [emotion, score] of Object.entries(emotionScores)) {
      if (score > maxScore) {
        maxScore = score;
        primaryEmotion = emotion;
      }
    }
    
    // 보조 감정 결정 (최고 점수의 70% 이상)
    const secondaryEmotions = [];
    const threshold = maxScore * 0.7;
    
    for (const [emotion, score] of Object.entries(emotionScores)) {
      if (emotion !== primaryEmotion && score >= threshold) {
        secondaryEmotions.push(emotion);
      }
    }
    
    // 전체 가중치로 정규화된 신뢰도 계산
    const normalizedScore = totalWeight > 0 ? maxScore / totalWeight : 0.5;
    
    // 최종 결과 구성
    return {
      primary: primaryEmotion,
      confidence: Math.min(normalizedScore, 0.95),
      intensity: Math.min(normalizedScore * 1.1, 1.0),
      secondary_emotions: secondaryEmotions.slice(0, 2)
    };
  }
  
  /**
   * 기본 감정 분석 결과 반환
   * @returns {Object} 기본 감정 분석 결과
   */
  getDefaultEmotionResult() {
    return {
      primary: 'neutral',
      confidence: 0.5,
      intensity: 0.5,
      secondary_emotions: []
    };
  }
  
  /**
   * 캐시 지우기
   */
  clearCache() {
    this.cache.clear();
  }
}

// 글로벌 액세스를 위해 window 객체에 추가
window.EmotionAnalyzer = EmotionAnalyzer;