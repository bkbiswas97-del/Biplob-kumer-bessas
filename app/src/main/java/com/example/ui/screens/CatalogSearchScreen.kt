package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CatalogSeedData
import com.example.data.model.Tractor
import com.example.data.model.TractorCondition
import com.example.data.model.TractorFilterState
import com.example.data.model.TractorRatingSummary
import com.example.data.model.TractorSortOption
import com.example.data.model.UiState
import com.example.ui.components.TractorListingCard
import com.example.ui.components.formatIndianNumber
import com.example.ui.components.formatTaka
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogSearchScreen(
    filteredTractorsState: UiState<List<Tractor>>,
    filterState: TractorFilterState,
    ratingSummaries: Map<String, TractorRatingSummary>,
    onSearchQueryChange: (String) -> Unit,
    onSelectBrand: (String?) -> Unit,
    onModelQueryChange: (String) -> Unit,
    onPriceRangeChange: (Long, Long) -> Unit,
    onMaxHoursChange: (Long) -> Unit,
    onToggleCondition: (TractorCondition) -> Unit,
    onSelectSort: (TractorSortOption) -> Unit,
    onResetFilters: () -> Unit,
    onTractorClick: (Tractor) -> Unit,
    onOrderClick: (Tractor) -> Unit
) {
    var filtersExpanded by remember { mutableStateOf(true) }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // Search + Filter Toggle Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = filterState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("ব্র্যান্ড, মডেল বা জেলা লিখুন...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "অনুসন্ধান"
                                )
                            },
                            trailingIcon = {
                                if (filterState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "মুছুন"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("catalog_search_input")
                        )

                        BadgedBox(
                            badge = {
                                if (filterState.activeFilterCount > 0) {
                                    Badge {
                                        Text(filterState.activeFilterCount.toString())
                                    }
                                }
                            }
                        ) {
                            OutlinedButton(
                                onClick = { filtersExpanded = !filtersExpanded },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("toggle_filters_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = "ফিল্টার"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ফিল্টার")
                            }
                        }
                    }

                    // Advanced Filters Panel
                    AnimatedVisibility(visible = filtersExpanded) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "উন্নত সার্চ ও ফিল্টার",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(
                                        onClick = onResetFilters,
                                        modifier = Modifier.testTag("reset_filters_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = "রিসেট",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("সব রিসেট")
                                    }
                                }

                                // 1. Brand chips
                                Text(
                                    text = "ব্র্যান্ড নির্বাচন করুন",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item {
                                        FilterChip(
                                            selected = filterState.selectedBrand == null,
                                            onClick = { onSelectBrand(null) },
                                            label = { Text("সব ব্র্যান্ড") }
                                        )
                                    }
                                    items(CatalogSeedData.brandOptions) { brand ->
                                        FilterChip(
                                            selected = filterState.selectedBrand == brand,
                                            onClick = {
                                                onSelectBrand(if (filterState.selectedBrand == brand) null else brand)
                                            },
                                            label = { Text(brand) },
                                            modifier = Modifier.testTag("filter_brand_$brand")
                                        )
                                    }
                                }

                                // 2. Model filter
                                OutlinedTextField(
                                    value = filterState.modelQuery,
                                    onValueChange = onModelQueryChange,
                                    label = { Text("নির্দিষ্ট মডেল (যেমন: 575 DI, 744 FE)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("filter_model_input")
                                )

                                // 3. Price range slider
                                Column {
                                    Text(
                                        text = "বিক্রয় মূল্যের সীমা: ${formatTaka(filterState.minPrice)} - ${formatTaka(filterState.maxPrice)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    RangeSlider(
                                        value = filterState.minPrice.toFloat()..filterState.maxPrice.toFloat(),
                                        onValueChange = { range ->
                                            val minVal = (range.start / 25_000f).roundToLong() * 25_000L
                                            val maxVal = (range.endInclusive / 25_000f).roundToLong() * 25_000L
                                            onPriceRangeChange(minVal, maxVal)
                                        },
                                        valueRange = 200_000f..1_500_000f,
                                        modifier = Modifier.testTag("filter_price_slider")
                                    )
                                }

                                // 4. Usage duration (used hours)
                                Column {
                                    Text(
                                        text = "ব্যবহারের সময়কাল (সর্বোচ্চ): ${formatIndianNumber(filterState.maxUsedHours)} ঘন্টা",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Slider(
                                        value = filterState.maxUsedHours.toFloat(),
                                        onValueChange = { value ->
                                            val rounded = (value / 100f).roundToLong() * 100L
                                            onMaxHoursChange(rounded)
                                        },
                                        valueRange = 500f..5_000f,
                                        modifier = Modifier.testTag("filter_hours_slider")
                                    )
                                }

                                // 5. Condition filter
                                Text(
                                    text = "বর্তমান অবস্থা (কন্ডিশন)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    TractorCondition.entries.forEach { condition ->
                                        FilterChip(
                                            selected = condition in filterState.selectedConditions,
                                            onClick = { onToggleCondition(condition) },
                                            label = { Text(condition.labelBn) },
                                            modifier = Modifier.testTag("filter_condition_${condition.name}")
                                        )
                                    }
                                }

                                // 6. Sort order
                                Text(
                                    text = "তালিকা সাজান",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(TractorSortOption.entries) { option ->
                                        FilterChip(
                                            selected = filterState.sortOption == option,
                                            onClick = { onSelectSort(option) },
                                            label = { Text(option.labelBn) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Results List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when (filteredTractorsState) {
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
                                text = filteredTractorsState.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is UiState.Success -> {
                        item {
                            Text(
                                text = "মোট ${formatIndianNumber(filteredTractorsState.data.size.toLong())}টি ট্রাক্টর পাওয়া গেছে",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (filteredTractorsState.data.isEmpty()) {
                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SearchOff,
                                        contentDescription = "পাওয়া যায়নি",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "আপনার ফিল্টার অনুযায়ী কোনো ট্রাক্টর মেলেনি",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    OutlinedButton(onClick = onResetFilters) {
                                        Text("ফিল্টার রিসেট করুন")
                                    }
                                }
                            }
                        } else {
                            items(filteredTractorsState.data, key = { it.id }) { tractor ->
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
}
