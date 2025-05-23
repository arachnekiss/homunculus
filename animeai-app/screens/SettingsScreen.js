import React, { useState, useEffect } from 'react';
import { 
  StyleSheet, 
  View, 
  Text, 
  TouchableOpacity, 
  Switch, 
  ScrollView,
  Alert,
  TextInput,
  SafeAreaView 
} from 'react-native';

export default function SettingsScreen({ navigation }) {
  const [credits, setCredits] = useState(100);
  const [autoPlayAudio, setAutoPlayAudio] = useState(true);
  const [showEmotionLabels, setShowEmotionLabels] = useState(true);
  const [creditsToAdd, setCreditsToAdd] = useState('10');
  
  useEffect(() => {
    // 서버에서 크레딧 정보 불러오기
    fetchCredits();
  }, []);
  
  const fetchCredits = async () => {
    try {
      const response = await fetch(`${global.API_URL}/api/get-credits`);
      const data = await response.json();
      setCredits(data.creditsRemaining);
    } catch (error) {
      console.error('크레딧 정보를 불러오는 중 오류 발생:', error);
    }
  };
  
  const handleAddCredits = async () => {
    const creditsNum = parseInt(creditsToAdd);
    if (isNaN(creditsNum) || creditsNum <= 0) {
      Alert.alert('오류', '유효한 크레딧 수를 입력해주세요.');
      return;
    }
    
    try {
      // 새 크레딧 값 계산
      const newCredits = credits + creditsNum;
      
      // 서버에 크레딧 저장 (실제로는 유저 ID 등 필요)
      const response = await fetch(`${global.API_URL}/api/save-credits`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: 'user1',
          credits: newCredits
        }),
      });
      
      const data = await response.json();
      
      if (data.success) {
        setCredits(newCredits);
        setCreditsToAdd('10');
        Alert.alert('성공', `${creditsNum} 크레딧이 추가되었습니다.`);
      } else {
        throw new Error('크레딧 추가에 실패했습니다.');
      }
    } catch (error) {
      console.error('크레딧 추가 중 오류 발생:', error);
      Alert.alert('오류', '크레딧 추가 중 오류가 발생했습니다.');
    }
  };
  
  const handleReset = () => {
    Alert.alert(
      '설정 초기화',
      '모든 설정을 기본값으로 되돌리시겠습니까?',
      [
        { text: '취소', style: 'cancel' },
        { 
          text: '확인', 
          onPress: () => {
            setAutoPlayAudio(true);
            setShowEmotionLabels(true);
            Alert.alert('성공', '모든 설정이 초기화되었습니다.');
          } 
        }
      ]
    );
  };
  
  const handleAbout = () => {
    Alert.alert(
      'AnimeAI 소개',
      'AnimeAI는 AI 기반 애니메이션 캐릭터와 상호작용할 수 있는 앱입니다. OpenAI의 최신 기술을 활용하여 다양한 캐릭터와 대화하고, 감정 표현을 경험해보세요.\n\n버전: 1.0.0\n\n© 2024 AnimeAI',
      [{ text: '확인', style: 'default' }]
    );
  };
  
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>크레딧</Text>
          <View style={styles.creditInfo}>
            <Text style={styles.creditText}>현재 크레딧: {credits}</Text>
            
            <View style={styles.addCreditContainer}>
              <TextInput
                style={styles.creditInput}
                value={creditsToAdd}
                onChangeText={setCreditsToAdd}
                keyboardType="number-pad"
                placeholder="충전할 크레딧"
                placeholderTextColor="#999"
              />
              
              <TouchableOpacity 
                style={styles.addCreditButton}
                onPress={handleAddCredits}
              >
                <Text style={styles.addCreditButtonText}>충전</Text>
              </TouchableOpacity>
            </View>
            
            <Text style={styles.creditDescription}>
              크레딧은 캐릭터와의 상호작용에 사용됩니다. 각 대화마다 1 크레딧이 소모됩니다.
            </Text>
          </View>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>앱 설정</Text>
          
          <View style={styles.settingItem}>
            <Text style={styles.settingLabel}>자동 음성 재생</Text>
            <Switch
              trackColor={{ false: "#ccc", true: "#a87cff" }}
              thumbColor={autoPlayAudio ? "#6A11CB" : "#f4f3f4"}
              onValueChange={setAutoPlayAudio}
              value={autoPlayAudio}
            />
          </View>
          
          <View style={styles.settingItem}>
            <Text style={styles.settingLabel}>감정 라벨 표시</Text>
            <Switch
              trackColor={{ false: "#ccc", true: "#a87cff" }}
              thumbColor={showEmotionLabels ? "#6A11CB" : "#f4f3f4"}
              onValueChange={setShowEmotionLabels}
              value={showEmotionLabels}
            />
          </View>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>기타</Text>
          
          <TouchableOpacity 
            style={styles.button}
            onPress={handleAbout}
          >
            <Text style={styles.buttonText}>앱 정보</Text>
          </TouchableOpacity>
          
          <TouchableOpacity 
            style={[styles.button, styles.resetButton]}
            onPress={handleReset}
          >
            <Text style={styles.buttonText}>설정 초기화</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
      
      <TouchableOpacity 
        style={styles.backButton}
        onPress={() => navigation.goBack()}
      >
        <Text style={styles.backButtonText}>돌아가기</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f8f8',
  },
  scrollView: {
    flex: 1,
    padding: 16,
  },
  section: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 16,
    color: '#6A11CB',
  },
  creditInfo: {
    alignItems: 'center',
  },
  creditText: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 16,
  },
  addCreditContainer: {
    flexDirection: 'row',
    marginBottom: 16,
    width: '100%',
    justifyContent: 'center',
  },
  creditInput: {
    backgroundColor: '#f0f0f0',
    borderRadius: 10,
    padding: 10,
    width: '50%',
    marginRight: 10,
    fontSize: 16,
  },
  addCreditButton: {
    backgroundColor: '#6A11CB',
    borderRadius: 10,
    padding: 10,
    justifyContent: 'center',
  },
  addCreditButtonText: {
    color: 'white',
    fontWeight: 'bold',
    textAlign: 'center',
  },
  creditDescription: {
    textAlign: 'center',
    color: '#666',
    marginTop: 8,
  },
  settingItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  settingLabel: {
    fontSize: 16,
    color: '#333',
  },
  button: {
    backgroundColor: '#6A11CB',
    borderRadius: 10,
    padding: 14,
    alignItems: 'center',
    marginBottom: 12,
  },
  resetButton: {
    backgroundColor: '#FF5252',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },
  backButton: {
    backgroundColor: '#6A11CB',
    padding: 16,
    alignItems: 'center',
    margin: 16,
    borderRadius: 10,
  },
  backButtonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },
});