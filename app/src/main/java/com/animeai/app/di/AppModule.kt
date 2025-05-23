package com.animeai.app.di

import com.animeai.app.service.ConversationService
import com.animeai.app.service.ConversationServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI 모듈 설정
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * ConversationService 인터페이스를 구현한 ConversationServiceImpl 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindConversationService(
        conversationServiceImpl: ConversationServiceImpl
    ): ConversationService
    
    /**
     * VoiceService 인터페이스를 구현한 VoiceServiceImpl 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindVoiceService(
        voiceServiceImpl: VoiceServiceImpl
    ): VoiceService
}