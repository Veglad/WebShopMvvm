package com.example.vshcheglov.webshop.presentation.purchase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.OrderProduct
import com.example.vshcheglov.webshop.presentation.helpres.Event
import com.google.firebase.Timestamp
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class PurchaseViewModel : ViewModel() {

    @Inject
    lateinit var dataProvider: DataProvider

    private val job: Job = Job()
    private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val _showNoProducts = MutableLiveData<Event>()
    val showNoProducts: LiveData<Event>
        get() = _showNoProducts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _showProductsLoadingError = MutableLiveData<Exception>()
    val showProductsLoadingError: LiveData<Exception>
        get() = _showProductsLoadingError

    private val _products = MutableLiveData<List<Pair<OrderProduct, Timestamp>>>()
    val products: LiveData<List<Pair<OrderProduct, Timestamp>>>
        get() = _products

    init {
        App.appComponent.inject(this)
        loadPurchasedProducts()
    }

    fun loadPurchasedProducts() {
        uiCoroutineScope.launch {
            try {
                _isLoading.value = true
                val orderList = withContext(Dispatchers.IO) { dataProvider.getUserOrders() }

                if (orderList.isNotEmpty()) {
                    val productToTimeStampList = orderList.map { order ->
                        order.orderProducts.map { Pair(it, order.timestamp) }
                    }.flatten()
                    _products.value = productToTimeStampList
                } else {
                    _showNoProducts.value = Event()
                }
            } catch (ex: Exception) {
                Timber.d("Getting user products error: $ex")
                _showProductsLoadingError.value = ex
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}