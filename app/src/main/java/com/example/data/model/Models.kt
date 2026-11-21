package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TractorCondition(val labelBn: String) {
    EXCELLENT("চমৎকার কন্ডিশন"),
    GOOD("ভালো কন্ডিশন"),
    FAIR("চলনসই কন্ডিশন"),
    NEEDS_SERVICE("সার্ভিসিং প্রয়োজন");

    companion object {
        fun fromRaw(raw: String?): TractorCondition =
            entries.firstOrNull { it.name == raw } ?: GOOD
    }
}

enum class OrderStatus(val labelBn: String) {
    PENDING("অপেক্ষমাণ"),
    CONFIRMED("নিশ্চিতকৃত"),
    DELIVERED("ডেলিভারি সম্পন্ন"),
    CANCELLED("বাতিল");

    companion object {
        fun fromRaw(raw: String?): OrderStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class TractorSortOption(val labelBn: String) {
    NEWEST("নতুন তালিকা"),
    PRICE_LOW_TO_HIGH("কম দাম আগে"),
    PRICE_HIGH_TO_LOW("বেশি দাম আগে"),
    HOURS_LOW_TO_HIGH("কম ব্যবহৃত ঘন্টা"),
    TOP_RATED("সেরা রেটিং")
}

@Keep
@IgnoreExtraProperties
data class Tractor(
    @DocumentId val id: String = "",
    val brand: String = "",
    val model: String = "",
    val title: String = "",
    val year: Long = 2020L,
    val originalPrice: Long = 0L,
    val usedHours: Long = 0L,
    val totalDue: Long = 0L,
    val sellingPrice: Long = 0L,
    val condition: String = TractorCondition.GOOD.name,
    val conditionNote: String = "",
    val features: List<String> = emptyList(),
    val location: String = "",
    val imageKey: String = "mahindra_575",
    val videoKey: String = "mahindra_575",
    val videoUrl: String? = null,
    val status: String = "available",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val conditionEnum: TractorCondition
        get() = TractorCondition.fromRaw(condition)
}

@Keep
@IgnoreExtraProperties
data class TractorOrder(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tractorId: String = "",
    val tractorTitle: String = "",
    val tractorBrand: String = "",
    val sellingPrice: Long = 0L,
    val customerName: String = "",
    val address: String = "",
    val phone: String = "",
    val note: String? = null,
    val status: String = OrderStatus.PENDING.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: OrderStatus
        get() = OrderStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class TractorReview(
    @DocumentId val id: String = "",
    val tractorId: String = "",
    val tractorStatus: String = "available",
    val userId: String = "",
    val reviewerName: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class TractorRatingSummary(
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0
)

data class TractorFilterState(
    val searchQuery: String = "",
    val selectedBrand: String? = null,
    val modelQuery: String = "",
    val minPrice: Long = 200_000L,
    val maxPrice: Long = 1_500_000L,
    val maxUsedHours: Long = 5_000L,
    val selectedConditions: Set<TractorCondition> = emptySet(),
    val sortOption: TractorSortOption = TractorSortOption.NEWEST
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedBrand != null) count++
            if (modelQuery.isNotBlank()) count++
            if (minPrice > 200_000L || maxPrice < 1_500_000L) count++
            if (maxUsedHours < 5_000L) count++
            if (selectedConditions.isNotEmpty()) count++
            return count
        }
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
