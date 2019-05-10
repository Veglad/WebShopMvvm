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

    private val _searchProductList = MutableLiveData<List<Product>>().apply { value = listOf() }
    val searchProductList: LiveData<List<Product>>
        get() = _searchProductList

    private val _productList = MutableLiveData<MutableList<Product>>().apply { value = mutableListOf() }
    val productList: LiveData<MutableList<Product>>
        get() = _productList

    private val _promotionalProductList = MutableLiveData<MutableList<Product>>().apply { value = mutableListOf() }
    val promotionalProductList: LiveData<MutableList<Product>>
        get() = _promotionalProductList

    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _avatarImage = MutableLiveData<Bitmap?>().apply { value = null }
    val avatarImage: LiveData<Bitmap?>
        get() = _avatarImage

    private val _userEmail = MutableLiveData<String?>().apply { value = null }
    val userEmail: LiveData<String?>
        get() = _userEmail

    private val _showError = MutableLiveData<EventWithContent<Exception>>()
    val showError: LiveData<EventWithContent<Exception>>
        get() = _showError

    private val _showNoInternetWarning = MutableLiveData<Event>()
    val showNoInternetWarning: LiveData<Event>
        get() = _showNoInternetWarning

    private val _startLoginActivity = MutableLiveData<Event>()
    val startLoginActivity: LiveData<Event>
        get() = _startLoginActivity

    init {
        App.appComponent.inject(this)

        try {
            fetchProducts()
            loadUserEmail()
            loadUserAvatar()
        } catch (e: Exception) {
            _showNoInternetWarning.value = Event()
            _isLoading.value = false
        }

        if (isNeedToSaveAvatar) {
            _avatarImage.value?.let { bitmap ->
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
        if (_productList.value?.isEmpty() != false && _productList.value?.isEmpty() != false) {
            forceLoadProducts(isNetworkAvailable)
        }
    }

    fun forceLoadProducts(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            fetchProducts()
        } else {
            _showNoInternetWarning.value = Event()
            _isLoading.value = false
        }
    }

    private fun fetchProducts() {
        uiCoroutineScope.launch {
            try {
                _isLoading.value = true

                val productsDeferred = uiCoroutineScope.async { dataProvider.getProducts() }
                val promotionalProductsDeferred = uiCoroutineScope.async { dataProvider.getPromotionalProducts() }
                _productList.value = productsDeferred.await()
                _promotionalProductList.value = promotionalProductsDeferred.await()

            } catch (ex: Exception) {
                Timber.e("Products fetching error:$ex")
                _showError.value = EventWithContent(ex)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserEmail() {
        if (_userEmail.value == null) {
            uiCoroutineScope.launch {
                try {
                    val user = withContext(Dispatchers.IO) { dataProvider.getCurrentUser() }
                    _userEmail.value = user.email
                } catch (ex: Exception) {
                    _userEmail.value = null
                }
            }
        }
    }

    private fun loadUserAvatar() {
        if (_avatarImage.value == null) {
            uiCoroutineScope.launch {
                try {
                    val avatarByteArray = withContext(Dispatchers.IO) { dataProvider.getUserAvatarByteArray() }
                    val avatarBitmap = withContext(Dispatchers.Default) {
                        BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.size)
                    }
                    _avatarImage.value = avatarBitmap
                } catch (ex: Exception) {
                    _avatarImage.value = null
                }
            }
        }
    }

    fun logOut() {
        dataProvider.logOut()
        _startLoginActivity.value = Event()
    }

    fun searchProducts(searchText: String) {
        _isLoading.value = true
        _productList.value?.let {
            val searchFilter = SearchFilter(it) { productList: List<Product>? ->
                _isLoading.value = false
                if (productList == null || productList.isEmpty()) {
                    _searchProductList.value = listOf()
                } else {
                    _searchProductList.value = productList
                }
            }
            searchFilter.filter.filter(searchText)
        }
    }

    //This Method called from OnActivityResult (before onResume) => view == null
    fun updateUserProfilePhoto(profilePhotoBitmap: Bitmap) {
        _avatarImage.value = profilePhotoBitmap
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