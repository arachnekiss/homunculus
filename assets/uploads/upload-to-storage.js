// Object Storage 업로드 도구
// Replit의 오브젝트 스토리지에 캐릭터 이미지를 업로드합니다.

const fs = require('fs');
const path = require('path');
const fetch = require('node-fetch');

// 오브젝트 스토리지 정보
const BUCKET_ID = 'replit-objstore-82708a44-60fd-4562-b6e0-ae6312a9c2d6';
const BUCKET_NAME = 'app_storage';
const API_URL = `https://${BUCKET_ID}.replit.dev/${BUCKET_NAME}`;

// 이미지 업로드 함수
async function uploadImage(filePath, fileName) {
  try {
    const fileData = fs.readFileSync(filePath);
    
    const response = await fetch(`${API_URL}/${fileName}`, {
      method: 'PUT',
      body: fileData,
      headers: {
        'Content-Type': getContentType(path.extname(filePath))
      }
    });
    
    if (response.ok) {
      console.log(`성공적으로 업로드됨: ${fileName}`);
      console.log(`다운로드 URL: ${API_URL}/${fileName}`);
      return true;
    } else {
      console.error(`업로드 실패 (${response.status}): ${fileName}`);
      return false;
    }
  } catch (error) {
    console.error(`업로드 오류 (${fileName}):`, error);
    return false;
  }
}

// 파일 확장자에 따른 Content-Type 결정
function getContentType(extension) {
  const contentTypes = {
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.png': 'image/png',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.webp': 'image/webp'
  };
  
  return contentTypes[extension.toLowerCase()] || 'application/octet-stream';
}

// 실행 코드
async function main() {
  // 업로드할 파일 목록
  const filesToUpload = [
    { path: 'assets/uploads/mika.png', name: 'mika.png' },
    { path: 'assets/uploads/yuki.png', name: 'yuki.png' },
    { path: 'assets/uploads/taro.png', name: 'taro.png' }
  ];
  
  console.log(`오브젝트 스토리지 URL: ${API_URL}`);
  
  let successCount = 0;
  
  for (const file of filesToUpload) {
    console.log(`업로드 중: ${file.path} -> ${file.name}`);
    const success = await uploadImage(file.path, file.name);
    if (success) successCount++;
  }
  
  console.log(`업로드 완료: ${successCount}/${filesToUpload.length} 파일`);
}

// 스크립트 직접 실행 시
if (require.main === module) {
  main().catch(error => {
    console.error('실행 오류:', error);
    process.exit(1);
  });
}

module.exports = { uploadImage, getContentType };