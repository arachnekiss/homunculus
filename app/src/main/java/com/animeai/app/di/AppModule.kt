package com.animeai.app.di

import android.content.Context
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.animeai.app.engine.AnimeCharacterEngine
import com.animeai.app.engine.CameraReactionEngine
import com.animeai.app.engine.VoiceSyncEngine
import com.animeai.app.repository.CharacterRepository
import com.animeai.app.repository.ConversationRepository
import com.animeai.app.repository.UserRepository
import com.animeai.app.service.BillingService
import com.animeai.app.service.OpenAIService
import com.animeai.app.service.ReplitService
import com.animeai.app.util.AudioProcessing
import com.animeai.app.util.ImageProcessing
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOpenAI(): OpenAI {
        return OpenAI(
            config = OpenAIConfig(
                token = System.getenv("OPENAI_API_KEY") ?: "default_key",
                logging = LoggingConfig(LogLevel.None),
                timeout = Timeout(socket = 60.seconds)
            )
        )
    }

    @Provides
    @Singleton
    fun provideOpenAIService(openAI: OpenAI): OpenAIService {
        return OpenAIService()
    }

    @Provides
    @Singleton
    fun provideImageProcessing(): ImageProcessing {
        return ImageProcessing()
    }

    @Provides
    @Singleton
    fun provideAudioProcessing(): AudioProcessing {
        return AudioProcessing()
    }

    @Provides
    @Singleton
    fun provideReplitService(): ReplitService {
        return ReplitService()
    }

    @Provides
    @Singleton
    fun provideAnimeCharacterEngine(
        openAI: OpenAI,
        imageProcessing: ImageProcessing
    ): AnimeCharacterEngine {
        return AnimeCharacterEngine(openAI, imageProcessing)
    }

    @Provides
    @Singleton
    fun provideVoiceSyncEngine(
        openAI: OpenAI,
        audioProcessing: AudioProcessing
    ): VoiceSyncEngine {
        return VoiceSyncEngine(openAI, audioProcessing)
    }

    @Provides
    @Singleton
    fun provideCameraReactionEngine(
        openAI: OpenAI,
        imageProcessing: ImageProcessing
    ): CameraReactionEngine {
        return CameraReactionEngine(openAI, imageProcessing)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        replitService: ReplitService
    ): UserRepository {
        return UserRepository(replitService)
    }

    @Provides
    @Singleton
    fun provideCharacterRepository(
        animeCharacterEngine: AnimeCharacterEngine,
        replitService: ReplitService
    ): CharacterRepository {
        return CharacterRepository(animeCharacterEngine, replitService)
    }

    @Provides
    @Singleton
    fun provideConversationRepository(
        openAI: OpenAI,
        voiceSyncEngine: VoiceSyncEngine,
        characterRepository: CharacterRepository,
        replitService: ReplitService,
        userRepository: UserRepository
    ): ConversationRepository {
        return ConversationRepository(
            openAI,
            voiceSyncEngine,
            characterRepository,
            replitService,
            userRepository
        )
    }

    @Provides
    @Singleton
    fun provideBillingService(
        userRepository: UserRepository
    ): BillingService {
        return BillingService(userRepository)
    }
}
