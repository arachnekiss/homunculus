# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep model classes for serialization
-keep class com.animeai.app.model.** { *; }

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# OpenAI client
-keep class com.aallam.openai.** { *; }
-keep class kotlinx.serialization.** { *; }
-keep class io.ktor.** { *; }