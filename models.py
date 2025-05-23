import os
from datetime import datetime
from sqlalchemy import Column, Integer, String, Float, Text, DateTime, ForeignKey, Boolean, JSON
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
import json

Base = declarative_base()

class User(Base):
    __tablename__ = 'users'
    
    id = Column(Integer, primary_key=True)
    username = Column(String(80), unique=True, nullable=False)
    email = Column(String(120), unique=True, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    credits = Column(Integer, default=100)
    
    # 관계 설정
    interactions = relationship("Interaction", back_populates="user")
    
    def __repr__(self):
        return f'<User {self.username}>'

class Character(Base):
    __tablename__ = 'characters'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(80), nullable=False)
    description = Column(Text, nullable=True)
    image_url = Column(String(255), nullable=True)
    personality = Column(JSON, nullable=True)  # JSON 형태로 저장
    voice_type = Column(String(50), default='nova')
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # 관계 설정
    interactions = relationship("Interaction", back_populates="character")
    expressions = relationship("CharacterExpression", back_populates="character")
    
    def __repr__(self):
        return f'<Character {self.name}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'description': self.description,
            'image_url': self.image_url,
            'personality': self.personality,
            'voice_type': self.voice_type
        }

class CharacterExpression(Base):
    __tablename__ = 'character_expressions'
    
    id = Column(Integer, primary_key=True)
    character_id = Column(Integer, ForeignKey('characters.id'))
    emotion = Column(String(50), nullable=False)  # happy, sad, angry, etc.
    image_url = Column(String(255), nullable=True)
    
    # 관계 설정
    character = relationship("Character", back_populates="expressions")
    
    def __repr__(self):
        return f'<Expression {self.character_id} - {self.emotion}>'

class Interaction(Base):
    __tablename__ = 'interactions'
    
    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    character_id = Column(Integer, ForeignKey('characters.id'))
    message = Column(Text, nullable=False)
    response = Column(Text, nullable=True)
    emotion = Column(String(50), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # 관계 설정
    user = relationship("User", back_populates="interactions")
    character = relationship("Character", back_populates="interactions")
    
    def __repr__(self):
        return f'<Interaction {self.id}>'