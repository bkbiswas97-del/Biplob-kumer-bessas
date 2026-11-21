package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.OrderStatus
import com.example.data.model.Tractor
import com.example.data.model.TractorCondition
import com.example.data.model.TractorFilterState
import com.example.data.model.TractorOrder
import com.example.data.model.TractorRatingSummary
import com.example.data.model.TractorReview
import com.example.data.model.TractorSortOption
import com.example.data.model.UiState
import com.example.data.repository.TractorRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceViewModel(
    private val repository: TractorRepository,
    private val currentUserId: String,
    private val currentUser: FirebaseUser?
) : ViewModel() {

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _filterState = MutableStateFlow(TractorFilterState())
    val filterState: StateFlow<TractorFilterState> = _filterState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            if (currentUser != null) {
                repository.ensureUserProfile(currentUser)
                val admin = repository.checkAdminStatus(currentUser)
                _isAdmin.value = admin
                if (admin) {
                    repository.seedInitialTractorsIfEmpty(isAdmin = true)
                }
            }
        }
    }

    private fun <T> Flow<List<T>>.toUiState(tag: String, fallbackMsg: String): StateFlow<UiState<List<T>>> =
        this.map<List<T>, UiState<List<T>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "$tag error", rootError)
                emit(UiState.Error(rootError.message ?: fallbackMsg))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val availableTractorsState: StateFlow<UiState<List<Tractor>>> =
        repository.observeAvailableTractors()
            .toUiState("availableTractors", "ট্রাক্টর তালিকা লোড করা যায়নি")

    val reviewsState: StateFlow<UiState<List<TractorReview>>> =
        repository.observeReviewsForAvailableTractors()
            .toUiState("reviews", "রিভিউ লোড করা যায়নি")

    val myOrdersState: StateFlow<UiState<List<TractorOrder>>> =
        repository.observeMyOrders(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
            .toUiState("myOrders", "আপনার অর্ডার লোড করা যায়নি")

    // Admin streams are strictly gated behind _isAdmin == true to avoid unauthorized queries
    val adminTractorsState: StateFlow<UiState<List<Tractor>>> =
        _isAdmin.flatMapLatest { admin ->
            if (!admin) flowOf(emptyList())
            else repository.observeAllTractorsForAdmin()
                .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
        }.toUiState("adminTractors", "এডমিন ট্রাক্টর তালিকা লোড করা যায়নি")

    val adminOrdersState: StateFlow<UiState<List<TractorOrder>>> =
        _isAdmin.flatMapLatest { admin ->
            if (!admin) flowOf(emptyList())
            else repository.observeAllOrdersForAdmin()
                .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
        }.toUiState("adminOrders", "এডমিন অর্ডার তালিকা লোড করা যায়নি")

    val ratingSummaries: StateFlow<Map<String, TractorRatingSummary>> =
        reviewsState.map { state ->
            when (state) {
                is UiState.Success -> {
                    state.data.groupBy { it.tractorId }.mapValues { (_, reviews) ->
                        val avg = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
                        TractorRatingSummary(avgRating = avg, reviewCount = reviews.size)
                    }
                }
                else -> emptyMap()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyMap()
        )

    val filteredTractorsState: StateFlow<UiState<List<Tractor>>> =
        combine(availableTractorsState, _filterState, ratingSummaries) { tractorsUi, filter, ratings ->
            when (tractorsUi) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> tractorsUi
                is UiState.Success -> {
                    val filtered = tractorsUi.data.filter { tractor ->
                        val matchesQuery = filter.searchQuery.isBlank() ||
                            tractor.title.contains(filter.searchQuery, ignoreCase = true) ||
                            tractor.brand.contains(filter.searchQuery, ignoreCase = true) ||
                            tractor.model.contains(filter.searchQuery, ignoreCase = true) ||
                            tractor.location.contains(filter.searchQuery, ignoreCase = true)

                        val matchesBrand = filter.selectedBrand == null ||
                            tractor.brand.equals(filter.selectedBrand, ignoreCase = true)

                        val matchesModel = filter.modelQuery.isBlank() ||
                            tractor.model.contains(filter.modelQuery, ignoreCase = true) ||
                            tractor.title.contains(filter.modelQuery, ignoreCase = true)

                        val matchesPrice = tractor.sellingPrice in filter.minPrice..filter.maxPrice

                        val matchesHours = tractor.usedHours <= filter.maxUsedHours

                        val matchesCondition = filter.selectedConditions.isEmpty() ||
                            tractor.conditionEnum in filter.selectedConditions

                        matchesQuery && matchesBrand && matchesModel && matchesPrice && matchesHours && matchesCondition
                    }

                    val sorted = when (filter.sortOption) {
                        TractorSortOption.NEWEST ->
                            filtered.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                        TractorSortOption.PRICE_LOW_TO_HIGH ->
                            filtered.sortedBy { it.sellingPrice }
                        TractorSortOption.PRICE_HIGH_TO_LOW ->
                            filtered.sortedByDescending { it.sellingPrice }
                        TractorSortOption.HOURS_LOW_TO_HIGH ->
                            filtered.sortedBy { it.usedHours }
                        TractorSortOption.TOP_RATED ->
                            filtered.sortedByDescending { ratings[it.id]?.avgRating ?: 0.0 }
                    }
                    UiState.Success(sorted)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun selectBrand(brand: String?) {
        _filterState.update { it.copy(selectedBrand = brand) }
    }

    fun updateModelQuery(model: String) {
        _filterState.update { it.copy(modelQuery = model) }
    }

    fun updatePriceRange(minPrice: Long, maxPrice: Long) {
        _filterState.update { it.copy(minPrice = minPrice, maxPrice = maxPrice) }
    }

    fun updateMaxUsedHours(maxHours: Long) {
        _filterState.update { it.copy(maxUsedHours = maxHours) }
    }

    fun toggleConditionFilter(condition: TractorCondition) {
        _filterState.update { state ->
            val updated = if (condition in state.selectedConditions) {
                state.selectedConditions - condition
            } else {
                state.selectedConditions + condition
            }
            state.copy(selectedConditions = updated)
        }
    }

    fun updateSortOption(option: TractorSortOption) {
        _filterState.update { it.copy(sortOption = option) }
    }

    fun resetFilters() {
        _filterState.value = TractorFilterState()
    }

    fun submitOrder(
        tractor: Tractor,
        customerName: String,
        address: String,
        phone: String,
        note: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.submitOrder(tractor, customerName, address, phone, note)
            _isSubmitting.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("অর্ডার সফলভাবে সাবমিট হয়েছে! এডমিন প্যানেলে পাঠানো হয়েছে।")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "অর্ডার সাবমিট করা যায়নি")
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.cancelMyOrder(orderId)
            _isSubmitting.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("অর্ডার বাতিল করা হয়েছে।")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "অর্ডার বাতিল করা যায়নি")
            }
        }
    }

    fun updateOrderStatusAdmin(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.updateOrderStatusAdmin(orderId, status)
            _isSubmitting.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("অর্ডার স্ট্যাটাস আপডেট হয়েছে: ${status.labelBn}")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "স্ট্যাটাস আপডেট করা যায়নি")
            }
        }
    }

    fun saveTractorAdmin(
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
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.saveTractorAdmin(
                tractorId = tractorId,
                brand = brand,
                model = model,
                title = title,
                year = year,
                originalPrice = originalPrice,
                usedHours = usedHours,
                totalDue = totalDue,
                sellingPrice = sellingPrice,
                condition = condition,
                conditionNote = conditionNote,
                features = features,
                location = location,
                imageKey = imageKey,
                videoKey = videoKey,
                videoUrl = videoUrl,
                status = status
            )
            _isSubmitting.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit(
                    if (tractorId == null) "নতুন ট্রাক্টর যুক্ত হয়েছে!" else "ট্রাক্টরের তথ্য আপডেট হয়েছে!"
                )
                onSuccess()
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "ট্রাক্টর সংরক্ষণ করা যায়নি")
            }
        }
    }

    fun deleteTractorAdmin(tractorId: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.deleteTractorAdmin(tractorId)
            _isSubmitting.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("ট্রাক্টরটি তালিকা থেকে মুছে ফেলা হয়েছে।")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "মুছে ফেলা যায়নি")
            }
        }
    }

    fun seedDemoTractors() {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.seedInitialTractorsIfEmpty(_isAdmin.value)
            _isSubmitting.value = false
            result.onSuccess { count ->
                if (count > 0) {
                    _snackbarEvents.tryEmit("৬টি ট্রাক্টর সফলভাবে যোগ করা হয়েছে!")
                } else {
                    _snackbarEvents.tryEmit("ইতিমধ্যে ট্রাক্টর তালিকাভুক্ত রয়েছে।")
                }
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "সিড করা যায়নি")
            }
        }
    }

    fun submitReview(
        existingReviewId: String?,
        tractorId: String,
        rating: Int,
        comment: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val reviewerName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "ক্রেতা"
            val result = repository.submitOrUpdateReview(
                existingReviewId = existingReviewId,
                tractorId = tractorId,
                reviewerName = reviewerName,
                rating = rating,
                comment = comment
            )
            _isSubmitting.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("আপনার রিভিউ ও রেটিং সংরক্ষিত হয়েছে!")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "রিভিউ সংরক্ষণ করা যায়নি")
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.deleteReview(reviewId)
            _isSubmitting.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("রিভিউ মুছে ফেলা হয়েছে।")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "রিভিউ মুছে ফেলা যায়নি")
            }
        }
    }

    private companion object {
        const val TAG = "MarketplaceVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
