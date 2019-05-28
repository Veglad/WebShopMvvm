package com.example.vshcheglov.webshop.presentation.main

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.helpres.Event
import com.example.vshcheglov.webshop.presentation.helpres.EventWithContent
import com.example.vshcheglov.webshop.presentation.main.helpers.SearchFilter
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MainViewModel : ViewModel() {
    @Inject
    lateinit var dataProvider: DataProvider

    private var isNeedToSaveAvatar = false

    private val job: Job = Job()
    private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val _liveDataSearchProductList = MutableLiveData<List<Product>>().apply { value = listOf() }
    val liveDataSearchProductList: LiveData<List<Product>> = _liveDataSearchProductList

    private val _liveDataProductList = MutableLiveData<MutableList<Product>>().apply { value = mutableListOf() }
    val liveDataProductList: LiveData<MutableList<Product>> = _liveDataProductList

    private val _liveDataPromotionalProductList = MutableLiveData<MutableList<Product>>().apply { value = mutableListOf() }
    val liveDataPromotionalProductList: LiveData<MutableList<Product>> = _liveDataPromotionalProductList

    private val _liveDataIsLoading = MutableLiveData<Boolean>().apply { value = false }
    val liveDataIsLoading: LiveData<Boolean> = _liveDataIsLoading

    private val _liveDataAvatarImage = MutableLiveData<Bitmap?>().apply { value = null }
    val liveDataAvatarImage: LiveData<Bitmap?> = _liveDataAvatarImage

    private val _liveDataUserEmail = MutableLiveData<String?>().apply { value = null }
    val liveDataUserEmail: LiveData<String?> = _liveDataUserEmail

    private val _liveDataShowError = MutableLiveData<EventWithContent<Exception>>()
    val liveDataShowError: LiveData<EventWithContent<Exception>> = _liveDataShowError

    private val _liveDataShowNoInternetWarning = MutableLiveData<Event>()
    val liveDataShowNoInternetWarning: LiveData<Event> = _liveDataShowNoInternetWarning

    private val _liveDataStartLoginActivity = MutableLiveData<Event>()
    val liveDataStartLoginActivity: LiveData<Event> = _liveDataStartLoginActivity

    init {
        App.appComponent.inject(this)

        try {
            fetchProducts()
            loadUserEmail()
            loadUserAvatar()
        } catch (e: Exception) {
            _liveDataShowNoInternetWarning.value = Event()
            _liveDataIsLoading.value = false
        }

        if (isNeedToSaveAvatar) {
            _liveDataAvatarImage.value?.let { bitmap ->
                uiCoroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        dataProvider.saveUserProfilePhoto(bitmap, "JPEG_" + UUID.randomUUID())
                    }
                    isNeedToSaveAvatar = false//TODO: Handle if photo not saved (Use WorkManager)
                }
            }
        }
    }

    fun loadProducts(isNetworkAvailable: Boolean) {
        if (_liveDataProductList.value?.isEmpty() != false && _liveDataProductList.value?.isEmpty() != false) {
            forceLoadProducts(isNetworkAvailable)
        }
    }

    fun forceLoadProducts(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            fetchProducts()
        } else {
            _liveDataShowNoInternetWarning.value = Event()
            _liveDataIsLoading.value = false
        }
    }

    private fun fetchProducts() {
        uiCoroutineScope.launch {
            try {
                _liveDataIsLoading.value = true

                val productsDeferred = uiCoroutineScope.async { dataProvider.getProducts() }
                val promotionalProductsDeferred = uiCoroutineScope.async { dataProvider.getPromotionalProducts() }
                _liveDataProductList.value = productsDeferred.await()
                _liveDataPromotionalProductList.value = promotionalProductsDeferred.await()

            } catch (ex: Exception) {
                Timber.e("Products fetching error:$ex")
                _liveDataShowError.value = EventWithContent(ex)
            } finally {
                _liveDataIsLoading.value = false
            }
        }
    }

    private fun loadUserEmail() {
        if (_liveDataUserEmail.value == null) {
            uiCoroutineScope.launch {
                try {
                    val user = withContext(Dispatchers.IO) { dataProvider.getCurrentUser() }
                    _liveDataUserEmail.value = user.email
                } catch (ex: Exception) {
                    _liveDataUserEmail.value = null
                }
            }
        }
    }

    private fun loadUserAvatar() {
        if (_liveDataAvatarImage.value == null) {
            uiCoroutineScope.launch {
                try {
                    val avatarByteArray = withContext(Dispatchers.IO) { dataProvider.getUserAvatarByteArray() }
                    val avatarBitmap = withContext(Dispatchers.Default) {
                        BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.size)
                    }
                    _liveDataAvatarImage.value = avatarBitmap
                } catch (ex: Exception) {
                    _liveDataAvatarImage.value = null
                }
            }
        }
    }

    fun logOut() {
        dataProvider.logOut()
        _liveDataStartLoginActivity.value = Event()
    }

    fun searchProducts(searchText: String) {
        _liveDataIsLoading.value = true
        _liveDataProductList.value?.let {
            val searchFilter = SearchFilter(it) { productList: List<Product>? ->
                _liveDataIsLoading.value = false
                if (productList == null || productList.isEmpty()) {
                    _liveDataSearchProductList.value = listOf()
                } else {
                    _liveDataSearchProductList.value = productList
                }
            }
            searchFilter.filter.filter(searchText)
        }
    }

    //This Method called from OnActivityResult (before onResume) => view == null
    fun updateUserProfilePhoto(profilePhotoBitmap: Bitmap) {
        _liveDataAvatarImage.value = profilePhotoBitmap
        isNeedToSaveAvatar = true
    }

    fun buyProduct(product: Product) {
        Basket.addProduct(product)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}