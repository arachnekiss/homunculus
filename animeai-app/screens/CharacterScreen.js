import React, { useState, useEffect, useRef } from 'react';
import { 
  StyleSheet, 
  View, 
  Text, 
  TextInput, 
  TouchableOpacity, 
  Image, 
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
  Alert
} from 'react-native';
import { Audio } from 'expo-av';
import * as FileSystem from 'expo-file-system';

export default function CharacterScreen({ route, navigation }) {
  const { character } = route.params;
  
  const [message, setMessage] = useState('');
  const [chatHistory, setChatHistory] = useState([]);
  const [currentEmotion, setCurrentEmotion] = useState('neutral');
  const [isLoading, setIsLoading] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [characterExpressions, setCharacterExpressions] = useState({});
  const [credits, setCredits] = useState(100);
  
  const soundRef = useRef(null);
  const scrollViewRef = useRef(null);
  
  // 초기화
  useEffect(() => {
    // 크레딧 정보 불러오기
    fetchCredits();
    
    // 기본 환영 메시지 추가
    setChatHistory([
      { 
        id: '0',
        role: 'assistant', 
        content: `안녕하세요! 저는 ${character.name}입니다. 무엇을 도와드릴까요?`,
        emotion: 'happy'
      }
    ]);
    
    // 캐릭터 표정 이미지 가져오기 (실제로는 API 호출해야 함)
    // 현재는 모든 감정에 동일한 이미지 사용
    const emotionImages = {
      happy: character.image,
      sad: character.image,
      angry: character.image,
      surprised: character.image,
      neutral: character.image,
      embarrassed: character.image,
      thoughtful: character.image,
      excited: character.image,
      nervous: character.image
    };
    setCharacterExpressions(emotionImages);
    
    // 오디오 초기화
    return () => {
      if (soundRef.current) {
        soundRef.current.unloadAsync();
      }
    };
  }, []);
  
  const fetchCredits = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/get-credits');
      const data = await response.json();
      setCredits(data.creditsRemaining);
    } catch (error) {
      console.error('크레딧 정보를 불러오는 중 오류 발생:', error);
    }
  };
  
  const handleSendMessage = async () => {
    if (!message.trim()) return;
    
    if (credits <= 0) {
      Alert.alert(
        '크레딧 부족',
        '크레딧이 부족합니다. 설정에서 크레딧을 충전해주세요.',
        [{ text: '확인', style: 'cancel' }]
      );
      return;
    }
    
    // 사용자 메시지 추가
    const userMsg = { id: Date.now().toString(), role: 'user', content: message, emotion: 'neutral' };
    const updatedHistory = [...chatHistory, userMsg];
    setChatHistory(updatedHistory);
    setMessage('');
    
    // 스크롤 맨 아래로 이동
    setTimeout(() => {
      scrollViewRef.current?.scrollToEnd({ animated: true });
    }, 100);
    
    setIsLoading(true);
    
    try {
      // API로 메시지 전송
      const response = await fetch('http://localhost:5000/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: userMsg.content,
          persona: character.personality,
          history: updatedHistory.map(msg => ({ role: msg.role, content: msg.content }))
        }),
      });
      
      const data = await response.json();
      
      if (data.error) {
        throw new Error(data.error);
      }
      
      // 캐릭터 응답 추가
      const newMessage = { 
        id: (Date.now() + 1).toString(), 
        role: 'assistant', 
        content: data.response,
        emotion: data.emotion || 'neutral'
      };
      
      setChatHistory([...updatedHistory, newMessage]);
      setCurrentEmotion(data.emotion || 'neutral');
      
      // 스크롤 맨 아래로 이동
      setTimeout(() => {
        scrollViewRef.current?.scrollToEnd({ animated: true });
      }, 100);
      
      // 크레딧 차감
      const newCredits = credits - 1;
      setCredits(newCredits);
      
      // 서버에 크레딧 저장 (실제로는 유저 ID 등 필요)
      await fetch('http://localhost:5000/api/save-credits', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: 'user1',
          credits: newCredits
        }),
      });
      
      // TTS 음성 재생
      await playCharacterVoice(data.response);
      
    } catch (error) {
      console.error('메시지 전송 중 오류 발생:', error);
      Alert.alert('오류', '메시지 전송 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };
  
  const playCharacterVoice = async (text) => {
    try {
      setIsSpeaking(true);
      
      // API로 TTS 요청
      const response = await fetch('http://localhost:5000/api/text-to-speech', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          text: text,
          voice: 'nova' // 캐릭터에 맞는 음성 선택
        }),
      });
      
      const data = await response.json();
      
      if (data.error) {
        throw new Error(data.error);
      }
      
      // base64 오디오 데이터를 파일로 저장
      const base64Audio = data.audio;
      const audioPath = FileSystem.documentDirectory + 'temp_audio.mp3';
      await FileSystem.writeAsStringAsync(audioPath, base64Audio, { encoding: FileSystem.EncodingType.Base64 });
      
      // 오디오 재생
      if (soundRef.current) {
        await soundRef.current.unloadAsync();
      }
      
      const { sound } = await Audio.Sound.createAsync({ uri: audioPath });
      soundRef.current = sound;
      
      await sound.playAsync();
      
      // 재생이 끝날 때 상태 변경
      sound.setOnPlaybackStatusUpdate((status) => {
        if (status.didJustFinish) {
          setIsSpeaking(false);
        }
      });
      
    } catch (error) {
      console.error('음성 재생 중 오류 발생:', error);
      setIsSpeaking(false);
    }
  };
  
  const handleCameraPress = () => {
    navigation.navigate('Camera', { 
      onReturn: (emotion) => {
        // 카메라에서 감지된 감정을 기반으로 캐릭터 반응 생성
        handleEmotionDetected(emotion);
      }
    });
  };
  
  const handleEmotionDetected = async (userEmotion) => {
    setIsLoading(true);
    
    try {
      // 유저의 감정에 대한 캐릭터 반응 생성
      const emotionPrompt = `사용자가 ${userEmotion} 감정을 보이고 있습니다. 어떻게 반응하시겠어요?`;
      
      const response = await fetch('http://localhost:5000/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: emotionPrompt,
          persona: character.personality,
          history: chatHistory.map(msg => ({ role: msg.role, content: msg.content }))
        }),
      });
      
      const data = await response.json();
      
      if (data.error) {
        throw new Error(data.error);
      }
      
      // 캐릭터 응답 추가
      const newMessage = { 
        id: Date.now().toString(), 
        role: 'assistant', 
        content: data.response,
        emotion: data.emotion || 'neutral'
      };
      
      setChatHistory([...chatHistory, newMessage]);
      setCurrentEmotion(data.emotion || 'neutral');
      
      // 스크롤 맨 아래로 이동
      setTimeout(() => {
        scrollViewRef.current?.scrollToEnd({ animated: true });
      }, 100);
      
      // 크레딧 차감
      const newCredits = credits - 1;
      setCredits(newCredits);
      
      // 서버에 크레딧 저장
      await fetch('http://localhost:5000/api/save-credits', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: 'user1',
          credits: newCredits
        }),
      });
      
      // TTS 음성 재생
      await playCharacterVoice(data.response);
      
    } catch (error) {
      console.error('감정 반응 생성 중 오류 발생:', error);
      Alert.alert('오류', '감정 반응 생성 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };
  
  // 채팅 메시지 렌더링
  const renderChatMessage = (item) => {
    const isUser = item.role === 'user';
    
    return (
      <View 
        key={item.id} 
        style={[
          styles.messageContainer, 
          isUser ? styles.userMessage : styles.assistantMessage
        ]}
      >
        {!isUser && (
          <View style={styles.messageBubbleHeader}>
            <Text style={styles.characterNameInBubble}>{character.name}</Text>
            <Text style={styles.emotionText}>{item.emotion}</Text>
          </View>
        )}
        <Text style={styles.messageText}>{item.content}</Text>
      </View>
    );
  };
  
  return (
    <View style={styles.container}>
      {/* 캐릭터 표시 영역 (화면의 상단 65-70%) */}
      <View style={styles.characterSection}>
        <View style={styles.characterContainer}>
          {characterExpressions[currentEmotion] ? (
            <Image 
              source={characterExpressions[currentEmotion]}
              style={styles.characterImage}
              resizeMode="contain"
            />
          ) : (
            <Image 
              source={character.image}
              style={styles.characterImage}
              resizeMode="contain"
            />
          )}
          
          {isSpeaking && (
            <View style={styles.speakingIndicator}>
              <Text style={styles.speakingText}>말하는 중...</Text>
            </View>
          )}
          
          <View style={styles.emotionBadge}>
            <Text style={styles.emotionBadgeText}>{currentEmotion}</Text>
          </View>
        </View>
      </View>
      
      {/* 채팅 영역 (화면의 하단 30-35%) */}
      <KeyboardAvoidingView 
        behavior={Platform.OS === "ios" ? "padding" : "height"}
        style={styles.chatSection}
        keyboardVerticalOffset={Platform.OS === "ios" ? 100 : 0}
      >
        {/* 채팅 히스토리 */}
        <ScrollView 
          style={styles.chatHistory}
          ref={scrollViewRef}
          contentContainerStyle={styles.chatHistoryContent}
        >
          {chatHistory.map(renderChatMessage)}
          
          {isLoading && (
            <View style={styles.loadingContainer}>
              <ActivityIndicator size="small" color="#6A11CB" />
              <Text style={styles.loadingText}>생각 중...</Text>
            </View>
          )}
        </ScrollView>
        
        {/* 입력 영역 */}
        <View style={styles.inputContainer}>
          <TouchableOpacity 
            style={styles.cameraButton}
            onPress={handleCameraPress}
          >
            <Text style={styles.cameraButtonText}>📷</Text>
          </TouchableOpacity>
          
          <TextInput
            style={styles.input}
            value={message}
            onChangeText={setMessage}
            placeholder="메시지를 입력하세요..."
            placeholderTextColor="#999"
            multiline
          />
          
          <TouchableOpacity 
            style={[
              styles.sendButton,
              (!message.trim() || isLoading) && styles.sendButtonDisabled
            ]}
            onPress={handleSendMessage}
            disabled={!message.trim() || isLoading}
          >
            <Text style={styles.sendButtonText}>전송</Text>
          </TouchableOpacity>
        </View>
        
        {/* 크레딧 표시 */}
        <View style={styles.creditsContainer}>
          <Text style={styles.creditsText}>크레딧: {credits}</Text>
        </View>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f8f8',
  },
  characterSection: {
    flex: 0.65, // 화면의 65%
    backgroundColor: '#f0f0f0',
    borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
    overflow: 'hidden',
  },
  characterContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  characterImage: {
    width: '80%',
    height: '80%',
  },
  emotionBadge: {
    position: 'absolute',
    top: 20,
    right: 20,
    backgroundColor: '#6A11CB',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
  },
  emotionBadgeText: {
    color: 'white',
    fontWeight: 'bold',
  },
  speakingIndicator: {
    position: 'absolute',
    bottom: 20,
    backgroundColor: 'rgba(106, 17, 203, 0.8)',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
  },
  speakingText: {
    color: 'white',
    fontWeight: 'bold',
  },
  chatSection: {
    flex: 0.35, // 화면의 35%
    backgroundColor: 'white',
  },
  chatHistory: {
    flex: 1,
  },
  chatHistoryContent: {
    padding: 16,
  },
  messageContainer: {
    maxWidth: '80%',
    marginBottom: 12,
    padding: 12,
    borderRadius: 16,
  },
  userMessage: {
    alignSelf: 'flex-end',
    backgroundColor: '#6A11CB',
  },
  assistantMessage: {
    alignSelf: 'flex-start',
    backgroundColor: '#f0f0f0',
  },
  messageBubbleHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 4,
  },
  characterNameInBubble: {
    fontWeight: 'bold',
    fontSize: 12,
    color: '#6A11CB',
  },
  emotionText: {
    fontSize: 12,
    fontStyle: 'italic',
    color: '#666',
  },
  messageText: {
    fontSize: 16,
    color: '#333',
  },
  userMessage: {
    alignSelf: 'flex-end',
    backgroundColor: '#6A11CB',
  },
  assistantMessage: {
    alignSelf: 'flex-start',
    backgroundColor: '#f0f0f0',
  },
  messageText: {
    color: props => props.isUser ? 'white' : '#333',
  },
  loadingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'flex-start',
    backgroundColor: '#f0f0f0',
    padding: 12,
    borderRadius: 16,
    marginBottom: 12,
  },
  loadingText: {
    marginLeft: 8,
    color: '#666',
  },
  inputContainer: {
    flexDirection: 'row',
    padding: 12,
    backgroundColor: 'white',
    borderTopWidth: 1,
    borderTopColor: '#eee',
    alignItems: 'center',
  },
  cameraButton: {
    width: 40,
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
    borderRadius: 20,
    marginRight: 8,
  },
  cameraButtonText: {
    fontSize: 20,
  },
  input: {
    flex: 1,
    backgroundColor: '#f0f0f0',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 8,
    maxHeight: 100,
  },
  sendButton: {
    backgroundColor: '#6A11CB',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    marginLeft: 8,
  },
  sendButtonDisabled: {
    backgroundColor: '#ccc',
  },
  sendButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
  creditsContainer: {
    padding: 8,
    alignItems: 'center',
  },
  creditsText: {
    color: '#666',
  },
});