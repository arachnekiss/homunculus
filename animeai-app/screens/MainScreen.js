import React, { useState, useEffect } from 'react';
import { 
  StyleSheet, 
  View, 
  Text, 
  TouchableOpacity, 
  ScrollView,
  SafeAreaView,
  Image 
} from 'react-native';

// 오브젝트 스토리지의 버킷 ID
const BUCKET_ID = 'replit-objstore-82708a44-60fd-4562-b6e0-ae6312a9c2d6';
// 오브젝트 스토리지 URL 기본 경로
const STORAGE_BASE_URL = `https://${BUCKET_ID}.replit.dev/app_storage`;
// 캐릭터 이미지 URL
const CHARACTER_IMAGES = {
  "미카": "https://i.imgur.com/JqYxQHO.png", // 첨부된 이미지를 외부 이미지 호스팅으로 업로드
  "유키": "https://i.imgur.com/7FEpnLV.jpg",
  "타로": "https://i.imgur.com/QnxwWHc.png"
};

export default function MainScreen() {
  const [characters, setCharacters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // 서버로부터 캐릭터 데이터 가져오기
    fetchCharacters();
  }, []);

  const fetchCharacters = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${global.API_URL}/api/characters`);
      const data = await response.json();
      
      if (data.success) {
        setCharacters(data.characters || []);
      } else {
        setError("캐릭터 데이터를 불러오는데 실패했습니다.");
      }
    } catch (err) {
      console.error("캐릭터 데이터 로딩 오류:", err);
      setError("서버 연결 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const getImageUrl = (path, characterName) => {
    // 이미지 경로가 http로 시작하면 그대로 사용
    if (path && (path.startsWith('http://') || path.startsWith('https://'))) {
      return path;
    }
    
    // 캐릭터 이름 기반으로 임시 이미지 제공
    if (characterName && CHARACTER_IMAGES[characterName]) {
      return CHARACTER_IMAGES[characterName];
    }
    
    // 오브젝트 스토리지 경로로 변환 (기존 경로가 있는 경우)
    if (path && path.startsWith('/assets/')) {
      const imageName = path.split('/').pop();
      return `${STORAGE_BASE_URL}/${imageName}`;
    }
    
    // 기본 이미지
    return 'https://i.pravatar.cc/300?img=' + Math.floor(Math.random() * 70);
  };

  if (loading) {
    return (
      <View style={styles.centerContainer}>
        <Text style={styles.loadingText}>캐릭터 데이터 로딩 중...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.centerContainer}>
        <Text style={styles.errorText}>{error}</Text>
        <TouchableOpacity style={styles.retryButton} onPress={fetchCharacters}>
          <Text style={styles.retryButtonText}>다시 시도</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>애니메이션 AI 캐릭터</Text>
      </View>
      
      <ScrollView style={styles.scrollView}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>내 캐릭터</Text>
          
          {characters.length === 0 ? (
            <View style={styles.emptyContainer}>
              <Text style={styles.emptyText}>등록된 캐릭터가 없습니다.</Text>
            </View>
          ) : (
            <View style={styles.charactersGrid}>
              {characters.map(character => (
                <View key={character.id} style={styles.characterCard}>
                  <Image 
                    source={{ uri: getImageUrl(character.image_url, character.name) }} 
                    style={styles.characterImage} 
                    resizeMode="cover"
                  />
                  <View style={styles.characterInfo}>
                    <Text style={styles.characterName}>{character.name}</Text>
                    <Text style={styles.characterType}>{character.personality?.type || '일반'}</Text>
                    <Text numberOfLines={2} style={styles.characterDesc}>{character.description}</Text>
                  </View>
                  <TouchableOpacity style={styles.talkButton}>
                    <Text style={styles.talkButtonText}>대화하기</Text>
                  </TouchableOpacity>
                </View>
              ))}
            </View>
          )}
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>오브젝트 스토리지 정보</Text>
          <Text style={styles.storageInfo}>버킷 ID: {BUCKET_ID}</Text>
          <Text style={styles.storageInfo}>베이스 URL: {STORAGE_BASE_URL}</Text>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>앱 정보</Text>
          <Text style={styles.appInfo}>React Native와 Flask를 활용한 애니메이션 AI 캐릭터 앱입니다.</Text>
          <Text style={styles.appInfo}>이 앱은 OpenAI API를 활용하여 캐릭터와 대화하고, 감정 표현을 생성합니다.</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  header: {
    backgroundColor: '#6A11CB',
    padding: 16,
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: 'white',
  },
  scrollView: {
    flex: 1,
    padding: 16,
  },
  section: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 16,
    color: '#6A11CB',
  },
  emptyContainer: {
    padding: 30,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyText: {
    color: '#666',
    fontSize: 16,
  },
  charactersGrid: {
    flexDirection: 'column',
    gap: 16,
  },
  characterCard: {
    backgroundColor: '#f8f8f8',
    borderRadius: 12,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: '#e0e0e0',
  },
  characterImage: {
    width: '100%',
    height: 180,
    backgroundColor: '#e0e0e0',
  },
  characterInfo: {
    padding: 12,
  },
  characterName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
  },
  characterType: {
    fontSize: 14,
    color: '#6A11CB',
    marginBottom: 8,
  },
  characterDesc: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
  },
  talkButton: {
    backgroundColor: '#6A11CB',
    padding: 12,
    alignItems: 'center',
  },
  talkButtonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },
  loadingText: {
    fontSize: 18,
    color: '#666',
  },
  errorText: {
    fontSize: 18,
    color: '#FF5252',
    marginBottom: 20,
    textAlign: 'center',
  },
  retryButton: {
    backgroundColor: '#6A11CB',
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 8,
  },
  retryButtonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },
  storageInfo: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  appInfo: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
    lineHeight: 20,
  },
});