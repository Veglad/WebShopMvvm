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
import com.example.vshcheglov.webshop.presentation.basket.BasketActivity
import com.example.vshcheglov.webshop.presentation.purchase.PurchaseActivity
import com.example.vshcheglov.webshop.presentation.login.LoginActivity
import com.example.vshcheglov.webshop.presentation.main.adapters.ProductsRecyclerAdapter
import com.example.vshcheglov.webshop.presentation.main.adapters.SearchRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_error_layout.*
import kotlinx.android.synthetic.main.main_products.*
import kotlinx.android.synthetic.main.main_search_empty.*
import kotlinx.android.synthetic.main.main_search_list.*
import nucleus5.factory.RequiresPresenter
import nucleus5.view.NucleusAppCompatActivity
import android.app.Activity
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.vshcheglov.webshop.BuildConfig
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


@RequiresPresenter(MainPresenter::class)
class MainActivity : NucleusAppCompatActivity<MainPresenter>(), MainPresenter.MainView {

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 14561
    }

    private lateinit var searchView: SearchView
    private lateinit var headerUserEmail: TextView
    private lateinit var navHeaderUserImage: ImageView
    private lateinit var navHeaderImageProgressBar: ProgressBar
    private lateinit var navMainHeader: View
    private lateinit var currentPhotoPath: String
    private var snackbar: Snackbar? = null
    private val productsRecyclerAdapter = ProductsRecyclerAdapter(this)
    private val searchRecyclerAdapter = SearchRecyclerAdapter(this)

    private lateinit var toggle: ActionBarDrawerToggle
    private var isErrorVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter?.loadProducts(isNetworkAvailable())

        tryAgainButton.setOnClickListener {
            val isNetworkAvailable = isNetworkAvailable()

            setErrorVisibility(!isNetworkAvailable)
            presenter?.loadProducts(isNetworkAvailable)
            if (isNetworkAvailable) {
                snackbar?.dismiss()
            }
        }

        productsSwipeRefreshLayout.setOnRefreshListener {
            val isNetworkAvailable = isNetworkAvailable()

            presenter?.loadProducts(isNetworkAvailable)
            if (isNetworkAvailable) {
                snackbar?.dismiss()
            }
        }

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

    private fun onBuyClicked(product: Product) {
        presenter.buyProduct(product)
        startBasketActivity()
    }

    private fun initNavigationDrawer() {
        mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_main_log_out -> presenter.logOut()
                R.id.nav_main_basket -> startBasketActivity()
                R.id.nav_main_bought -> startActivity(Intent(this, PurchaseActivity::class.java))
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
            startActivityForImageResult()
        }
    }

    private fun startActivityForImageResult() {
        val galleryIntent = getPickImageIntent()
        val cameraIntent = getCaptureIntent()

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

    override fun setUserAvatarImage(bitmap: Bitmap) {
        navHeaderUserImage = navMainHeader.findViewById(R.id.navHeaderUserImage)
        Glide.with(this).load(bitmap).apply(RequestOptions.circleCropTransform()).into(navHeaderUserImage)
    }

    private fun getPickImageIntent() = Intent(Intent.ACTION_PICK).also { imagePickIntent ->
        imagePickIntent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        imagePickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    }

    private fun getCaptureIntent() = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        takePictureIntent.resolveActivity(packageManager)?.also {
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, resources.getString(R.string.taking_photo_error), Toast.LENGTH_LONG).show()
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK ||
            //Always returns requestCode == -1 TODO: Investigate problem
            requestCode == PICK_IMAGE_REQUEST_CODE && resultCode != Activity.RESULT_CANCELED
        ) {
            when (requestCode) {
                PICK_IMAGE_REQUEST_CODE -> {
                    val selectedImage: Uri
                    val isPhotoFromCamera = data == null
                    selectedImage = if (isPhotoFromCamera) {
                        Uri.parse("file://$currentPhotoPath")
                    } else {
                        data?.data ?: kotlin.run {
                            Toast.makeText(
                                this,
                                resources.getString(R.string.taking_photo_error), Toast.LENGTH_LONG
                            ).show()
                            return
                        }
                    }

                    val profilePhotoBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                    profilePhotoBitmap?.let {
                        currentPhotoPath = ""
                        presenter.updateUserProfilePhoto(profilePhotoBitmap)
                    }

                    if (currentPhotoPath.isNotEmpty()) {
                        deleteTempPhotoFile(currentPhotoPath)
                    }
                }
            }
        }
    }


    private fun deleteTempPhotoFile(currentPhotoPath: String) {
        val file = File(currentPhotoPath)
        file.delete()
    }

    override fun showLoading(isLoading: Boolean) {
        productsSwipeRefreshLayout.isRefreshing = isLoading
        if (isLoading) {
            productsRecyclerView.visibility = View.INVISIBLE
        } else {
            productsRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun showNoInternetWarning() {
        val isNetworkAvailable = isNetworkAvailable()
        snackbar = Snackbar.make(
            mainFrameLayout,
            getString(R.string.no_internet_connection_warning), Snackbar.LENGTH_INDEFINITE
        )
        snackbar?.setAction(getString(R.string.try_again_button)) {
            if (isNetworkAvailable) {
                setErrorVisibility(false)
            }

            presenter?.loadProducts(isNetworkAvailable)
            snackbar?.dismiss()
        }
        snackbar?.show()
    }

    override fun showError(throwable: Throwable) {
        setErrorVisibility(true)
    }

    override fun showProductList(productList: MutableList<Product>) {
        setErrorVisibility(false)
        productsRecyclerAdapter.apply {
            setProductList(productList)
            notifyDataSetChanged()
        }
    }

    override fun showPromotionalProductList(promotionalList: List<Product>) {
        setErrorVisibility(false)
        productsRecyclerAdapter.updatePromotionalList(promotionalList)
    }

    override fun startLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun showUserEmail(email: String?) {
        email?.let {
            headerUserEmail.text = it
        }
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
                startBasketActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun startBasketActivity() {
        startActivity(Intent(this, BasketActivity::class.java))
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
                            presenter.searchProducts(searchText)
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

    override fun showNoResults() {
        showLayout(MainLayouts.SEARCH_EMPTY)
        mainSearchEmptyTextView.text = resources.getString(R.string.no_search_result)
    }

    override fun showSearchedProducts(productList: List<Product>) {
        showLayout(MainLayouts.SEARCH_PRODUCTS)
        searchRecyclerAdapter.apply {
            this.productList.clear()
            this.productList.addAll(productList)
            notifyDataSetChanged()
        }
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

    override fun showEmailLoadError(throwable: Throwable) {
        showUserEmail("")
    }

    override fun showAvatarLoadError(throwable: Throwable) {
        setUserAvatarImage(BitmapFactory.decodeResource(resources, R.drawable.profile_avatar_placeholder_large))
    }
}
