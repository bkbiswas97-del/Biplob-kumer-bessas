package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.CatalogSeedData
import com.example.data.model.OrderStatus
import com.example.data.model.Tractor
import com.example.data.model.TractorCondition
import com.example.data.model.TractorRatingSummary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusInfoBg
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningBg
import java.util.Locale

private val BENGALI_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

fun toBengaliDigits(input: String): String = buildString {
    for (ch in input) {
        if (ch in '0'..'9') append(BENGALI_DIGITS[ch - '0']) else append(ch)
    }
}

fun formatIndianNumber(value: Long): String {
    val neg = value < 0
    val str = kotlin.math.abs(value).toString()
    val formatted = if (str.length <= 3) {
        str
    } else {
        val lastThree = str.takeLast(3)
        val rest = str.dropLast(3)
        val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
        "$grouped,$lastThree"
    }
    return toBengaliDigits(if (neg) "-$formatted" else formatted)
}

fun formatTaka(amount: Long): String = "৳ ${formatIndianNumber(amount)}"

@Composable
fun ConditionBadge(condition: TractorCondition, modifier: Modifier = Modifier) {
    val (bg, fg) = when (condition) {
        TractorCondition.EXCELLENT -> StatusSuccessBg to StatusSuccess
        TractorCondition.GOOD -> StatusInfoBg to StatusInfo
        TractorCondition.FAIR -> StatusWarningBg to StatusWarning
        TractorCondition.NEEDS_SERVICE -> StatusDangerBg to StatusDanger
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = condition.labelBn,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        OrderStatus.PENDING -> StatusWarningBg to StatusWarning
        OrderStatus.CONFIRMED -> StatusInfoBg to StatusInfo
        OrderStatus.DELIVERED -> StatusSuccessBg to StatusSuccess
        OrderStatus.CANCELLED -> StatusDangerBg to StatusDanger
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = status.labelBn,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StarRatingDisplay(
    avgRating: Double,
    reviewCount: Int,
    starSize: Dp = 16.dp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "রেটিং",
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(starSize)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (reviewCount > 0) {
                "${toBengaliDigits(String.format(Locale.US, "%.1f", avgRating))} (${formatIndianNumber(reviewCount.toLong())})"
            } else {
                "নতুন"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InteractiveStarBar(
    rating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        (1..5).forEach { star ->
            IconButton(
                onClick = { onRatingSelected(star) },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("star_rating_$star")
            ) {
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "$star স্টার",
                    tint = if (star <= rating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun TractorListingCard(
    tractor: Tractor,
    ratingSummary: TractorRatingSummary?,
    onViewDetails: () -> Unit,
    onOrderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails)
            .testTag("tractor_card_${tractor.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
            ) {
                Image(
                    painter = painterResource(id = CatalogSeedData.resolveDrawable(tractor.imageKey)),
                    contentDescription = tractor.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(188.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(188.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.12f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter)
                ) {
                    ConditionBadge(condition = tractor.conditionEnum)
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircle,
                                contentDescription = "ভিডিও রয়েছে",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ভিডিও দেখুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${tractor.brand} • ${toBengaliDigits(tractor.year.toString())}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "লোকেশন",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = tractor.location,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1C1917)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tractor.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = tractor.model,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    StarRatingDisplay(
                        avgRating = ratingSummary?.avgRating ?: 0.0,
                        reviewCount = ratingSummary?.reviewCount ?: 0
                    )
                }

                // Financial & usage summary grid
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MetricMiniItem(
                                label = "বিক্রয় মূল্য",
                                value = formatTaka(tractor.sellingPrice),
                                valueColor = MaterialTheme.colorScheme.primary,
                                isBold = true
                            )
                            MetricMiniItem(
                                label = "মূল দাম",
                                value = formatTaka(tractor.originalPrice),
                                valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                strikethrough = true
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = "ব্যবহৃত ঘন্টা",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ব্যবহৃত: ${formatIndianNumber(tractor.usedHours)} ঘন্টা",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (tractor.totalDue > 0) {
                                    "মোট পাওনা: ${formatTaka(tractor.totalDue)}"
                                } else {
                                    "পাওনা: ০ টাকা (ক্লিয়ার)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (tractor.totalDue > 0) StatusWarning else StatusSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onViewDetails,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("details_button_${tractor.id}")
                    ) {
                        Text("ভিডিও ও ফিচার", maxLines = 1)
                    }
                    Button(
                        onClick = onOrderClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("order_button_${tractor.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCartCheckout,
                            contentDescription = "অর্ডার করুন",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("অর্ডার করুন", maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMiniItem(
    label: String,
    value: String,
    valueColor: Color,
    isBold: Boolean = false,
    strikethrough: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                textDecoration = if (strikethrough) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFormBottomSheet(
    tractor: Tractor,
    defaultCustomerName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, address: String, phone: String, note: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customerName by remember { mutableStateOf(defaultCustomerName) }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ট্রাক্টর অর্ডার ফর্ম",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = tractor.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "বিক্রয় মূল্য: ${formatTaka(tractor.sellingPrice)}  •  মোট পাওনা: ${formatTaka(tractor.totalDue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it; validationError = null },
                label = { Text("আপনার নাম *") },
                placeholder = { Text("যেমন: মোঃ রহিম উদ্দিন") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_name_input")
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it; validationError = null },
                label = { Text("সম্পূর্ণ ঠিকানা *") },
                placeholder = { Text("গ্রাম, ইউনিয়ন, উপজেলা, জেলা") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_address_input")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; validationError = null },
                label = { Text("মোবাইল নম্বর *") },
                placeholder = { Text("যেমন: 017XXXXXXXX") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_phone_input")
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("অতিরিক্ত মন্তব্য (ঐচ্ছিক)") },
                placeholder = { Text("ডেলিভারি বা দেখার সময় সম্পর্কে লিখুন") },
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_note_input")
            )

            if (!validationError.isNullOrBlank()) {
                Text(
                    text = validationError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("order_cancel_button")
                ) {
                    Text("বাতিল")
                }

                Button(
                    onClick = {
                        val cleanName = customerName.trim()
                        val cleanAddress = address.trim()
                        val cleanPhone = phone.trim()
                        when {
                            cleanName.length < 2 -> validationError = "অনুগ্রহ করে সঠিক নাম লিখুন।"
                            cleanAddress.length < 5 -> validationError = "অনুগ্রহ করে সম্পূর্ণ ঠিকানা লিখুন (কমপক্ষে ৫ অক্ষর)।"
                            cleanPhone.length < 6 -> validationError = "অনুগ্রহ করে সঠিক মোবাইল নম্বর লিখুন।"
                            else -> onSubmit(cleanName, cleanAddress, cleanPhone, note.trim().ifEmpty { null })
                        }
                    },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(50.dp)
                        .testTag("order_submit_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("সাবমিট করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
