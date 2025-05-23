import os
import json
import base64
from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
from openai import OpenAI
from dotenv import load_dotenv
from PIL import Image
import io
import requests
from database import db_session, init_db
from models import User, Character, CharacterExpression, Interaction

# Load environment variables
load_dotenv()

# Configure OpenAI
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

app = Flask(__name__, static_url_path='', static_folder='.')
CORS(app, resources={r"/*": {"origins": "*"}})

# 애플리케이션 시작 시 데이터베이스 초기화
with app.app_context():
    # 모델 클래스 가져오기
    from models import Base
    from database import engine
    # 테이블 생성
    Base.metadata.create_all(bind=engine)

@app.teardown_appcontext
def shutdown_session(exception=None):
    db_session.remove()

@app.route("/", methods=["GET"])
def root():
    # 기본 경로로 접속하면 웹 인터페이스 제공
    return app.send_static_file('index.html')

@app.route("/api/status", methods=["GET"])
def status():
    # API 상태 확인용 엔드포인트
    return jsonify({"status": "OK", "message": "AnimeAI API Server"})

@app.route("/api/health", methods=["GET"])
def health_check():
    return jsonify({"status": "OK", "message": "AnimeAI API is running"})

@app.route("/api/characters", methods=["GET"])
def get_characters():
    try:
        characters = db_session.query(Character).all()
        character_list = []
        
        for character in characters:
            char_dict = character.to_dict()
            
            # 감정 표현 이미지 가져오기
            expressions = {}
            for expr in character.expressions:
                expressions[expr.emotion] = expr.image_url
                
            char_dict["expressions"] = expressions
            character_list.append(char_dict)
            
        return jsonify({
            "success": True,
            "characters": character_list
        })
    except Exception as e:
        print(f"Error in get-characters endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/chat", methods=["POST"])
def chat():
    data = request.json
    if not data:
        return jsonify({"error": "No data provided"}), 400
        
    user_message = data.get("message", "")
    character_persona = data.get("persona", {})
    chat_history = data.get("history", [])
    
    try:
        # the newest OpenAI model is "gpt-4o" which was released May 13, 2024.
        # do not change this unless explicitly requested by the user
        response = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": f"You are an anime character with the following traits: {json.dumps(character_persona)}. Respond as this character would, with appropriate tone, expressions, and mannerisms."},
                *[{"role": msg["role"], "content": msg["content"]} for msg in chat_history],
                {"role": "user", "content": user_message}
            ],
            temperature=0.7,
            max_tokens=150
        )
        
        character_response = response.choices[0].message.content
        if character_response is None:
            character_response = "..."
        
        # Get character's emotion based on the response
        emotion_response = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": "Extract the primary emotion from this anime character's response. Output only one word: happy, sad, angry, surprised, embarrassed, thoughtful, excited, nervous, or neutral."},
                {"role": "user", "content": character_response}
            ],
            temperature=0.3,
            max_tokens=10
        )
        
        emotion = emotion_response.choices[0].message.content
        if emotion is None:
            emotion = "neutral"
        else:
            emotion = emotion.strip().lower()
        
        return jsonify({
            "response": character_response,
            "emotion": emotion
        })
        
    except Exception as e:
        print(f"Error in chat endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/analyze-expression", methods=["POST"])
def analyze_expression():
    if not request.json or 'image' not in request.json:
        return jsonify({"error": "No image provided"}), 400
    
    try:
        # Get the base64 encoded image
        image_data = request.json['image'].split(',')[1] if ',' in request.json['image'] else request.json['image']
        
        # Decode the image
        image_bytes = base64.b64decode(image_data)
        
        # the newest OpenAI model is "gpt-4o" which was released May 13, 2024.
        # do not change this unless explicitly requested by the user
        response = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "text", 
                            "text": "Analyze this facial expression and tell me the emotion. Only respond with one word: happy, sad, angry, surprised, neutral, confused, or other."
                        },
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{image_data}"
                            }
                        }
                    ]
                }
            ],
            max_tokens=10
        )
        
        emotion = response.choices[0].message.content
        if emotion is None:
            emotion = "neutral"
        else:
            emotion = emotion.strip().lower()
        
        return jsonify({
            "emotion": emotion
        })
        
    except Exception as e:
        print(f"Error in analyze-expression endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/text-to-speech", methods=["POST"])
def text_to_speech():
    data = request.json
    if not data:
        return jsonify({"error": "No data provided"}), 400
        
    text = data.get("text", "")
    voice = data.get("voice", "nova")  # Default: nova
    
    try:
        response = client.audio.speech.create(
            model="tts-1",
            voice=voice,
            input=text
        )
        
        # Get the audio data
        audio_data = response.content
        
        # Convert to base64 for sending to frontend
        audio_base64 = base64.b64encode(audio_data).decode('utf-8')
        
        return jsonify({
            "audio": audio_base64
        })
        
    except Exception as e:
        print(f"Error in text-to-speech endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/get-character-expressions", methods=["POST"])
def get_character_expressions():
    data = request.json
    if not data:
        return jsonify({"error": "No data provided"}), 400
        
    base_image = data.get("baseImage", "")
    emotions = data.get("emotions", ["happy", "sad", "angry", "surprised", "neutral"])
    
    # For now, return a placeholder implementation
    # In a real implementation, we would use the OpenAI image model to generate variants
    # based on the base image and emotions
    emotion_images = {}
    
    try:
        # Decode base image
        if base_image:
            image_data = base_image.split(',')[1] if ',' in base_image else base_image
            image_bytes = base64.b64decode(image_data)
            
            # For this implementation, we'll just return the base image for all emotions
            # In a production app, we would use AI to generate variations
            for emotion in emotions:
                emotion_images[emotion] = base_image
        else:
            # Return empty dictionary if no base image provided
            for emotion in emotions:
                emotion_images[emotion] = ""
            
        return jsonify({
            "expressions": emotion_images
        })
        
    except Exception as e:
        print(f"Error in get-character-expressions endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

# 새 캐릭터 이미지를 생성하는 엔드포인트
@app.route("/api/generate-character-image", methods=["POST"])
def generate_character_image():
    data = request.json
    if not data or "prompt" not in data:
        return jsonify({"error": "No prompt provided"}), 400

    prompt = data.get("prompt")
    size = data.get("size", "1024x1024")
    base_image = data.get("image")
    animate = data.get("animate", False)

    try:
        response = client.images.generate(
            model="gpt-image-1",
            prompt=prompt,
            image=base_image,
            size=size,
            quality="hd",
            n=1,
            response_format="b64_json",
        )

        image_base64 = response.data[0].b64_json

        if animate:
            frames = [image_base64] * 4  # placeholder animation frames
            return jsonify({"frames": frames})

        return jsonify({"image": image_base64})

    except Exception as e:
        print(f"Error in generate-character-image endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/save-credits", methods=["POST"])
def save_credits():
    data = request.json
    if not data:
        return jsonify({"error": "No data provided"}), 400
        
    user_id = data.get("userId", "user1")  # Default: user1
    credits = data.get("credits", 0)
    
    try:
        # 데이터베이스에서 사용자 찾기
        user = db_session.query(User).filter_by(username=user_id).first()
        
        # 사용자가 없으면 생성
        if not user:
            user = User(username=user_id, credits=credits)
            db_session.add(user)
        else:
            # 기존 사용자 크레딧 업데이트
            user.credits = credits
            
        db_session.commit()
        
        return jsonify({
            "success": True,
            "userId": user.username,
            "creditsRemaining": user.credits
        })
    except Exception as e:
        print(f"Error in save-credits endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/get-credits", methods=["GET"])
def get_credits():
    user_id = request.args.get("userId", "user1") if request.args else "user1"
    
    try:
        # 데이터베이스에서 사용자 찾기
        user = db_session.query(User).filter_by(username=user_id).first()
        
        # 사용자가 없으면 생성
        if not user:
            user = User(username=user_id, credits=100)
            db_session.add(user)
            db_session.commit()
            
        return jsonify({
            "userId": user.username,
            "creditsRemaining": user.credits
        })
    except Exception as e:
        print(f"Error in get-credits endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    app.run(host="0.0.0.0", port=port)