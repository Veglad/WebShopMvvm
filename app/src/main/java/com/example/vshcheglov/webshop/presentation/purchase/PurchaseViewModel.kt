package com.example.vshcheglov.webshop.presentation.purchase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.OrderProduct
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.helpres.Event
import com.example.vshcheglov.webshop.presentation.helpres.EventWithContent
import com.google.firebase.Timestamp
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class PurchaseViewModel : ViewModel() {

    @Inject
    lateinit var dataProvider: DataProvider

    private val job: Job = Job()
    private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val _stateLiveData: MutableLiveData<PurchaseViewState> = MutableLiveData<PurchaseViewState>().apply {
        value = PurchaseViewState()
    }
    val stateLiveData: LiveData<PurchaseViewState> = _stateLiveData

    private val _command: MutableLiveData<EventWithContent<PurchaseCommand>> = MutableLiveData()
    val commandLiveData: LiveData<EventWithContent<PurchaseCommand>> = _command

    private fun getState() = stateLiveData.value!!

    init {
        App.appComponent.inject(this)
        loadPurchasedProducts()
    }

    fun loadPurchasedProducts() {
        uiCoroutineScope.launch {
            try {
                _stateLiveData.value = PurchaseViewState(isLoading = true)
                val orderList = withContext(Dispatchers.IO) { dataProvider.getUserOrders() }

                if (orderList.isNotEmpty()) {
                    val productToTimeStampList = orderList.map { order ->
                        order.orderProducts.map { Pair(it, order.timestamp) }
                    }.flatten()
                    _stateLiveData.value = getState().copy(products = productToTimeStampList)
                } else {
                    _command.value = EventWithContent(PurchaseCommand.NotifyNoProducts)
                }
            } catch (ex: Exception) {
                Timber.d("Getting user products error: $ex")
                _command.value = EventWithContent(PurchaseCommand.NotifyError(ex))
            } finally {
                _stateLiveData.value = getState().copy(isLoading = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}

data class PurchaseViewState(
    var products: List<Pair<OrderProduct, Timestamp>> = listOf(),
    var isLoading: Boolean = false
)

sealed class PurchaseCommand {
    class NotifyError(val exception: Exception = Exception()) : PurchaseCommand()
    object NotifyNoProducts : PurchaseCommand()
}