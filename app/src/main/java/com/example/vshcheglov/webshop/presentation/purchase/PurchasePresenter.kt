package com.example.vshcheglov.webshop.presentation.purchase

import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.OrderProduct
import com.google.firebase.Timestamp
import kotlinx.coroutines.*
import nucleus5.presenter.Presenter
import timber.log.Timber
import javax.inject.Inject

class PurchasePresenter : Presenter<PurchasePresenter.View>() {

    @Inject
    lateinit var dataProvider: DataProvider

    private lateinit var job: Job
    private lateinit var uiCoroutineScope: CoroutineScope

    init {
        App.appComponent.inject(this)
    }

    override fun onTakeView(view: View?) {
        super.onTakeView(view)
        initCoroutineJob()
        loadPurchasedProducts(view)
    }

    private fun loadPurchasedProducts(view: View?) {
        uiCoroutineScope.launch {
            try {
                view?.setShowLoading(true)
                val orderList = withContext(Dispatchers.IO) { dataProvider.getUserOrders() }

                if (orderList.isNotEmpty()) {
                    val productToTimeStampList = orderList.map { order ->
                        order.orderProducts.map { Pair(it, order.timestamp) }
                    }.flatten()
                    view?.showProducts(productToTimeStampList)
                } else {
                    view?.showNoData()
                }
            } catch (ex: Exception) {
                Timber.d("Getting user products error: $ex")
                view?.showProductsFetchingError(ex)
            } finally {
                view?.setShowLoading(false)
            }
        }
    }

    private fun initCoroutineJob() {
        job = Job()
        uiCoroutineScope = CoroutineScope(Dispatchers.Main + job)
    }

    override fun onDropView() {
        super.onDropView()
        job.cancel()
    }

    interface View {
        fun showProducts(productToTimeStampList: List<Pair<OrderProduct, Timestamp>>)

        fun showProductsFetchingError(exception: Exception)

        fun showNoData()

        fun setShowLoading(isLoading: Boolean)
    }
}