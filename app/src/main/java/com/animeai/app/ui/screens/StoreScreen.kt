package com.animeai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.animeai.app.viewmodel.CreditViewModel

data class CreditPackage(
    val id: String,
    val name: String,
    val credits: Int,
    val price: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    creditViewModel: CreditViewModel,
    onNavigateBack: () -> Unit
) {
    val currentCredits = creditViewModel.userCredits.collectAsState().value
    val purchaseState = creditViewModel.purchaseState.collectAsState().value
    
    val creditPackages = listOf(
        CreditPackage("small", "Small Pack", 100, 0.99f),
        CreditPackage("medium", "Medium Pack", 500, 4.99f),
        CreditPackage("large", "Large Pack", 1000, 9.99f),
        CreditPackage("premium", "Premium Pack", 5000, 49.99f)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credit Store") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current Credits Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Current Credits",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        currentCredits.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Information about credits
            Text(
                "Purchase credits to use with your AI character",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Credit packages list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(creditPackages) { pack ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pack.name, style = MaterialTheme.typography.titleMedium)
                                Text("${pack.credits} credits", style = MaterialTheme.typography.bodyMedium)
                            }
                            
                            Button(
                                onClick = { creditViewModel.purchaseCredits(pack.id, pack.credits) }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$${pack.price}")
                            }
                        }
                    }
                }
            }
            
            // Purchase status
            if (purchaseState.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    purchaseState,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
