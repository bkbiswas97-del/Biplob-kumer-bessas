package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.CatalogSeedData
import com.example.data.model.Tractor
import com.example.data.model.TractorRatingSummary
import com.example.data.model.TractorReview
import com.example.ui.components.ConditionBadge
import com.example.ui.components.InteractiveStarBar
import com.example.ui.components.TractorVideoPlayer
import com.example.ui.components.formatIndianNumber
import com.example.ui.components.formatTaka
import com.example.ui.components.toBengaliDigits
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TractorDetailScreen(
    tractor: Tractor,
    currentUserId: String,
    isAdmin: Boolean,
    reviews: List<TractorReview>,
    ratingSummary: TractorRatingSummary?,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onOrderClick: () -> Unit,
    onSubmitReview: (existingReviewId: String?, rating: Int, comment: String, onDone: () -> Unit) -> Unit,
    onDeleteReview: (reviewId: String) -> Unit
) {
    BackHandler(onBack = onBack)

    var showVideoTab by remember { mutableStateOf(true) }
    val myExistingReview = remember(reviews, currentUserId) {
        reviews.firstOrNull { it.userId == currentUserId }
    }

    var editingReviewId by remember(myExistingReview?.id) { mutableStateOf<String?>(null) }
    var selectedRating by remember(myExistingReview?.id) { mutableIntStateOf(5) }
    var commentInput by remember(myExistingReview?.id) { mutableStateOf("") }
    var reviewError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tractor.brand,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "পেছনে যান"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "বিক্রয় মূল্য",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTaka(tractor.sellingPrice),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onOrderClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("detail_order_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCartCheckout,
                            contentDescription = "অর্ডার করুন"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "অর্ডার করুন",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                // Media switcher: Video / Photo
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = showVideoTab,
                                onClick = { showVideoTab = true },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.PlayCircle,
                                        contentDescription = "ভিডিও",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            ) {
                                Text("ভিডিও দেখুন")
                            }
                            SegmentedButton(
                                selected = !showVideoTab,
                                onClick = { showVideoTab = false },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Image,
                                        contentDescription = "ছবি",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            ) {
                                Text("ছবি দেখুন")
                            }
                        }

                        if (showVideoTab) {
                            TractorVideoPlayer(
                                videoKey = tractor.videoKey,
                                videoUrl = tractor.videoUrl
                            )
                        } else {
                            Image(
                                painter = painterResource(
                                    id = CatalogSeedData.resolveDrawable(tractor.imageKey)
                                ),
                                contentDescription = tractor.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    }
                }

                // Title & Quick Badges
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ConditionBadge(condition = tractor.conditionEnum)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "লোকেশন",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = tractor.location,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = tractor.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "মডেল: ${tractor.model}  •  সাল: ${toBengaliDigits(tractor.year.toString())}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Price, Used Hours, Total Due, Selling Price Breakdown Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "মূল্য ও ব্যবহারের হিসাব",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            DetailSpecRow(
                                label = "দাম (মূল বাজার মূল্য)",
                                value = formatTaka(tractor.originalPrice),
                                strikethrough = true,
                                valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            DetailSpecRow(
                                label = "বিক্রয় মূল্য",
                                value = formatTaka(tractor.sellingPrice),
                                valueColor = MaterialTheme.colorScheme.primary,
                                bold = true
                            )
                            DetailSpecRow(
                                label = "ব্যবহৃত ঘন্টা",
                                value = "${formatIndianNumber(tractor.usedHours)} ঘন্টা"
                            )
                            DetailSpecRow(
                                label = "মোট পাওনা",
                                value = if (tractor.totalDue > 0) formatTaka(tractor.totalDue) else "০ টাকা (কোনো পাওনা নেই)",
                                valueColor = if (tractor.totalDue > 0) StatusWarning else StatusSuccess,
                                bold = true
                            )
                            HorizontalDivider()
                            Text(
                                text = "বর্তমান কন্ডিশন: ${tractor.conditionEnum.labelBn}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = tractor.conditionNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Features Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "ট্রাক্টরের প্রধান ফিচারসমূহ",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            tractor.features.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "ফিচার",
                                        tint = StatusSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Reviews & Star Rating Section
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ক্রেতাদের রিভিউ ও রেটিং",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "গড় রেটিং",
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val avg = ratingSummary?.avgRating ?: 0.0
                                        val count = ratingSummary?.reviewCount ?: 0
                                        Text(
                                            text = if (count > 0) {
                                                "${toBengaliDigits(String.format(Locale.US, "%.1f", avg))} / ৫ (${formatIndianNumber(count.toLong())})"
                                            } else {
                                                "কোনো রিভিউ নেই"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            HorizontalDivider()

                            // Review Composer
                            if (myExistingReview == null || editingReviewId != null) {
                                Text(
                                    text = if (editingReviewId != null) "আপনার রিভিউ সম্পাদনা করুন" else "আপনার রেটিং ও রিভিউ দিন",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                InteractiveStarBar(
                                    rating = selectedRating,
                                    onRatingSelected = { selectedRating = it }
                                )
                                OutlinedTextField(
                                    value = commentInput,
                                    onValueChange = { commentInput = it; reviewError = null },
                                    label = { Text("ট্রাক্টর সম্পর্কে আপনার মতামত লিখুন") },
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("review_comment_input")
                                )
                                if (!reviewError.isNullOrBlank()) {
                                    Text(
                                        text = reviewError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val clean = commentInput.trim()
                                            if (clean.isEmpty()) {
                                                reviewError = "অনুগ্রহ করে কিছু মন্তব্য লিখুন।"
                                            } else {
                                                onSubmitReview(editingReviewId, selectedRating, clean) {
                                                    editingReviewId = null
                                                    commentInput = ""
                                                    selectedRating = 5
                                                }
                                            }
                                        },
                                        enabled = !isSubmitting,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("submit_review_button")
                                    ) {
                                        if (isSubmitting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text(if (editingReviewId != null) "আপডেট করুন" else "রিভিউ পোস্ট করুন")
                                        }
                                    }
                                    if (editingReviewId != null) {
                                        Button(
                                            onClick = {
                                                editingReviewId = null
                                                commentInput = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Text("বাতিল")
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "আপনি ইতিমধ্যে এই ট্রাক্টরে রিভিউ দিয়েছেন। নিচে আপনার রিভিউ থেকে সম্পাদনা বা মুছতে পারবেন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Individual Reviews List
                items(reviews, key = { it.id }) { review ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = review.reviewerName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(review.rating.toInt().coerceIn(1, 5)) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = review.comment,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (review.userId == currentUserId || isAdmin) {
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (review.userId == currentUserId) {
                                        IconButton(
                                            onClick = {
                                                editingReviewId = review.id
                                                selectedRating = review.rating.toInt().coerceIn(1, 5)
                                                commentInput = review.comment
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = "রিভিউ সম্পাদনা",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onDeleteReview(review.id) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "রিভিউ মুছুন",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun DetailSpecRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false,
    strikethrough: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                textDecoration = if (strikethrough) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}
