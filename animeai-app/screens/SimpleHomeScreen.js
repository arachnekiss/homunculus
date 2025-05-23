import React, { useState, useEffect } from 'react';
import { 
  StyleSheet, 
  View, 
  Text, 
  TouchableOpacity, 
  ScrollView,
  SafeAreaView 
} from 'react-native';

const CHARACTERS = [
  {
    id: '1',
    name: '미카',
    description: '활발하고 친절한 성격의 10대 소녀. 긍정적인 에너지를 가지고 있으며, 친구들을 격려하는 것을 좋아합니다.',
    personality: {
      type: 'Friendly',
      traits: ['positive', 'enthusiastic', 'caring'],
      background: '고등학교 학생회장. 예술과 음악을 좋아합니다.',
      speechStyle: '친근하고 활기차게 말합니다. 종종 "-다요!" 라는 말투를 사용해요.',
    }
  },
  {
    id: '2',
    name: '유키',
    description: '조용하고 사려 깊은 대학생. 책을 읽는 것을 좋아하며, 깊은 철학적 대화를 나누는 것을 즐깁니다.',
    personality: {
      type: 'Thoughtful',
      traits: ['calm', 'philosophical', 'intelligent'],
      background: '문학을 전공하는 대학생. 카페에서 일하며 소설을 쓰고 있습니다.',
      speechStyle: '조용하고 신중하게 말합니다. 때때로 책에서 인용구를 사용합니다.',
    }
  },
  {
    id: '3',
    name: '타로',
    description: '열정적이고 에너지가 넘치는 10대 소년. 스포츠와 모험을 좋아합니다.',
    personality: {
      type: 'Energetic',
      traits: ['passionate', 'brave', 'competitive'],
      background: '고등학교 농구부 주장. 프로 선수가 되는 것이 꿈입니다.',
      speechStyle: '자신감 있고 열정적으로 말합니다. 종종 "-다고!" 라는 말투를 사용해요.',
    }
  },
];

export default function SimpleHomeScreen({ navigation }) {
  const [credits, setCredits] = useState(100);
  const [apiStatus, setApiStatus] = useState('확인 중...');

  useEffect(() => {
    // API 서버 상태 확인
    checkApiStatus();
  }, []);

  const checkApiStatus = async () => {
    try {
      const response = await fetch(`${global.API_URL}/api/health`);
      const data = await response.json();
      if (data.status === 'OK') {
        setApiStatus('연결됨 ✅');
      } else {
        setApiStatus('연결 실패 ❌');
      }
    } catch (error) {
      console.error('API 서버 연결 오류:', error);
      setApiStatus('연결 오류 ❌');
    }
  };

  const handleCharacterSelect = (character) => {
    // 캐릭터 선택 시 간단한 알림
    alert(`${character.name} 캐릭터를 선택했습니다!\n\n아직 이 기능은 구현 중입니다.`);
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>애니메이션 AI 캐릭터</Text>
        <View style={styles.statusContainer}>
          <Text style={styles.statusText}>API 서버: {apiStatus}</Text>
          <Text style={styles.creditsText}>크레딧: {credits}</Text>
        </View>
      </View>
      
      <ScrollView style={styles.scrollView}>
        <Text style={styles.sectionTitle}>캐릭터 선택</Text>
        <View style={styles.charactersContainer}>
          {CHARACTERS.map((character) => (
            <TouchableOpacity
              key={character.id}
              style={styles.characterCard}
              onPress={() => handleCharacterSelect(character)}
            >
              <View style={styles.characterInfo}>
                <Text style={styles.characterName}>{character.name}</Text>
                <Text style={styles.characterDescription} numberOfLines={2}>
                  {character.description}
                </Text>
                <Text style={styles.personalityText}>
                  성격: {character.personality.type}
                </Text>
              </View>
            </TouchableOpacity>
          ))}
        </View>
        
        <View style={styles.infoSection}>
          <Text style={styles.sectionTitle}>애니메이션 AI 앱 정보</Text>
          <Text style={styles.infoText}>
            이 앱은 AI 기술을 활용하여 애니메이션 캐릭터와 실시간으로 상호작용할 수 있는 환경을 제공합니다.
          </Text>
          <Text style={styles.infoText}>
            주요 기능:
          </Text>
          <Text style={styles.bulletText}>• 개성 있는 AI 캐릭터와의 대화</Text>
          <Text style={styles.bulletText}>• 감정 표현과 음성 합성</Text>
          <Text style={styles.bulletText}>• 카메라를 통한 사용자 감정 인식</Text>
          <Text style={styles.bulletText}>• 다양한 캐릭터 선택 및 커스터마이징</Text>
          
          <Text style={styles.versionText}>버전: 1.0.0</Text>
        </View>
      </ScrollView>
      
      <View style={styles.footer}>
        <TouchableOpacity 
          style={styles.refreshButton}
          onPress={checkApiStatus}
        >
          <Text style={styles.buttonText}>API 연결 확인</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f8f8',
  },
  header: {
    padding: 16,
    backgroundColor: '#6A11CB',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 8,
  },
  statusContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  statusText: {
    color: 'white',
  },
  creditsText: {
    color: 'white',
    fontWeight: 'bold',
  },
  scrollView: {
    flex: 1,
    padding: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
    color: '#6A11CB',
  },
  charactersContainer: {
    marginBottom: 24,
  },
  characterCard: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderLeftWidth: 4,
    borderLeftColor: '#6A11CB',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  characterInfo: {
    flex: 1,
  },
  characterName: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 4,
    color: '#6A11CB',
  },
  characterDescription: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  personalityText: {
    fontSize: 12,
    color: '#888',
    fontStyle: 'italic',
  },
  infoSection: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
  },
  infoText: {
    fontSize: 14,
    color: '#444',
    marginBottom: 16,
    lineHeight: 20,
  },
  bulletText: {
    fontSize: 14,
    color: '#444',
    marginBottom: 8,
    marginLeft: 8,
  },
  versionText: {
    fontSize: 12,
    color: '#888',
    marginTop: 16,
    textAlign: 'center',
  },
  footer: {
    padding: 16,
    alignItems: 'center',
  },
  refreshButton: {
    backgroundColor: '#6A11CB',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 24,
    width: '80%',
    alignItems: 'center',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },
});