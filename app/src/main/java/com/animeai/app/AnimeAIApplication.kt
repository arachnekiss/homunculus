package com.animeai.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 애플리케이션 클래스
 * Hilt 의존성 주입 초기화
 */
@HiltAndroidApp
class AnimeAIApplication : Application()