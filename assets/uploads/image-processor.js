const sharp = require('sharp');
const fs = require('fs');
const path = require('path');
const fetch = require('node-fetch');

// 오브젝트 스토리지 정보
const BUCKET_ID = 'replit-objstore-82708a44-60fd-4562-b6e0-ae6312a9c2d6';
const BUCKET_NAME = 'app_storage';
const API_URL = `https://${BUCKET_ID}.replit.dev/${BUCKET_NAME}`;

// 이미지 처리 함수
async function processAndUploadImage(inputPath, outputName) {
  try {
    // 이미지 읽기
    const imageBuffer = fs.readFileSync(inputPath);
    
    // 이미지 처리 (크기 조정 및 최적화)
    const processedImage = await sharp(imageBuffer)
      .resize(400, 400, { fit: 'contain', background: { r: 255, g: 255, b: 255, alpha: 1 } })
      .jpeg({ quality: 90 })
      .toBuffer();
    
    // 처리된 이미지 업로드
    const response = await fetch(`${API_URL}/${outputName}`, {
      method: 'PUT',
      body: processedImage,
      headers: {
        'Content-Type': 'image/jpeg'
      }
    });
    
    if (response.ok) {
      console.log(`성공적으로 업로드됨: ${outputName}`);
      console.log(`다운로드 URL: ${API_URL}/${outputName}`);
      return `${API_URL}/${outputName}`;
    } else {
      console.error(`업로드 실패 (${response.status}): ${outputName}`);
      return null;
    }
  } catch (error) {
    console.error(`이미지 처리 및 업로드 오류:`, error);
    return null;
  }
}

// 메인 함수
async function main() {
  // 처리할 이미지 목록
  const images = [
    { input: 'assets/uploads/mika.png', output: 'mika.jpg' },
    { input: 'assets/uploads/yuki.png', output: 'yuki.jpg' },
    { input: 'assets/uploads/taro.png', output: 'taro.jpg' }
  ];
  
  console.log('이미지 처리 및 업로드 시작...');
  
  const results = [];
  for (const image of images) {
    console.log(`처리 중: ${image.input} -> ${image.output}`);
    const url = await processAndUploadImage(image.input, image.output);
    if (url) {
      results.push({ name: image.output, url });
    }
  }
  
  console.log('처리 완료');
  console.log('결과:', results);
}

// 스크립트 직접 실행 시
if (require.main === module) {
  main().catch(error => {
    console.error('실행 오류:', error);
    process.exit(1);
  });
}

module.exports = { processAndUploadImage };