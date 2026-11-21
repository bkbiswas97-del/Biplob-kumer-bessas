package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CatalogSeedData
import com.example.data.model.Tractor
import com.example.data.model.TractorCondition
import com.example.data.model.TractorOrder
import com.example.data.model.TractorRatingSummary
import com.example.data.model.UiState
import com.example.ui.components.TractorListingCard
import com.example.ui.components.formatIndianNumber

@Composable
fun DashboardScreen(
    userName: String,
    userPhotoUrl: String?,
    isAdmin: Boolean,
    tractorsState: UiState<List<Tractor>>,
    ordersState: UiState<List<TractorOrder>>,
    ratingSummaries: Map<String, TractorRatingSummary>,
    onSelectBrandShortcut: (String) -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onTractorClick: (Tractor) -> Unit,
    onOrderClick: (Tractor) -> Unit,
    onSeedDemoTractors: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val tractors = (tractorsState as? UiState.Success)?.data.orEmpty()
    val orders = (ordersState as? UiState.Success)?.data.orEmpty()
    val excellentCount = tractors.count { it.conditionEnum == TractorCondition.EXCELLENT }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // Top Greeting & Profile Row
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!userPhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = "প্রোফাইল ছবি",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_tractor_logo),
                                    contentDescription = "লোগো",
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "স্বাগতম, $userName",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (isAdmin) "পুরাতন ট্রাক্টর বাজার • এডমিন" else "পুরাতন ট্রাক্টর বাজার • ক্রেতা ড্যাশবোর্ড",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onSignOutClick,
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "সাইন আউট",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Hero Marketplace Banner
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF14532D),
                                        Color(0xFF166534),
                                        Color(0xFF92400E)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "সেরা মানের পুরাতন ট্রাক্টর বাজার",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ভিডিও দেখে, ব্যবহৃত ঘন্টা ও মোট পাওনা যাচাই করে সরাসরি অর্ডার করুন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToCatalog,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFBBF24),
                                        contentColor = Color(0xFF1C1917)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("dashboard_search_cta")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "ট্রাক্টর খুঁজুন",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("সার্চ ও ফিল্টার", fontWeight = FontWeight.Bold)
                                }
                                if (isAdmin) {
                                    OutlinedButton(
                                        onClick = onNavigateToAdmin,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("dashboard_admin_cta")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AdminPanelSettings,
                                            contentDescription = "এডমিন প্যানেল",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("এডমিন প্যানেল")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dashboard Summary Stat Cards
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DashboardStatCard(
                        icon = Icons.Filled.Agriculture,
                        label = "উপলব্ধ ট্রাক্টর",
                        value = formatIndianNumber(tractors.size.toLong()),
                        onClick = onNavigateToCatalog,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        icon = Icons.Filled.Verified,
                        label = "চমৎকার কন্ডিশন",
                        value = formatIndianNumber(excellentCount.toLong()),
                        onClick = onNavigateToCatalog,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "আমার অর্ডার",
                        value = formatIndianNumber(orders.size.toLong()),
                        onClick = onNavigateToOrders,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Brand Quick Filter Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "জনপ্রিয় ব্র্যান্ড অনুযায়ী দেখুন",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CatalogSeedData.brandOptions) { brand ->
                            FilterChip(
                                selected = false,
                                onClick = { onSelectBrandShortcut(brand) },
                                label = { Text(brand) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.testTag("brand_shortcut_$brand")
                            )
                        }
                    }
                }
            }

            // Section Header: Featured Tractors
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "বাজারে থাকা ট্রাক্টরসমূহ",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "সব দেখুন",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToCatalog)
                            .padding(6.dp)
                    )
                }
            }

            // Tractors List
            when (tractorsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = tractorsState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    if (tractorsState.data.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Agriculture,
                                        contentDescription = "খালি তালিকা",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Text(
                                        text = "এখনো কোনো ট্রাক্টর যুক্ত করা হয়নি",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = if (isAdmin) {
                                            "নিচের বাটনে ক্লিক করে ৬টি নমুনা ট্রাক্টর লোড করুন অথবা এডমিন প্যানেল থেকে যুক্ত করুন।"
                                        } else {
                                            "এডমিন নতুন ট্রাক্টর যুক্ত করলে এখানে দেখতে পাবেন।"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isAdmin) {
                                        Button(
                                            onClick = onSeedDemoTractors,
                                            modifier = Modifier.testTag("seed_tractors_button")
                                        ) {
                                            Text("নমুনা ট্রাক্টর লোড করুন")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        items(tractorsState.data, key = { it.id }) { tractor ->
                            TractorListingCard(
                                tractor = tractor,
                                ratingSummary = ratingSummaries[tractor.id],
                                onViewDetails = { onTractorClick(tractor) },
                                onOrderClick = { onOrderClick(tractor) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
