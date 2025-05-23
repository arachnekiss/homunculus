import React, { useState, useEffect, useRef } from 'react';
import { 
  StyleSheet, 
  View, 
  Text, 
  TouchableOpacity, 
  ActivityIndicator,
  Alert 
} from 'react-native';
import { Camera } from 'expo-camera';
import * as FaceDetector from 'expo-face-detector';

export default function CameraScreen({ route, navigation }) {
  const [hasPermission, setHasPermission] = useState(null);
  const [type, setType] = useState(Camera.Constants.Type.front);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [detectedEmotion, setDetectedEmotion] = useState(null);
  
  const cameraRef = useRef(null);
  const { onReturn } = route.params || {};
  
  // 카메라 권한 요청
  useEffect(() => {
    (async () => {
      const { status } = await Camera.requestCameraPermissionsAsync();
      setHasPermission(status === 'granted');
    })();
  }, []);
  
  const takePicture = async () => {
    if (cameraRef.current) {
      setIsAnalyzing(true);
      
      try {
        // 사진 촬영
        const photo = await cameraRef.current.takePictureAsync({
          quality: 0.7,
          base64: true,
          skipProcessing: true
        });
        
        // 서버로 표정 분석 요청
        const response = await fetch(`${global.API_URL}/api/analyze-expression`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            image: `data:image/jpeg;base64,${photo.base64}`
          }),
        });
        
        const data = await response.json();
        
        if (data.error) {
          throw new Error(data.error);
        }
        
        // 감지된 감정 표시
        setDetectedEmotion(data.emotion);
        
        // 3초 후 결과와 함께 이전 화면으로 이동
        setTimeout(() => {
          if (onReturn && typeof onReturn === 'function') {
            onReturn(data.emotion);
          }
          navigation.goBack();
        }, 3000);
        
      } catch (error) {
        console.error('표정 분석 중 오류 발생:', error);
        Alert.alert('오류', '표정 분석 중 오류가 발생했습니다.');
        setIsAnalyzing(false);
      }
    }
  };
  
  // 권한이 아직 확인되지 않은 경우
  if (hasPermission === null) {
    return (
      <View style={styles.container}>
        <ActivityIndicator size="large" color="#6A11CB" />
        <Text style={styles.statusText}>카메라 권한을 확인하는 중...</Text>
      </View>
    );
  }
  
  // 권한이 거부된 경우
  if (hasPermission === false) {
    return (
      <View style={styles.container}>
        <Text style={styles.statusText}>카메라 접근 권한이 없습니다.</Text>
        <TouchableOpacity 
          style={styles.button}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.buttonText}>돌아가기</Text>
        </TouchableOpacity>
      </View>
    );
  }
  
  return (
    <View style={styles.container}>
      <Camera 
        style={styles.camera} 
        type={type}
        ref={cameraRef}
        faceDetectorSettings={{
          mode: FaceDetector.FaceDetectorMode.fast,
          detectLandmarks: FaceDetector.FaceDetectorLandmarks.none,
          runClassifications: FaceDetector.FaceDetectorClassifications.all,
          minDetectionInterval: 100,
          tracking: true,
        }}
      >
        <View style={styles.cameraContent}>
          {detectedEmotion ? (
            <View style={styles.resultContainer}>
              <Text style={styles.resultText}>
                감지된 감정: {detectedEmotion}
              </Text>
              <Text style={styles.redirectText}>
                잠시 후 캐릭터 화면으로 돌아갑니다...
              </Text>
            </View>
          ) : (
            <>
              <View style={styles.instructionContainer}>
                <Text style={styles.instructionText}>
                  감정을 표현하고 촬영 버튼을 눌러주세요
                </Text>
              </View>
              
              <View style={styles.controlsContainer}>
                <TouchableOpacity
                  style={styles.flipButton}
                  onPress={() => {
                    setType(
                      type === Camera.Constants.Type.back
                        ? Camera.Constants.Type.front
                        : Camera.Constants.Type.back
                    );
                  }}
                >
                  <Text style={styles.buttonText}>전환</Text>
                </TouchableOpacity>
                
                <TouchableOpacity
                  style={[styles.captureButton, isAnalyzing && styles.buttonDisabled]}
                  onPress={takePicture}
                  disabled={isAnalyzing}
                >
                  {isAnalyzing ? (
                    <ActivityIndicator size="small" color="white" />
                  ) : (
                    <View style={styles.captureButtonInner} />
                  )}
                </TouchableOpacity>
                
                <TouchableOpacity
                  style={styles.cancelButton}
                  onPress={() => navigation.goBack()}
                >
                  <Text style={styles.buttonText}>취소</Text>
                </TouchableOpacity>
              </View>
            </>
          )}
        </View>
      </Camera>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'black',
    justifyContent: 'center',
    alignItems: 'center',
  },
  camera: {
    flex: 1,
    width: '100%',
  },
  cameraContent: {
    flex: 1,
    backgroundColor: 'transparent',
    justifyContent: 'space-between',
  },
  instructionContainer: {
    padding: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.6)',
    marginTop: 40,
    alignSelf: 'center',
    borderRadius: 12,
  },
  instructionText: {
    color: 'white',
    fontSize: 16,
    textAlign: 'center',
  },
  controlsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    marginBottom: 40,
    width: '100%',
    paddingHorizontal: 20,
  },
  flipButton: {
    backgroundColor: '#6A11CB',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 30,
  },
  captureButton: {
    width: 70,
    height: 70,
    borderRadius: 35,
    backgroundColor: '#6A11CB',
    justifyContent: 'center',
    alignItems: 'center',
  },
  captureButtonInner: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: 'white',
    borderWidth: 2,
    borderColor: '#6A11CB',
  },
  cancelButton: {
    backgroundColor: '#FF5252',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 30,
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  resultContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
  },
  resultText: {
    color: 'white',
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  redirectText: {
    color: 'white',
    fontSize: 16,
  },
  statusText: {
    color: 'white',
    fontSize: 18,
    marginBottom: 20,
  },
  button: {
    backgroundColor: '#6A11CB',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 10,
  },
});