import React, { useState, useEffect } from 'react';
import { 
  StyleSheet, 
  View, 
  Text, 
  TouchableOpacity, 
  Image, 
  ScrollView,
  Alert,
  SafeAreaView,
  ImageBackground 
} from 'react-native';

const CHARACTERS = [
  {
    id: '1',
    name: '미카',
    description: '활발하고 친절한 성격의 10대 소녀. 긍정적인 에너지를 가지고 있으며, 친구들을 격려하는 것을 좋아합니다.',
    image: require('../assets/icon.png'),
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
    image: require('../assets/icon.png'),
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
    image: require('../assets/icon.png'),
    personality: {
      type: 'Energetic',
      traits: ['passionate', 'brave', 'competitive'],
      background: '고등학교 농구부 주장. 프로 선수가 되는 것이 꿈입니다.',
      speechStyle: '자신감 있고 열정적으로 말합니다. 종종 "-다고!" 라는 말투를 사용해요.',
    }
  },
];

export default function HomeScreen({ navigation }) {
  const [credits, setCredits] = useState(100);

  useEffect(() => {
    // 서버에서 크레딧 정보 불러오기
    fetchCredits();
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

  const handleCharacterSelect = (character) => {
    if (credits <= 0) {
      Alert.alert(
        '크레딧 부족',
        '크레딧이 부족합니다. 설정에서 크레딧을 충전해주세요.',
        [{ text: '확인', style: 'cancel' }]
      );
      return;
    }
    
    navigation.navigate('Character', { character });
  };

  return (
    <ImageBackground 
      source={require('../assets/icon.png')} 
      style={styles.backgroundImage}
      imageStyle={{ opacity: 0.1 }}
    >
      <SafeAreaView style={styles.container}>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>애니메이션 AI 캐릭터</Text>
          <View style={styles.creditsContainer}>
            <Text style={styles.creditsText}>크레딧: {credits}</Text>
          </View>
        </View>
        
        <ScrollView style={styles.scrollView}>
          <View style={styles.charactersContainer}>
            {CHARACTERS.map((character) => (
              <TouchableOpacity
                key={character.id}
                style={styles.characterCard}
                onPress={() => handleCharacterSelect(character)}
              >
                <Image source={character.image} style={styles.characterImage} />
                <View style={styles.characterInfo}>
                  <Text style={styles.characterName}>{character.name}</Text>
                  <Text style={styles.characterDescription} numberOfLines={2}>
                    {character.description}
                  </Text>
                </View>
              </TouchableOpacity>
            ))}
          </View>
        </ScrollView>
        
        <View style={styles.footer}>
          <TouchableOpacity 
            style={styles.settingsButton}
            onPress={() => navigation.navigate('Settings')}
          >
            <Text style={styles.settingsButtonText}>설정</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    </ImageBackground>
  );
}

const styles = StyleSheet.create({
  backgroundImage: {
    flex: 1,
    backgroundColor: '#f8f8f8',
  },
  container: {
    flex: 1,
    padding: 16,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#6A11CB',
  },
  creditsContainer: {
    backgroundColor: '#6A11CB',
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 20,
  },
  creditsText: {
    color: 'white',
    fontWeight: 'bold',
  },
  scrollView: {
    flex: 1,
  },
  charactersContainer: {
    marginBottom: 20,
  },
  characterCard: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    flexDirection: 'row',
    alignItems: 'center',
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  characterImage: {
    width: 80,
    height: 80,
    borderRadius: 40,
    marginRight: 16,
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
  },
  footer: {
    alignItems: 'center',
    marginTop: 10,
  },
  settingsButton: {
    backgroundColor: '#6A11CB',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 24,
    width: '50%',
    alignItems: 'center',
  },
  settingsButtonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },
});