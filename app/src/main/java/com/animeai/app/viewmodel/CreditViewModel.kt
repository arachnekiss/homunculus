package com.animeai.app.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animeai.app.repository.UserRepository
import com.animeai.app.service.BillingService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreditViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val billingService: BillingService
) : ViewModel() {
    
    // User credits
    val userCredits = userRepository.credits
    
    // Purchase state
    val purchaseState = billingService.purchaseState
    
    init {
        // Initialize billing service
        billingService.initialize()
    }
    
    /**
     * Purchase credits
     */
    fun purchaseCredits(packageId: String, amount: Float) {
        viewModelScope.launch {
            try {
                // In a real app, this would need an Activity reference
                // But for this demo, we'll pass null
                val success = billingService.purchaseCredits(null, packageId)
                
                Log.d(TAG, "Credit purchase initiated: $packageId for $amount credits, success=$success")
            } catch (e: Exception) {
                Log.e(TAG, "Error purchasing credits", e)
            }
        }
    }
    
    /**
     * Check if user has sufficient credits
     */
    fun hasEnoughCredits(amount: Float): Boolean {
        return userRepository.hasEnoughCredits(amount)
    }
    
    companion object {
        private const val TAG = "CreditViewModel"
    }
}
