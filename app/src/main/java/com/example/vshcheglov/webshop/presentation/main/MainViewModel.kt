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

    private val _stateLiveData: MutableLiveData<MainViewState> = MutableLiveData<MainViewState>().apply {
        value = MainViewState()
    }
    val stateLiveData: LiveData<MainViewState> = _stateLiveData

    private val _commandLiveData: MutableLiveData<EventWithContent<MainCommand>> = MutableLiveData()
    val commandLiveData: LiveData<EventWithContent<MainCommand>> = _commandLiveData

    private fun getState() = stateLiveData.value!!

    init {
        App.appComponent.inject(this)

        try {
            fetchProducts()
            loadUserEmail()
            loadUserAvatar()
        } catch (e: Exception) {
            setCommand(MainCommand.ShowNoInternet)
        } finally {
            _stateLiveData.value = getState().copy(isLoading = false)
        }

        if (isNeedToSaveAvatar) {
            getState().avatarImage?.let { bitmap ->
                uiCoroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        dataProvider.saveUserProfilePhoto(bitmap, "JPEG_" + UUID.randomUUID())
                    }
                    isNeedToSaveAvatar = false//TODO: Handle if photo is not saved (Use WorkManager)
                }
            }
        }
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
            setCommand(MainCommand.ShowNoInternet)
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
                setCommand(MainCommand.ShowError(ex))
            } finally {
                _stateLiveData.value = getState().copy(isLoading = false)
            }
        }
    }

    private fun loadUserEmail() {
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

    private fun loadUserAvatar() {
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
        setCommand(MainCommand.StartLoginScreen)
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

    //This Method called from OnActivityResult (before onResume) => view == null
    fun updateUserProfilePhoto(profilePhotoBitmap: Bitmap) {
        _stateLiveData.value = getState().copy(avatarImage = profilePhotoBitmap)
        isNeedToSaveAvatar = true
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
}

data class MainViewState(
    var isLoading: Boolean = false,
    var productList: MutableList<Product> = mutableListOf(),
    var promotionalProductList: MutableList<Product> = mutableListOf(),
    var searchProductList: List<Product> = listOf(),
    var avatarImage: Bitmap? = null,
    var userEmail: String? = null
)

sealed class MainCommand {
    class ShowError(val exception: Exception) : MainCommand()
    object ShowNoInternet : MainCommand()
    object StartLoginScreen : MainCommand()
}