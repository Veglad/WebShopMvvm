package com.example.vshcheglov.webshop.presentation.main

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.helpers.ImagePickHelper
import com.example.vshcheglov.webshop.presentation.helpres.EventWithContent
import com.example.vshcheglov.webshop.presentation.main.helpers.SearchFilter
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import com.example.vshcheglov.webshop.presentation.main.helpers.AvatarWorker
import com.example.vshcheglov.webshop.presentation.main.helpers.AvatarWorkerFactory


class MainViewModel(application: Application) : AndroidViewModel(application) {
    @Inject
    lateinit var dataProvider: DataProvider
    @Inject
    lateinit var avatarWorkerFactory: AvatarWorkerFactory
    @Inject
    lateinit var imagePicker: ImagePickHelper

    private val job: Job = Job()
    private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val _stateLiveData: MutableLiveData<MainViewState> = MutableLiveData<MainViewState>().apply {
        value = MainViewState()
    }
    val stateLiveData: LiveData<MainViewState> = _stateLiveData

    private val _commandLiveData: MutableLiveData<EventWithContent<MainCommand>> = MutableLiveData()
    val commandLiveData: LiveData<EventWithContent<MainCommand>> = _commandLiveData

    private fun getState() = stateLiveData.value!!

    init {
        App.appComponent.inject(this)
    }

    fun loadProducts(isNetworkAvailable: Boolean) {
        if (getState().productList.isEmpty() || getState().promotionalProductList.isEmpty()) {
            forceLoadProducts(isNetworkAvailable)
        }
    }

    fun forceLoadProducts(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            fetchProducts()
        } else {
            setCommand(ShowNoInternet)
            _stateLiveData.value = getState().copy(isLoading = false)
        }
    }

    private fun fetchProducts() {
        uiCoroutineScope.launch {
            try {
                _stateLiveData.value = getState().copy(isLoading = true)

                val productsDeferred = uiCoroutineScope.async { dataProvider.getProducts() }
                val promotionalProductsDeferred = uiCoroutineScope.async { dataProvider.getPromotionalProducts() }
                val productList = productsDeferred.await()
                val promotionalProductList = promotionalProductsDeferred.await()

                _stateLiveData.value =
                    getState().copy(productList = productList, promotionalProductList = promotionalProductList)

            } catch (ex: Exception) {
                Timber.e("Products fetching error:$ex")
                setCommand(ShowError(ex))
            } finally {
                _stateLiveData.value = getState().copy(isLoading = false)
            }
        }
    }

    fun loadUserEmail(isNetworkAvailable: Boolean) {
        if (!isNetworkAvailable) return
        if (getState().userEmail == null) {
            uiCoroutineScope.launch {
                try {
                    val user = withContext(Dispatchers.IO) { dataProvider.getCurrentUser() }
                    _stateLiveData.value = getState().copy(userEmail = user.email)
                } catch (ex: Exception) {
                    _stateLiveData.value = getState().copy(userEmail = null)
                }
            }
        }
    }

    fun loadUserAvatar(isNetworkAvailable: Boolean) {
        if (!isNetworkAvailable) return
        if (getState().avatarImage == null) {
            uiCoroutineScope.launch {
                try {
                    val avatarByteArray = withContext(Dispatchers.IO) { dataProvider.getUserAvatarByteArray() }
                    val avatarBitmap = withContext(Dispatchers.Default) {
                        BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.size)
                    }
                    _stateLiveData.value = getState().copy(avatarImage = avatarBitmap)
                } catch (ex: Exception) {
                    _stateLiveData.value = getState().copy(avatarImage = null)
                }
            }
        }
    }

    fun logOut() {
        dataProvider.logOut()
        setCommand(StartLoginScreen)
    }

    fun searchProducts(searchText: String) {
        val searchFilter = SearchFilter(getState().productList) { productList: List<Product>? ->
            if (productList == null || productList.isEmpty()) {
                _stateLiveData.value = getState().copy(searchProductList = listOf())
            } else {
                _stateLiveData.value = getState().copy(searchProductList = productList)
            }
        }
        searchFilter.filter.filter(searchText)

    }

    private fun saveUserAvatar(imagePath: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString(AvatarWorker.KEY_AVATAR_WORKER_IMAGE_PATH, imagePath)
            .build()

        val avatarRequest = OneTimeWorkRequestBuilder<AvatarWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(getApplication())
            .enqueue(avatarRequest)
    }

    fun buyProduct(product: Product) {
        Basket.addProduct(product)
    }

    private fun setCommand(command: MainCommand) {
        _commandLiveData.value = EventWithContent(command)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    fun setImage(imageUri: Uri?, isFromCamera: Boolean) {
        val imageBitmap = imagePicker.getImageBitmap(imageUri, isFromCamera)
        _stateLiveData.value = getState().copy(avatarImage = imageBitmap)

        uiCoroutineScope.launch {
            withContext(Dispatchers.Default) {
                imagePicker.saveImageToInternalStorage(imageUri, isFromCamera)
                imagePicker.imagePath?.let { imagePath ->
                    saveUserAvatar(imagePath)
                }
            }
        }
    }

    fun pickUserImage() {
        val cameraIntent = imagePicker.getCaptureIntent()
        val galleryIntent = imagePicker.getPickImageIntent()
        setCommand(StartImagePicking(cameraIntent, galleryIntent))
    }
}

data class MainViewState(
    var isLoading: Boolean = false,
    var productList: MutableList<Product> = mutableListOf(),
    var promotionalProductList: MutableList<Product> = mutableListOf(),
    var searchProductList: List<Product> = listOf(),
    var avatarImage: Bitmap? = null,
    var userEmail: String? = null
)

sealed class MainCommand
class ShowError(val exception: Exception) : MainCommand()
class StartImagePicking(val cameraIntent: Intent?, val galleryIntent: Intent) : MainCommand()
object ShowNoInternet : MainCommand()
object StartLoginScreen : MainCommand()
