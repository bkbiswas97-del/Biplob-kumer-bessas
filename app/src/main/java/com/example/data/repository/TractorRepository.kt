package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.CatalogSeedData
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.OrderStatus
import com.example.data.model.Tractor
import com.example.data.model.TractorCondition
import com.example.data.model.TractorOrder
import com.example.data.model.TractorReview
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class TractorRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        const val BOOTSTRAP_ADMIN_EMAIL = "bkbiswas97@gmail.com"
    }

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Firestore ব্যবহারের আগে Google দিয়ে সাইন ইন করুন।")

    suspend fun checkAdminStatus(user: FirebaseUser): Boolean {
        if (user.isEmailVerified && user.email.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true)) {
            return true
        }
        return try {
            val snapshot = db.collection("admins").document(user.uid).get().await()
            snapshot.exists()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    suspend fun ensureUserProfile(user: FirebaseUser): Result<Unit> {
        val path = "users/${user.uid}"
        return try {
            val docRef = db.collection("users").document(user.uid)
            val existing = docRef.get().await()
            val displayName = (user.displayName?.takeIf { it.isNotBlank() } ?: "ব্যবহারকারী").take(60)
            val email = (user.email?.takeIf { it.isNotBlank() } ?: "user@tractorbazar.app").take(120)
            val photoUrl = user.photoUrl?.toString()?.take(500)

            if (!existing.exists()) {
                val payload = mapOf(
                    "userId" to user.uid,
                    "displayName" to displayName,
                    "email" to email,
                    "photoUrl" to photoUrl,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "displayName" to displayName,
                    "email" to email,
                    "photoUrl" to photoUrl,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeAvailableTractors(): Flow<List<Tractor>> = flow {
        val uid = requireUserId()
        val path = "tractors"
        emitAll(
            db.collection(path)
                .whereEqualTo("status", "available")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Tractor::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeAllTractorsForAdmin(): Flow<List<Tractor>> = flow {
        val uid = requireUserId()
        val path = "tractors"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Tractor::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeReviewsForAvailableTractors(): Flow<List<TractorReview>> = flow {
        val uid = requireUserId()
        val path = "reviews"
        emitAll(
            db.collection(path)
                .whereEqualTo("tractorStatus", "available")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TractorReview::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeMyOrders(userId: String): Flow<List<TractorOrder>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch." }
        val path = "orders"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TractorOrder::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeAllOrdersForAdmin(): Flow<List<TractorOrder>> = flow {
        val uid = requireUserId()
        val path = "orders"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TractorOrder::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun seedInitialTractorsIfEmpty(isAdmin: Boolean): Result<Int> {
        if (!isAdmin) return Result.success(0)
        return try {
            val uid = requireUserId()
            val existing = db.collection("tractors").limit(1).get().await()
            if (!existing.isEmpty) return Result.success(0)

            var seededCount = 0
            val seeds = CatalogSeedData.buildInitialSeedTractors(uid)
            for ((docId, tractor) in seeds) {
                val payload = mapOf(
                    "brand" to tractor.brand,
                    "model" to tractor.model,
                    "title" to tractor.title,
                    "year" to tractor.year,
                    "originalPrice" to tractor.originalPrice,
                    "usedHours" to tractor.usedHours,
                    "totalDue" to tractor.totalDue,
                    "sellingPrice" to tractor.sellingPrice,
                    "condition" to tractor.condition,
                    "conditionNote" to tractor.conditionNote,
                    "features" to tractor.features.take(12),
                    "location" to tractor.location,
                    "imageKey" to tractor.imageKey,
                    "videoKey" to tractor.videoKey,
                    "status" to tractor.status,
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("tractors").document(docId).set(payload, SetOptions.merge()).await()
                seededCount++
            }
            Result.success(seededCount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "tractors")
            Result.failure(e)
        }
    }

    suspend fun submitOrder(
        tractor: Tractor,
        customerName: String,
        address: String,
        phone: String,
        note: String?
    ): Result<String> {
        var docPath = "orders"
        return try {
            val uid = requireUserId()
            val cleanName = customerName.trim()
            val cleanAddress = address.trim()
            val cleanPhone = phone.trim()
            val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }

            require(tractor.id.isNotBlank()) { "ট্রাক্টর আইডি সঠিক নয়।" }
            require(cleanName.length in 2..80) { "নাম কমপক্ষে ২ অক্ষরের হতে হবে।" }
            require(cleanAddress.length in 5..250) { "সম্পূর্ণ ঠিকানা কমপক্ষে ৫ অক্ষরের হতে হবে।" }
            require(cleanPhone.length in 6..20) { "সঠিক মোবাইল নম্বর লিখুন (৬-২০ অঙ্ক)।" }
            if (cleanNote != null) {
                require(cleanNote.length <= 300) { "মন্তব্য ৩০০ অক্ষরের মধ্যে রাখুন।" }
            }

            val docRef = db.collection("orders").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "tractorId" to tractor.id,
                "tractorTitle" to tractor.title.take(120),
                "tractorBrand" to tractor.brand.take(50),
                "sellingPrice" to tractor.sellingPrice,
                "customerName" to cleanName,
                "address" to cleanAddress,
                "phone" to cleanPhone,
                "note" to cleanNote,
                "status" to OrderStatus.PENDING.name,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun cancelMyOrder(orderId: String): Result<Unit> {
        val docPath = "orders/$orderId"
        return try {
            requireUserId()
            require(orderId.isNotBlank()) { "Order ID blank." }
            db.collection("orders").document(orderId).update(
                mapOf(
                    "status" to OrderStatus.CANCELLED.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatusAdmin(orderId: String, newStatus: OrderStatus): Result<Unit> {
        val docPath = "orders/$orderId"
        return try {
            requireUserId()
            require(orderId.isNotBlank()) { "Order ID blank." }
            db.collection("orders").document(orderId).update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun saveTractorAdmin(
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
        status: String
    ): Result<Unit> {
        var docPath = "tractors"
        return try {
            val uid = requireUserId()
            val cleanBrand = brand.trim().take(50)
            val cleanModel = model.trim().take(80)
            val cleanTitle = title.trim().take(120)
            val cleanNote = conditionNote.trim().take(300)
            val cleanLocation = location.trim().take(80)
            val cleanFeatures = features.map { it.trim() }.filter { it.isNotEmpty() }.take(12)
            val cleanVideoUrl = videoUrl?.trim()?.takeIf { it.isNotEmpty() }?.take(500)

            require(cleanBrand.isNotEmpty()) { "ব্র্যান্ড লিখুন" }
            require(cleanModel.isNotEmpty()) { "মডেল লিখুন" }
            require(cleanTitle.length >= 2) { "শিরোনাম কমপক্ষে ২ অক্ষরের হতে হবে" }
            require(year in 1990..2030) { "সঠিক বছর দিন (১৯৯০-২০৩০)" }
            require(originalPrice in 0..50_000_000) { "মূল দাম সঠিক নয়" }
            require(sellingPrice in 0..50_000_000) { "বিক্রয় মূল্য সঠিক নয়" }
            require(usedHours in 0..100_000) { "ব্যবহৃত ঘন্টা সঠিক নয়" }
            require(totalDue in 0..50_000_000) { "মোট পাওনা সঠিক নয়" }
            require(cleanNote.isNotEmpty()) { "কন্ডিশনের বিবরণ লিখুন" }
            require(cleanLocation.isNotEmpty()) { "লোকেশন লিখুন" }

            if (tractorId.isNullOrBlank()) {
                val docRef = db.collection("tractors").document()
                docPath = docRef.path
                val payload = mapOf(
                    "brand" to cleanBrand,
                    "model" to cleanModel,
                    "title" to cleanTitle,
                    "year" to year,
                    "originalPrice" to originalPrice,
                    "usedHours" to usedHours,
                    "totalDue" to totalDue,
                    "sellingPrice" to sellingPrice,
                    "condition" to condition.name,
                    "conditionNote" to cleanNote,
                    "features" to cleanFeatures,
                    "location" to cleanLocation,
                    "imageKey" to imageKey,
                    "videoKey" to videoKey,
                    "videoUrl" to cleanVideoUrl,
                    "status" to status,
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val docRef = db.collection("tractors").document(tractorId)
                docPath = docRef.path
                val updates = mutableMapOf<String, Any>(
                    "brand" to cleanBrand,
                    "model" to cleanModel,
                    "title" to cleanTitle,
                    "year" to year,
                    "originalPrice" to originalPrice,
                    "usedHours" to usedHours,
                    "totalDue" to totalDue,
                    "sellingPrice" to sellingPrice,
                    "condition" to condition.name,
                    "conditionNote" to cleanNote,
                    "features" to cleanFeatures,
                    "location" to cleanLocation,
                    "imageKey" to imageKey,
                    "videoKey" to videoKey,
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (cleanVideoUrl != null) {
                    updates["videoUrl"] = cleanVideoUrl
                }
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteTractorAdmin(tractorId: String): Result<Unit> {
        val docPath = "tractors/$tractorId"
        return try {
            requireUserId()
            db.collection("tractors").document(tractorId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun submitOrUpdateReview(
        existingReviewId: String?,
        tractorId: String,
        reviewerName: String,
        rating: Int,
        comment: String
    ): Result<Unit> {
        var docPath = "reviews"
        return try {
            val uid = requireUserId()
            val cleanName = reviewerName.trim().ifEmpty { "ক্রেতা" }.take(60)
            val cleanComment = comment.trim()
            require(rating in 1..5) { "রেটিং ১ থেকে ৫ স্টারের মধ্যে হতে হবে।" }
            require(cleanComment.length in 1..500) { "রিভিউ ১ থেকে ৫০০ অক্ষরের মধ্যে লিখুন।" }
            require(tractorId.isNotBlank()) { "ট্রাক্টর আইডি পাওয়া যায়নি।" }

            if (existingReviewId.isNullOrBlank()) {
                val docRef = db.collection("reviews").document()
                docPath = docRef.path
                val payload = mapOf(
                    "tractorId" to tractorId,
                    "tractorStatus" to "available",
                    "userId" to uid,
                    "reviewerName" to cleanName,
                    "rating" to rating.toLong(),
                    "comment" to cleanComment,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val docRef = db.collection("reviews").document(existingReviewId)
                docPath = docRef.path
                docRef.update(
                    mapOf(
                        "reviewerName" to cleanName,
                        "rating" to rating.toLong(),
                        "comment" to cleanComment,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String): Result<Unit> {
        val docPath = "reviews/$reviewId"
        return try {
            requireUserId()
            db.collection("reviews").document(reviewId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private fun handleFlowError(
        error: Throwable,
        operationType: OperationType,
        path: String,
        expectedUid: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
    }
}
