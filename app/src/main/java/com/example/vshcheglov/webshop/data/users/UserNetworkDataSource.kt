package com.example.vshcheglov.webshop.data.users

import android.graphics.Bitmap
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.entities.OrderResponse
import com.example.vshcheglov.webshop.data.entities.UserResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.graphics.Bitmap.CompressFormat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class UserNetworkDataSource {

    companion object {
        const val MAX_PHOTO_SIZE = 1024 * 1024 * 20L
    }

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore
    @Inject
    lateinit var firestoreStorage: FirebaseStorage

    val isSignedIn: Boolean
        get() = firebaseAuth.currentUser != null

    init {
        App.appComponent.inject(this)
    }

    suspend fun registerUser(email: String, password: String) =
        suspendCancellableCoroutine<Unit> { continuation ->
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(Unit)
                    saveNewUserToDb()
                } else {
                    continuation.resumeWithException(task.exception!!)
                }
            }
    }

    private fun saveNewUserToDb() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).set(UserResponse(user.email, user.uid))
            Timber.d("Added uid to Firestore")
        } else {
            Timber.e("UserResponse id saving error")
        }
    }

    suspend fun signInUser(email: String, password: String) = suspendCancellableCoroutine<Unit> { continuation ->
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(task.exception!!)
                }
            }
    }

    suspend fun getCurrentUser() = suspendCancellableCoroutine<UserResponse> { continuation ->
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document -> onGetUserSuccess(document, continuation) }
                .addOnFailureListener {
                    onSaveOrderError(it, continuation, "User fetching error")
                }
        } else {
            onSaveOrderError(Exception("User fetching error"), continuation, "")
        }
    }

    private fun onGetUserSuccess(document: DocumentSnapshot?, continuation: CancellableContinuation<UserResponse>) {
        val user = document?.toObject(UserResponse::class.java)
        if (user == null) {
            continuation.resumeWithException(Exception("User parse error"))
        } else {
            continuation.resume(user)
        }
    }

    suspend fun saveOrder(order: OrderResponse) = suspendCancellableCoroutine<Unit> { continuation ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            val ordersReference = firestore.collection("users")
                .document(user.uid)
                .collection("orders")
                .document()

            order.id = ordersReference.id

            for (orderProduct in order.orderProducts) {
                orderProduct.id = order.id + "$" + orderProduct.productId//Custom id for each product
            }

            ordersReference.set(order)
                .addOnSuccessListener { onSaveOrderSuccess(continuation) }
                .addOnFailureListener { onSaveOrderError(it, continuation, "Order saving error") }
        } else {
            onSaveOrderError(Exception("User is not authorized"), continuation, "")
        }
    }

    private fun onSaveOrderSuccess(continuation: CancellableContinuation<Unit>) {
        Timber.d("Order saved successfully")
        continuation.resume(Unit)
    }

    private fun onSaveOrderError(
        throwable: Throwable, continuation: CancellableContinuation<*>,
        exceptionMessage: String
    ) {
        if (continuation.isCancelled || !continuation.isActive) {
            continuation.resumeWithException(CancellationException())
        } else {
            Timber.d("$exceptionMessage: $throwable")
            continuation.resumeWithException(throwable)
        }
    }

    fun logOut() {
        firebaseAuth.signOut()
    }

    suspend fun getUserOrders() = suspendCancellableCoroutine<MutableList<OrderResponse>> { continuation ->
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            firestore.collection("users/${currentUser.uid}/orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { document ->
                    onGetUserOrdersSuccess(continuation, document)
                }
                .addOnFailureListener {
                    onSaveOrderError(it, continuation, "Order fetching error")
                }
        } else {
            onSaveOrderError(Exception("User is not authorized"), continuation, "")
        }
    }

    private fun onGetUserOrdersSuccess(
        continuation: CancellableContinuation<MutableList<OrderResponse>>,
        document: QuerySnapshot
    ) {

        val order = document.toObjects(OrderResponse::class.java)
        continuation.resume(order)
    }

    fun saveUserProfilePhoto(profilePhotoBitmap: Bitmap, name: String) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val byteOutputStream = ByteArrayOutputStream()
            profilePhotoBitmap.compress(CompressFormat.JPEG, 40, byteOutputStream)
            val bitmapData = byteOutputStream.toByteArray()
            val byteInputStream = ByteArrayInputStream(bitmapData)

            val avatarStorageReference = "userImages/$name"
            val avatarMap = mapOf(
                Pair("avatarStorageReference", avatarStorageReference),
                Pair("timestamp", Timestamp.now())
            )

            firestoreStorage.getReference(avatarStorageReference).putStream(byteInputStream)
            firestore.collection("users/${currentUser.uid}/avatars").add(avatarMap)
        } else {
            throw Exception("User not authorized.")
        }
    }

    suspend fun getUserAvatarByteArray(): ByteArray {
        val avatarReference = getUserAvatarReference()
        return getUserAvatarByteArray(avatarReference)
    }

    private suspend fun getUserAvatarByteArray(avatarReference: String) =
        suspendCancellableCoroutine<ByteArray> { continuation ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val reference = firestoreStorage.getReference(avatarReference)
                reference.getBytes(MAX_PHOTO_SIZE)
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }
                    .addOnFailureListener {
                        onAvatarLoadError(continuation = continuation)
                    }
            } else {
                throw Exception("User not authorized.")
            }
        }

    private fun onAvatarLoadError(
        throwable: Throwable = java.lang.Exception("Avatar load error"),
        continuation: CancellableContinuation<ByteArray>,
        exceptionMessage: String = "Avatar load error"
    ) {
        if (continuation.isCancelled || !continuation.isActive) {
            continuation.resumeWithException(CancellationException())
        } else {
            Timber.d("$exceptionMessage: $throwable")
            continuation.resumeWithException(throwable)
        }
    }

    private suspend fun getUserAvatarReference() = suspendCancellableCoroutine<String> { continuation ->
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            firestore.collection("users/${currentUser.uid}/avatars")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        onAvatarReferenceLoadError(
                            Exception("User not authorized."),
                            continuation, "AvatarLoadError"
                        )
                    } else {
                        for (document in documents) {
                            onAvatarMapLoadSuccess(continuation, document)
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    onAvatarReferenceLoadError(it, continuation, "AvatarLoadError")
                }
        } else {
            throw Exception("User not authorized.")
        }
    }

    private fun onAvatarReferenceLoadError(
        throwable: Throwable = java.lang.Exception("Avatar load error"),
        continuation: CancellableContinuation<String>,
        exceptionMessage: String = "Avatar load error"
    ) {
        if (continuation.isCancelled || !continuation.isActive) {
            continuation.resumeWithException(CancellationException())
        } else {
            Timber.d("$exceptionMessage: $throwable")
            continuation.resumeWithException(throwable)
        }
    }

    private fun onAvatarMapLoadSuccess(continuation: CancellableContinuation<String>, document: QueryDocumentSnapshot) {
        val avatarMap = document.data
        avatarMap["avatarStorageReference"]?.let {
            continuation.resume(it as String)
        } ?: onAvatarReferenceLoadError(continuation = continuation)
    }
}