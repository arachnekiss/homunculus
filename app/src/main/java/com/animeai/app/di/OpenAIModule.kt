package com.animeai.app.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Dependency injection module for OpenAI API client
 */
@Module
@InstallIn(SingletonComponent::class)
object OpenAIModule {
    
    @Provides
    @Singleton
    fun provideOpenAI(): OpenAI {
        // Get API key from environment (would be stored securely in a real app)
        val apiKey = System.getenv("OPENAI_API_KEY") ?: ""
        
        return OpenAI(
            config = OpenAIConfig(
                token = apiKey,
                logging = LoggingConfig(LogLevel.None),
                timeout = Timeout(socket = 60.seconds)
            )
        )
    }
}