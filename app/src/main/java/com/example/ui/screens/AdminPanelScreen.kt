package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CatalogSeedData
import com.example.data.model.OrderStatus
import com.example.data.model.Tractor
import com.example.data.model.TractorCondition
import com.example.data.model.TractorOrder
import com.example.data.model.UiState
import com.example.ui.components.ConditionBadge
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.formatIndianNumber
import com.example.ui.components.formatTaka
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminPanelScreen(
    ordersState: UiState<List<TractorOrder>>,
    tractorsState: UiState<List<Tractor>>,
    isSubmitting: Boolean,
    onUpdateOrderStatus: (orderId: String, status: OrderStatus) -> Unit,
    onSaveTractor: (
        tractorId: String?,
        brand: String,
        model: String,
        title: String,
        year: Long,
        originalPrice: Long,
        usedHours: Long,
        totalDue: Long,
        sellingPrice: Long,
        condition: TractorCondition,
        conditionNote: String,
        features: List<String>,
        location: String,
        imageKey: String,
        videoKey: String,
        videoUrl: String?,
        status: String,
        onDone: () -> Unit
    ) -> Unit,
    onDeleteTractor: (String) -> Unit,
    onSeedDemoTractors: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var orderStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var editingTractor by remember { mutableStateOf<Tractor?>(null) }
    var showTractorForm by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 900.dp)
        ) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column {
                            Text(
                                text = "এডমিন প্যানেল",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "সকল ক্রেতার অর্ডার ও ট্রাক্টর ইনভেন্টরি পরিচালনা",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("অর্ডারসমূহ") },
                            modifier = Modifier.testTag("admin_tab_orders")
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("ট্রাক্টর সমূহ") },
                            modifier = Modifier.testTag("admin_tab_tractors")
                        )
                    }
                }
            }

            if (selectedTabIndex == 0) {
                // Admin Orders Tab
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = orderStatusFilter == null,
                                    onClick = { orderStatusFilter = null },
                                    label = { Text("সকল অর্ডার") }
                                )
                            }
                            items(OrderStatus.entries) { status ->
                                FilterChip(
                                    selected = orderStatusFilter == status,
                                    onClick = {
                                        orderStatusFilter = if (orderStatusFilter == status) null else status
                                    },
                                    label = { Text(status.labelBn) }
                                )
                            }
                        }
                    }

                    when (ordersState) {
                        is UiState.Loading -> {
                            item {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp)
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is UiState.Error -> {
                            item {
                                Text(
                                    text = ordersState.message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        is UiState.Success -> {
                            val filtered = if (orderStatusFilter == null) {
                                ordersState.data
                            } else {
                                ordersState.data.filter { it.statusEnum == orderStatusFilter }
                            }

                            item {
                                Text(
                                    text = "মোট অর্ডার: ${formatIndianNumber(filtered.size.toLong())}টি",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (filtered.isEmpty()) {
                                item {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "এখনো কোনো অর্ডার পাওয়া যায়নি।",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(24.dp)
                                        )
                                    }
                                }
                            } else {
                                items(filtered, key = { it.id }) { order ->
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_order_card_${order.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = order.tractorTitle,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                OrderStatusBadge(status = order.statusEnum)
                                            }

                                            Text(
                                                text = "বিক্রয় মূল্য: ${formatTaka(order.sellingPrice)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )

                                            HorizontalDivider()

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Person,
                                                    contentDescription = "নাম",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "নাম: ${order.customerName}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Phone,
                                                        contentDescription = "মোবাইল",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "মোবাইল: ${order.phone}",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        val dialIntent = Intent(
                                                            Intent.ACTION_DIAL,
                                                            Uri.parse("tel:${order.phone}")
                                                        )
                                                        context.startActivity(dialIntent)
                                                    },
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Call,
                                                        contentDescription = "কল করুন",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("কল")
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.Top) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocationOn,
                                                    contentDescription = "ঠিকানা",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "ঠিকানা: ${order.address}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            if (!order.note.isNullOrBlank()) {
                                                Text(
                                                    text = "মন্তব্য: ${order.note}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            order.createdAt?.let { ts ->
                                                Text(
                                                    text = "অর্ডারের সময়: ${dateFormat.format(ts.toDate())}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (order.statusEnum != OrderStatus.CONFIRMED) {
                                                    Button(
                                                        onClick = {
                                                            onUpdateOrderStatus(order.id, OrderStatus.CONFIRMED)
                                                        },
                                                        enabled = !isSubmitting,
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.testTag("admin_confirm_${order.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.CheckCircle,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("নিশ্চিত করুন")
                                                    }
                                                }
                                                if (order.statusEnum != OrderStatus.DELIVERED) {
                                                    Button(
                                                        onClick = {
                                                            onUpdateOrderStatus(order.id, OrderStatus.DELIVERED)
                                                        },
                                                        enabled = !isSubmitting,
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.secondary
                                                        ),
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.testTag("admin_deliver_${order.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.LocalShipping,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("ডেলিভারি সম্পন্ন")
                                                    }
                                                }
                                                if (order.statusEnum != OrderStatus.CANCELLED) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            onUpdateOrderStatus(order.id, OrderStatus.CANCELLED)
                                                        },
                                                        enabled = !isSubmitting,
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Text("বাতিল")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Admin Tractors Tab
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    editingTractor = null
                                    showTractorForm = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("admin_add_tractor_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "যুক্ত করুন")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("নতুন ট্রাক্টর যুক্ত করুন")
                            }

                            OutlinedButton(
                                onClick = onSeedDemoTractors,
                                enabled = !isSubmitting,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("নমুনা লোড")
                            }
                        }
                    }

                    when (tractorsState) {
                        is UiState.Loading -> {
                            item {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp)
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is UiState.Error -> {
                            item {
                                Text(
                                    text = tractorsState.message,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        is UiState.Success -> {
                            items(tractorsState.data, key = { it.id }) { tractor ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
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
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = tractor.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${tractor.brand} • ${tractor.model} • স্ট্যাটাস: ${tractor.status}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            ConditionBadge(condition = tractor.conditionEnum)
                                        }
                                        Text(
                                            text = "বিক্রয় মূল্য: ${formatTaka(tractor.sellingPrice)}  |  ব্যবহৃত: ${formatIndianNumber(tractor.usedHours)} ঘন্টা  |  মোট পাওনা: ${formatTaka(tractor.totalDue)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    editingTractor = tractor
                                                    showTractorForm = true
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = "সম্পাদনা",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteTractor(tractor.id) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "মুছুন",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTractorForm) {
        AdminTractorEditorSheet(
            initialTractor = editingTractor,
            isSubmitting = isSubmitting,
            onDismiss = { showTractorForm = false },
            onSave = { brand, model, title, year, orig, hours, due, selling, cond, condNote, feats, loc, imgKey, vidKey, vidUrl, status ->
                onSaveTractor(
                    editingTractor?.id,
                    brand, model, title, year, orig, hours, due, selling,
                    cond, condNote, feats, loc, imgKey, vidKey, vidUrl, status
                ) {
                    showTractorForm = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AdminTractorEditorSheet(
    initialTractor: Tractor?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        brand: String,
        model: String,
        title: String,
        year: Long,
        originalPrice: Long,
        usedHours: Long,
        totalDue: Long,
        sellingPrice: Long,
        condition: TractorCondition,
        conditionNote: String,
        features: List<String>,
        location: String,
        imageKey: String,
        videoKey: String,
        videoUrl: String?,
        status: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var brand by remember { mutableStateOf(initialTractor?.brand ?: "মাহিন্দ্রা") }
    var model by remember { mutableStateOf(initialTractor?.model ?: "") }
    var title by remember { mutableStateOf(initialTractor?.title ?: "") }
    var yearStr by remember { mutableStateOf((initialTractor?.year ?: 2020L).toString()) }
    var originalPriceStr by remember { mutableStateOf((initialTractor?.originalPrice ?: 950000L).toString()) }
    var sellingPriceStr by remember { mutableStateOf((initialTractor?.sellingPrice ?: 650000L).toString()) }
    var usedHoursStr by remember { mutableStateOf((initialTractor?.usedHours ?: 1200L).toString()) }
    var totalDueStr by remember { mutableStateOf((initialTractor?.totalDue ?: 0L).toString()) }
    var condition by remember { mutableStateOf(initialTractor?.conditionEnum ?: TractorCondition.GOOD) }
    var conditionNote by remember { mutableStateOf(initialTractor?.conditionNote ?: "") }
    var featuresText by remember {
        mutableStateOf(
            initialTractor?.features?.joinToString(", ")
                ?: "পাওয়ার স্টিয়ারিং, অয়েল ব্রেক, ভালো টায়ার"
        )
    }
    var location by remember { mutableStateOf(initialTractor?.location ?: "বগুড়া") }
    var videoUrl by remember { mutableStateOf(initialTractor?.videoUrl ?: "") }
    var status by remember { mutableStateOf(initialTractor?.status ?: "available") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val mediaKeys = listOf(
        "mahindra_575" to "মাহিন্দ্রা",
        "sonalika_750" to "সোনালিকা",
        "swaraj_744" to "স্বরাজ",
        "johndeere_5050" to "জন ডিয়ার"
    )
    var selectedMediaKey by remember { mutableStateOf(initialTractor?.imageKey ?: "mahindra_575") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (initialTractor == null) "নতুন ট্রাক্টর যোগ করুন" else "ট্রাক্টর সম্পাদনা করুন",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text("ব্র্যান্ড নির্বাচন:", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CatalogSeedData.brandOptions.forEach { b ->
                    FilterChip(
                        selected = brand == b,
                        onClick = { brand = b },
                        label = { Text(b) }
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("শিরোনাম *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("মডেল *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = yearStr,
                    onValueChange = { yearStr = it },
                    label = { Text("মডেল সাল") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = usedHoursStr,
                    onValueChange = { usedHoursStr = it },
                    label = { Text("ব্যবহৃত ঘন্টা") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = originalPriceStr,
                    onValueChange = { originalPriceStr = it },
                    label = { Text("মূল দাম (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = sellingPriceStr,
                    onValueChange = { sellingPriceStr = it },
                    label = { Text("বিক্রয় মূল্য (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = totalDueStr,
                    onValueChange = { totalDueStr = it },
                    label = { Text("মোট পাওনা (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("লোকেশন *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("বর্তমান কন্ডিশন:", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TractorCondition.entries.forEach { cond ->
                    FilterChip(
                        selected = condition == cond,
                        onClick = { condition = cond },
                        label = { Text(cond.labelBn) }
                    )
                }
            }

            OutlinedTextField(
                value = conditionNote,
                onValueChange = { conditionNote = it },
                label = { Text("কন্ডিশনের বিস্তারিত বিবরণ *") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = featuresText,
                onValueChange = { featuresText = it },
                label = { Text("ফিচারসমূহ (কমা দিয়ে আলাদা করুন)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Text("ছবি ও ভিডিও টেমপ্লেট:", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                mediaKeys.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedMediaKey == key,
                        onClick = { selectedMediaKey = key },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text("কাস্টম ভিডিও URL (ঐচ্ছিক MP4)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("তালিকার স্ট্যাটাস:", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("available", "reserved", "sold").forEach { st ->
                    FilterChip(
                        selected = status == st,
                        onClick = { status = st },
                        label = { Text(st) }
                    )
                }
            }

            if (!errorMsg.isNullOrBlank()) {
                Text(
                    text = errorMsg ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    fun normalizeDigits(raw: String): String = buildString {
                        for (ch in raw.trim()) {
                            if (ch in '০'..'৯') append(('0'.code + (ch.code - '০'.code)).toChar())
                            else if (ch != ',' && ch != ' ') append(ch)
                        }
                    }
                    val yr = normalizeDigits(yearStr).toLongOrNull()
                    val orig = normalizeDigits(originalPriceStr).toLongOrNull()
                    val sell = normalizeDigits(sellingPriceStr).toLongOrNull()
                    val hrs = normalizeDigits(usedHoursStr).toLongOrNull()
                    val due = normalizeDigits(totalDueStr).toLongOrNull()
                    when {
                        title.trim().length < 2 -> errorMsg = "সঠিক শিরোনাম দিন"
                        model.trim().isEmpty() -> errorMsg = "মডেল লিখুন"
                        yr == null || orig == null || sell == null || hrs == null || due == null ->
                            errorMsg = "সকল সংখ্যা সঠিকভাবে লিখুন"
                        conditionNote.trim().isEmpty() -> errorMsg = "কন্ডিশনের বিবরণ লিখুন"
                        location.trim().isEmpty() -> errorMsg = "লোকেশন লিখুন"
                        else -> {
                            val feats = featuresText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            onSave(
                                brand,
                                model,
                                title,
                                yr,
                                orig,
                                hrs,
                                due,
                                sell,
                                condition,
                                conditionNote,
                                feats,
                                location,
                                selectedMediaKey,
                                selectedMediaKey,
                                videoUrl.ifBlank { null },
                                status
                            )
                        }
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
