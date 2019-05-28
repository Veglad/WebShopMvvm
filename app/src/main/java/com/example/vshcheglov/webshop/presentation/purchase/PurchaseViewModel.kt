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

    private val _liveDataShowNoProducts = MutableLiveData<Event>()
    val liveDataShowNoProducts: LiveData<Event> = _liveDataShowNoProducts

    private val _liveDataIsLoading = MutableLiveData<Boolean>()
    val liveDataIsLoading: LiveData<Boolean> = _liveDataIsLoading

    private val _liveDataShowProductsLoadingError = MutableLiveData<Exception>()
    val liveDataShowProductsLoadingError: LiveData<Exception> = _liveDataShowProductsLoadingError

    private val _liveDataProducts = MutableLiveData<List<Pair<OrderProduct, Timestamp>>>()
    val liveDataProducts: LiveData<List<Pair<OrderProduct, Timestamp>>> = _liveDataProducts

    init {
        App.appComponent.inject(this)
        loadPurchasedProducts()
    }

    fun loadPurchasedProducts() {
        uiCoroutineScope.launch {
            try {
                _liveDataIsLoading.value = true
                val orderList = withContext(Dispatchers.IO) { dataProvider.getUserOrders() }

                if (orderList.isNotEmpty()) {
                    val productToTimeStampList = orderList.map { order ->
                        order.orderProducts.map { Pair(it, order.timestamp) }
                    }.flatten()
                    _liveDataProducts.value = productToTimeStampList
                } else {
                    _liveDataShowNoProducts.value = Event()
                }
            } catch (ex: Exception) {
                Timber.d("Getting user products error: $ex")
                _liveDataShowProductsLoadingError.value = ex
            } finally {
                _liveDataIsLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}