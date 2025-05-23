package com.animeai.app.service

import android.util.Log
import com.animeai.app.model.Config
import com.animeai.app.model.CreditPackage
import com.animeai.app.model.UserCredits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing user credits
 */
@Singleton
class CreditService @Inject constructor() {
    private val TAG = "CreditService"
    
    // In-memory cache of user credits (in a real app, this would use a database)
    private val userCreditsCache = mutableMapOf<String, UserCredits>()
    
    // Available credit packages
    private val creditPackages = listOf(
        CreditPackage(
            id = "credits_10",
            name = "기본 패키지",
            credits = 10,
            price = 1000f,
            description = "일상 대화를 위한 기본 크레딧"
        ),
        CreditPackage(
            id = "credits_50",
            name = "인기 패키지",
            credits = 50,
            price = 4500f,
            description = "가장 인기있는 패키지, 10% 할인"
        ),
        CreditPackage(
            id = "credits_100",
            name = "프리미엄 패키지",
            credits = 100,
            price = 8000f,
            description = "열렬한 사용자를 위한 20% 할인"
        ),
        CreditPackage(
            id = "credits_500",
            name = "프로 패키지",
            credits = 500,
            price = 35000f,
            description = "전문가를 위한 30% 할인"
        )
    )
    
    /**
     * Get user's current credits
     */
    suspend fun getUserCredits(userId: String): Float = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting credits for user: $userId")
            val userCredits = userCreditsCache[userId] ?: createNewUserCredits(userId)
            return@withContext userCredits.credits
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user credits", e)
            return@withContext Config.DEFAULT_CREDITS
        }
    }
    
    /**
     * Update user credits
     */
    suspend fun updateUserCredits(userId: String, newCredits: Float): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating credits for user $userId to $newCredits")
            val userCredits = userCreditsCache[userId] ?: createNewUserCredits(userId)
            
            // Update cached credits
            userCreditsCache[userId] = userCredits.copy(
                credits = newCredits,
                lastUpdated = System.currentTimeMillis()
            )
            
            // In a real app, this would write to a persistent database
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user credits", e)
            return@withContext false
        }
    }
    
    /**
     * Add credits to user account
     */
    suspend fun addCredits(userId: String, credits: Float): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Adding $credits credits to user $userId")
            val currentCredits = getUserCredits(userId)
            return@withContext updateUserCredits(userId, currentCredits + credits)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding credits", e)
            return@withContext false
        }
    }
    
    /**
     * Deduct credits from user account
     */
    suspend fun deductCredits(userId: String, credits: Float): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deducting $credits credits from user $userId")
            val currentCredits = getUserCredits(userId)
            
            // Check if user has enough credits
            if (currentCredits < credits) {
                Log.w(TAG, "User $userId doesn't have enough credits (has $currentCredits, needs $credits)")
                return@withContext false
            }
            
            return@withContext updateUserCredits(userId, currentCredits - credits)
        } catch (e: Exception) {
            Log.e(TAG, "Error deducting credits", e)
            return@withContext false
        }
    }
    
    /**
     * Get available credit packages
     */
    suspend fun getCreditPackages(): List<CreditPackage> = withContext(Dispatchers.IO) {
        return@withContext creditPackages
    }
    
    /**
     * Purchase credits using Google Play billing
     */
    suspend fun purchaseCredits(userId: String, packageId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Purchasing credit package $packageId for user $userId")
            
            // In a real app, this would integrate with Google Play Billing
            // For now, just find the package and add credits
            val creditPackage = creditPackages.find { it.id == packageId }
                ?: throw IllegalArgumentException("Invalid package ID: $packageId")
            
            // Add credits
            addCredits(userId, creditPackage.credits.toFloat())
            
            // Record purchase time
            val userCredits = userCreditsCache[userId]
            if (userCredits != null) {
                userCreditsCache[userId] = userCredits.copy(
                    lastPurchase = System.currentTimeMillis()
                )
            }
            
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error purchasing credits", e)
            return@withContext false
        }
    }
    
    /**
     * Check if user has enough credits for operation
     */
    suspend fun hasEnoughCredits(userId: String, requiredCredits: Float): Boolean = withContext(Dispatchers.IO) {
        val currentCredits = getUserCredits(userId)
        return@withContext currentCredits >= requiredCredits
    }
    
    /**
     * Create a new user credits record
     */
    private fun createNewUserCredits(userId: String): UserCredits {
        val newCredits = UserCredits(
            userId = userId,
            credits = Config.DEFAULT_CREDITS
        )
        userCreditsCache[userId] = newCredits
        return newCredits
    }
}