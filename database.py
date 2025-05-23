import os
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import scoped_session, sessionmaker
from dotenv import load_dotenv

# 환경 변수 로드
load_dotenv()

# 데이터베이스 URL 가져오기
DATABASE_URL = os.environ.get("DATABASE_URL")

# 엔진 생성
engine = create_engine(DATABASE_URL)

# 세션 생성
db_session = scoped_session(sessionmaker(autocommit=False, autoflush=False, bind=engine))

# Base 클래스 생성
Base = declarative_base()
Base.query = db_session.query_property()

def init_db():
    # 모델 가져오기
    import models
    
    # 테이블 생성
    Base.metadata.create_all(bind=engine)
    
    # 기본 캐릭터 데이터 추가
    add_default_characters()

def add_default_characters():
    from models import Character, CharacterExpression
    
    # 이미 데이터가 있는지 확인
    existing_characters = db_session.query(Character).all()
    if existing_characters:
        # 이미 데이터가 있으면 추가하지 않음
        return
    
    # 기본 캐릭터 데이터
    characters = [
        {
            "name": "미카",
            "description": "활발하고 친절한 성격의 10대 소녀. 긍정적인 에너지를 가지고 있으며, 친구들을 격려하는 것을 좋아합니다.",
            "image_url": "/assets/mika.png",
            "personality": {
                "type": "Friendly",
                "traits": ["positive", "enthusiastic", "caring"],
                "background": "고등학교 학생회장. 예술과 음악을 좋아합니다.",
                "speechStyle": "친근하고 활기차게 말합니다. 종종 \"-다요!\" 라는 말투를 사용해요."
            },
            "voice_type": "nova"
        },
        {
            "name": "유키",
            "description": "조용하고 사려 깊은 대학생. 책을 읽는 것을 좋아하며, 깊은 철학적 대화를 나누는 것을 즐깁니다.",
            "image_url": "/assets/yuki.png",
            "personality": {
                "type": "Thoughtful",
                "traits": ["calm", "philosophical", "intelligent"],
                "background": "문학을 전공하는 대학생. 카페에서 일하며 소설을 쓰고 있습니다.",
                "speechStyle": "조용하고 신중하게 말합니다. 때때로 책에서 인용구를 사용합니다."
            },
            "voice_type": "alloy"
        },
        {
            "name": "타로",
            "description": "열정적이고 에너지가 넘치는 10대 소년. 스포츠와 모험을 좋아합니다.",
            "image_url": "/assets/taro.png",
            "personality": {
                "type": "Energetic",
                "traits": ["passionate", "brave", "competitive"],
                "background": "고등학교 농구부 주장. 프로 선수가 되는 것이 꿈입니다.",
                "speechStyle": "자신감 있고 열정적으로 말합니다. 종종 \"-다고!\" 라는 말투를 사용해요."
            },
            "voice_type": "onyx"
        }
    ]
    
    # 캐릭터 추가
    for char_data in characters:
        character = Character(
            name=char_data["name"],
            description=char_data["description"],
            image_url=char_data["image_url"],
            personality=char_data["personality"],
            voice_type=char_data["voice_type"]
        )
        db_session.add(character)
    
    # 저장
    db_session.commit()
    
    # 이제 캐릭터별 표정 데이터 추가
    # 현재는 모든 캐릭터가 동일한 이미지를 사용하므로 간단하게 설정
    characters = db_session.query(Character).all()
    emotions = ["happy", "sad", "angry", "surprised", "neutral", "embarrassed", "thoughtful", "excited", "nervous"]
    
    for character in characters:
        base_image = character.image_url
        
        for emotion in emotions:
            expression = CharacterExpression(
                character_id=character.id,
                emotion=emotion,
                image_url=base_image  # 현재는 동일한 이미지 사용, 나중에 다양한 이미지로 업데이트 가능
            )
            db_session.add(expression)
    
    # 저장
    db_session.commit()