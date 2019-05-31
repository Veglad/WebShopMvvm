package com.example.vshcheglov.webshop.presentation.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.extensions.isNetworkAvailable
import com.example.vshcheglov.webshop.presentation.main.adapters.ProductsRecyclerAdapter
import com.example.vshcheglov.webshop.presentation.main.adapters.SearchRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_error_layout.*
import kotlinx.android.synthetic.main.main_products.*
import kotlinx.android.synthetic.main.main_search_empty.*
import kotlinx.android.synthetic.main.main_search_list.*
import android.app.Activity
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.vshcheglov.webshop.presentation.helpers.ImageLoaderManager
import com.example.vshcheglov.webshop.presentation.helpers.Router

class MainActivity : AppCompatActivity() {

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 14561
    }

    private lateinit var searchView: SearchView
    private lateinit var headerUserEmail: TextView
    private lateinit var navHeaderUserImage: ImageView
    private lateinit var navHeaderImageProgressBar: ProgressBar
    private lateinit var navMainHeader: View
    private var snackbar: Snackbar? = null
    private val productsRecyclerAdapter = ProductsRecyclerAdapter(this)
    private val searchRecyclerAdapter = SearchRecyclerAdapter(this)
    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    private lateinit var toggle: ActionBarDrawerToggle
    private var isErrorVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.stateLiveData.observe(this, Observer { state -> updateUi(state) })
        viewModel.commandLiveData.observe(this, Observer { commandEvent ->
            commandEvent.getContentIfNotHandled()?.let { command -> performCommand(command) }
        })

        viewModel.loadProducts(isNetworkAvailable())
        viewModel.loadUserEmail(isNetworkAvailable())
        viewModel.loadUserAvatar(isNetworkAvailable())

        tryAgainButton.setOnClickListener { loadProducts() }
        productsSwipeRefreshLayout.setOnRefreshListener { loadProducts() }

        productsSwipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.primary),
            ContextCompat.getColor(this, R.color.color_accent),
            ContextCompat.getColor(this, R.color.dark_gray)
        )

        productsRecyclerAdapter.onBuyClickListener = { product -> onBuyClicked(product) }
        searchRecyclerAdapter.onBuyClickListener = { product -> onBuyClicked(product) }
        with(productsRecyclerView)
        {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
            adapter = productsRecyclerAdapter
        }

        with(mainSearchRecyclerView)
        {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
            adapter = searchRecyclerAdapter
        }

        setSupportActionBar(mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initNavigationDrawer()
    }

    private fun loadProducts() {
        val isNetworkAvailable = isNetworkAvailable()

        setErrorVisibility(!isNetworkAvailable)
        viewModel.forceLoadProducts(isNetworkAvailable)
        if (isNetworkAvailable) {
            snackbar?.dismiss()
        }
    }

    private fun performCommand(command: MainCommand) {
        when (command) {
            is ShowError -> showError(command.exception)
            is ShowNoInternet -> showNoInternetWarning()
            is StartLoginScreen -> Router.showLogin(this)
            is StartImagePicking -> startActivityForImageResult(command.galleryIntent, command.cameraIntent)
        }
    }

    private fun updateUi(state: MainViewState) {
        showUserEmail(state.userEmail)
        setUserAvatarImage(state.avatarImage)

        if (mainProductsLayout.visibility == View.VISIBLE) {
            showLoading(state.isLoading)
            showProductList(state.productList)
            showPromotionalProductList(state.promotionalProductList)
        } else {
            showSearchedProducts(state.searchProductList)
        }
    }

    private fun onBuyClicked(product: Product) {
        viewModel.buyProduct(product)
        Router.showBasket(this)
    }

    private fun initNavigationDrawer() {
        mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_main_log_out -> viewModel.logOut()
                R.id.nav_main_basket -> Router.showBasket(this)
                R.id.nav_main_bought -> Router.showPurchase(this)
            }

            mainDrawerLayout.closeDrawers()
            true
        }

        toggle = ActionBarDrawerToggle(this, mainDrawerLayout, R.string.open, R.string.close)
        mainDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        initNavDrawerHeader()
    }

    private fun initNavDrawerHeader() {
        navMainHeader = mainNavigationView.getHeaderView(0)
        headerUserEmail = navMainHeader.findViewById(R.id.navMainHeaderEmail)
        navHeaderImageProgressBar = navMainHeader.findViewById(R.id.navHeaderImageProgressBar)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.profile_avatar_placeholder_large)
        setUserAvatarImage(bitmap)

        navHeaderUserImage.setOnClickListener {
            mainDrawerLayout.closeDrawer(GravityCompat.START)
            viewModel.pickUserImage()
        }
    }

    private fun startActivityForImageResult(galleryIntent: Intent, cameraIntent: Intent?) {
        if (cameraIntent == null) {
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
            return
        }

        val openInChooser = Intent.createChooser(cameraIntent, resources.getString(R.string.set_picture_with))

        val resultInfoList = packageManager.queryIntentActivities(galleryIntent, 0)
        val extraIntents = arrayOfNulls<Intent>(resultInfoList.size)
        for (i in 0 until resultInfoList.size) {
            val resultInfo = resultInfoList[i]
            val packageName = resultInfo.activityInfo.packageName
            extraIntents[i] = Intent().apply {
                component = ComponentName(packageName, resultInfo.activityInfo.name)
                action = Intent.ACTION_PICK
            }
        }

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents)
        startActivityForResult(openInChooser, PICK_IMAGE_REQUEST_CODE)
    }

    private fun setUserAvatarImage(bitmap: Bitmap?) {
        navHeaderUserImage = navMainHeader.findViewById(R.id.navHeaderUserImage)
        if (bitmap == null) {
            ImageLoaderManager.loadCircularImage(navHeaderUserImage, R.drawable.profile_avatar_placeholder_large)
        } else {
            ImageLoaderManager.loadCircularImage(navHeaderUserImage, bitmap)
        }
    }

    private fun showUserEmail(email: String?) {
        email?.let {
            headerUserEmail.text = it
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK ||
            //Always returns requestCode == -1 TODO: Investigate problem
            requestCode == PICK_IMAGE_REQUEST_CODE && resultCode != Activity.RESULT_CANCELED
        ) {
            val imageUri = data?.data
            when (requestCode) {
                PICK_IMAGE_REQUEST_CODE -> viewModel.setImage(imageUri, imageUri == null)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        productsSwipeRefreshLayout.isRefreshing = isLoading
        if (isLoading) {
            productsRecyclerView.visibility = View.INVISIBLE
        } else {
            productsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showNoInternetWarning() {
        val isNetworkAvailable = isNetworkAvailable()
        snackbar = Snackbar.make(
            mainFrameLayout,
            getString(R.string.no_internet_connection_warning), Snackbar.LENGTH_INDEFINITE
        )
        snackbar?.setAction(getString(R.string.try_again_button)) {
            if (isNetworkAvailable) {
                setErrorVisibility(false)
            }

            viewModel.loadProducts(isNetworkAvailable)
            snackbar?.dismiss()
        }
        snackbar?.show()
    }

    private fun showError(exception: Exception) {
        setErrorVisibility(true)
    }

    private fun showProductList(productList: MutableList<Product>) {
        setErrorVisibility(false)
        productsRecyclerAdapter.apply {
            setProductList(productList)
            notifyDataSetChanged()
        }
    }

    private fun showPromotionalProductList(promotionalList: List<Product>) {
        setErrorVisibility(false)
        productsRecyclerAdapter.updatePromotionalList(promotionalList)
    }

    private fun showSearchedProducts(productList: List<Product>) {
        if (productList.isEmpty()) {
            showSearchProductsNoResults()
        } else {
            showLayout(MainLayouts.SEARCH_PRODUCTS)
            searchRecyclerAdapter.apply {
                this.productList.clear()
                this.productList.addAll(productList)
                notifyDataSetChanged()
            }
        }
    }

    private fun showSearchProductsNoResults() {
        showLayout(MainLayouts.SEARCH_EMPTY)
        mainSearchEmptyTextView.text = resources.getString(R.string.no_search_result)
    }

    private fun setErrorVisibility(isVisible: Boolean) {
        if (isVisible) {
            showLayout(MainLayouts.ERROR)
        } else {
            showLayout(MainLayouts.PRODUCTS)
        }

        isErrorVisible = isVisible
        invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                mainDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.actionBasket -> {
                Router.showBasket(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isErrorVisible) return false

        menu?.let {
            menuInflater.inflate(R.menu.main_menu, menu)
            val searchItem = menu.findItem(R.id.actionSearch)
            searchView = searchItem.actionView as SearchView
            with(searchView) {
                queryHint = getString(R.string.search_hint)
                val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
                setSearchableInfo(searchManager.getSearchableInfo(componentName))
                imeOptions = EditorInfo.IME_ACTION_DONE

                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(searchText: String?): Boolean {
                        clearFocus()
                        return true
                    }

                    override fun onQueryTextChange(searchText: String?): Boolean {
                        if (searchText != null && searchText.isEmpty() || searchText == null) {
                            showLayout(MainLayouts.SEARCH_EMPTY)
                            mainSearchEmptyTextView.text = resources.getString(R.string.search_list_empty_query)
                        } else {
                            viewModel.searchProducts(searchText)
                        }
                        return true
                    }
                })
            }

            searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    showLayout(MainLayouts.SEARCH_EMPTY)
                    mainSearchEmptyTextView.text = resources.getString(R.string.search_list_empty_query)
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    searchView.setQuery("", true)
                    showLayout(MainLayouts.PRODUCTS)
                    return true
                }
            })
        }

        return true
    }

    fun showLayout(mainLayouts: MainLayouts) {
        mainSearchEmptyLayout.visibility = View.GONE
        mainProductsLayout.visibility = View.GONE
        mainSearchListLayout.visibility = View.GONE
        mainErrorLayout.visibility = View.GONE

        when (mainLayouts) {
            MainLayouts.PRODUCTS -> mainProductsLayout.visibility = View.VISIBLE
            MainLayouts.SEARCH_PRODUCTS -> mainSearchListLayout.visibility = View.VISIBLE
            MainLayouts.SEARCH_EMPTY -> mainSearchEmptyLayout.visibility = View.VISIBLE
            MainLayouts.ERROR -> mainErrorLayout.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (!searchView.isIconified) {
            searchView.isIconified = true
        } else {
            super.onBackPressed()
        }
    }

    enum class MainLayouts {
        PRODUCTS, SEARCH_PRODUCTS, SEARCH_EMPTY, ERROR
    }
}
