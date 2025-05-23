package com.animeai.app.model

/**
 * Configuration constants for the application
 */
object Config {
    // API endpoints
    const val API_BASE_URL = "https://api.openai.com/v1/"
    
    // Default character generation prompts
    val DEFAULT_CHARACTER_PROMPTS = listOf(
        "Female high school student with brown hair and blue eyes, wearing a school uniform",
        "Male wizard with white hair and purple eyes, wearing a magical robe",
        "Female warrior with red hair and green eyes, wearing light armor",
        "Male detective with black hair and glasses, wearing a suit",
        "Female idol with pink hair and colorful eyes, wearing a stage outfit"
    )
    
    // Default credits for new users
    const val DEFAULT_CREDITS = 100f
    
    // Usage costs
    const val TOKEN_COST = 0.00001f   // Cost per token
    const val IMAGE_COST = 0.02f      // Cost per image generation
    const val AUDIO_COST = 0.006f     // Cost per minute of audio
}