package com.example.vshcheglov.webshop.presentation.main

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.main.helpers.SearchFilter
import kotlinx.coroutines.*
import nucleus5.presenter.Presenter
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MainPresenter : Presenter<MainPresenter.MainView>() {
    @Inject
    lateinit var dataProvider: DataProvider

    init {
        App.appComponent.inject(this)
    }

    private var isLoading = false
    private var isNetworkAvailable = false

    private var isNeedToSaveAvatar = false

    private lateinit var job: Job
    private lateinit var uiCoroutineScope: CoroutineScope

    private var productList: MutableList<Product>? = null
    private var promotionalProductList: MutableList<Product>? = null
    private var userAvatarBitmap: Bitmap? = null
    private var userEmail: String? = null

    private lateinit var searchFilter: SearchFilter



    fun loadProducts(isNetworkAvailable: Boolean) {
        this.isNetworkAvailable = isNetworkAvailable
        fetchProducts(true, isNetworkAvailable)
    }

    override fun onDropView() {
        super.onDropView()
        job.cancel()
    }

    private fun fetchProducts(refresh: Boolean, isNetworkAvailable: Boolean) {
        initCoroutineJob()
        if (productList != null && promotionalProductList != null && !refresh) return
        uiCoroutineScope.launch {
            try {
                isLoading = true
                view?.showLoading(true)

                val productsDeferred = uiCoroutineScope.async { dataProvider.getProducts() }
                val promotionalProductsDeferred = uiCoroutineScope.async { dataProvider.getPromotionalProducts() }
                productList = productsDeferred.await()
                promotionalProductList = promotionalProductsDeferred.await()

                productList?.let { products ->
                    promotionalProductList?.let { promotionalProducts ->
                        processUiWithProducts(products, promotionalProducts)
                    }
                }

                if (!isNetworkAvailable) {
                    view?.showNoInternetWarning()
                }
            } catch (ex: Exception) {
                Timber.e("Products fetching error:$ex")
                view?.let {
                    it.showError(ex)
                }
            } finally {
                isLoading = false
                view?.showLoading(false)
            }
        }
    }

    private fun processUiWithProducts(
        productList: MutableList<Product>,
        promotionalProductList: MutableList<Product>
    ) {
        Timber.d("Products fetched successfully")
        view?.let {
            it.showProductList(productList)
            it.showPromotionalProductList(promotionalProductList)
        }
    }

    private fun loadUserEmail() {
        if (userEmail == null) {
            uiCoroutineScope.launch {
                try {
                    val user = withContext(Dispatchers.IO) { dataProvider.getCurrentUser() }
                    userEmail = user.email
                    view?.showUserEmail(userEmail)
                } catch (ex: Exception) {
                    view?.showEmailLoadError(ex)
                }
            }
        }
    }

    private fun loadUserAvatar() {
        if (userAvatarBitmap == null) {
            uiCoroutineScope.launch {
                try {
                    val avatarByteArray = withContext(Dispatchers.IO) { dataProvider.getUserAvatarByteArray() }
                    val avatarBitmap = withContext(Dispatchers.Default) {
                        BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.size)
                    }
                    userAvatarBitmap = avatarBitmap
                    view?.setUserAvatarImage(avatarBitmap)
                } catch (ex: Exception) {
                    view?.showAvatarLoadError(ex)
                }
            }
        }
    }


    override fun onTakeView(view: MainView?) {
        super.onTakeView(view)
        initCoroutineJob()

        view?.showLoading(isLoading)
        fetchProducts(false, isNetworkAvailable)
        loadUserEmail()
        loadUserAvatar()

        if (isNeedToSaveAvatar) {
            userAvatarBitmap?.let {
                uiCoroutineScope.launch {
                    view?.setUserAvatarImage(it)
                    withContext(Dispatchers.IO) {
                        dataProvider.saveUserProfilePhoto(it, "JPEG_" + UUID.randomUUID())
                    }
                    isNeedToSaveAvatar = false//TODO: Handle if photo not saved
                }
            }
        }
    }

    private fun initCoroutineJob() {
        job = Job()
        uiCoroutineScope = CoroutineScope(Dispatchers.Main + job)
    }

    fun logOut() {
        dataProvider.logOut()
        view?.startLoginActivity()
    }

    fun searchProducts(searchText: String) {
        view?.showLoading(true)
        productList?.let {
            val searchFilter = SearchFilter(it) { productList: List<Product>? ->
                view?.showLoading(false)
                if (productList == null || productList.isEmpty()) {
                    view?.showNoResults()
                } else {
                    view?.showSearchedProducts(productList)
                }
            }
            searchFilter.filter.filter(searchText)
        }
    }

    //This Method called from OnActivityResult (before onResume) => view == null
    fun updateUserProfilePhoto(profilePhotoBitmap: Bitmap) {
        this.userAvatarBitmap = profilePhotoBitmap
        isNeedToSaveAvatar = true
    }

    fun buyProduct(product: Product) {
        Basket.addProduct(product)
    }

    interface MainView {
        fun showLoading(isLoading: Boolean)

        fun showNoInternetWarning()

        fun showError(throwable: Throwable)

        fun showEmailLoadError(throwable: Throwable)

        fun showProductList(productList: MutableList<Product>)

        fun showPromotionalProductList(promotionalList: List<Product>)

        fun startLoginActivity()

        fun showUserEmail(email: String?)

        fun showNoResults()

        fun showSearchedProducts(productList: List<Product>)

        fun setUserAvatarImage(bitmap: Bitmap)

        fun showAvatarLoadError(throwable: Throwable)

        fun startBasketActivity()
    }
}