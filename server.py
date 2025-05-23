import os
import json
import base64
from flask import Flask, request, jsonify
from flask_cors import CORS
import openai
from dotenv import load_dotenv
from PIL import Image
import io
import requests

# Load environment variables
load_dotenv()

# Configure OpenAI
openai.api_key = os.getenv("OPENAI_API_KEY")

app = Flask(__name__)
CORS(app)

@app.route("/api/health", methods=["GET"])
def health_check():
    return jsonify({"status": "OK", "message": "AnimeAI API is running"})

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
        response = openai.chat.completions.create(
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
        emotion_response = openai.chat.completions.create(
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
    if 'image' not in request.json:
        return jsonify({"error": "No image provided"}), 400
    
    try:
        # Get the base64 encoded image
        image_data = request.json['image'].split(',')[1] if ',' in request.json['image'] else request.json['image']
        
        # Decode the image
        image_bytes = base64.b64decode(image_data)
        
        # the newest OpenAI model is "gpt-4o" which was released May 13, 2024.
        # do not change this unless explicitly requested by the user
        response = openai.chat.completions.create(
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
        
        emotion = response.choices[0].message.content.strip().lower()
        
        return jsonify({
            "emotion": emotion
        })
        
    except Exception as e:
        print(f"Error in analyze-expression endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/text-to-speech", methods=["POST"])
def text_to_speech():
    data = request.json
    text = data.get("text", "")
    voice = data.get("voice", "nova")  # Default: nova
    
    try:
        response = openai.audio.speech.create(
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
    base_image = data.get("baseImage", "")
    emotions = data.get("emotions", ["happy", "sad", "angry", "surprised", "neutral"])
    
    # For now, return a placeholder implementation
    # In a real implementation, we would use the OpenAI image model to generate variants
    # based on the base image and emotions
    emotion_images = {}
    
    try:
        # Decode base image
        image_data = base_image.split(',')[1] if ',' in base_image else base_image
        image_bytes = base64.b64decode(image_data)
        
        # For this implementation, we'll just return the base image for all emotions
        # In a production app, we would use AI to generate variations
        for emotion in emotions:
            emotion_images[emotion] = base_image
            
        return jsonify({
            "expressions": emotion_images
        })
        
    except Exception as e:
        print(f"Error in get-character-expressions endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/save-credits", methods=["POST"])
def save_credits():
    data = request.json
    user_id = data.get("userId", "user1")  # Default: user1
    credits = data.get("credits", 0)
    
    # In a production app, this would save to a database
    # For this example, we'll just return a success response
    try:
        return jsonify({
            "success": True,
            "userId": user_id,
            "creditsRemaining": credits
        })
    except Exception as e:
        print(f"Error in save-credits endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/get-credits", methods=["GET"])
def get_credits():
    user_id = request.args.get("userId", "user1")  # Default: user1
    
    # In a production app, this would query a database
    # For this example, we'll return a fixed number
    try:
        return jsonify({
            "userId": user_id,
            "creditsRemaining": 100  # Default value
        })
    except Exception as e:
        print(f"Error in get-credits endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port)