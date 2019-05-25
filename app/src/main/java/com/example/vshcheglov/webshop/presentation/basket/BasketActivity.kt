package com.example.vshcheglov.webshop.presentation.basket

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.MenuItem
import android.view.View
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.presentation.basket.adapter.BasketRecyclerAdapter
import com.example.vshcheglov.webshop.presentation.basket.adapter.BasketRecyclerItemTouchHelper
import com.example.vshcheglov.webshop.presentation.entites.ProductBasketCard
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.example.vshcheglov.webshop.presentation.order.OrderActivity
import kotlinx.android.synthetic.main.activity_basket.*
import kotlinx.android.synthetic.main.activity_basket_list_layout.*
import kotlinx.android.synthetic.main.message_with_action_layout.*
import nucleus5.factory.RequiresPresenter
import nucleus5.view.NucleusAppCompatActivity

@RequiresPresenter(BasketViewModel::class)
class BasketActivity : NucleusAppCompatActivity<BasketViewModel>(), BasketViewModel.BasketView,
    BasketRecyclerItemTouchHelper.BasketRecyclerItemTouchHelperListener {

    private lateinit var basketAdapter: BasketRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)

        initActionBar()
        initEmptyBasketLayout()
        basketMakeOrderButton.setOnClickListener { presenter?.makeOrder() }
    }

    private fun initEmptyBasketLayout() {
        messageActionLayoutTitle.text = getString(R.string.basket_empty_title)
        messageActionLayoutText.text = getString(R.string.basket_empty_text)
        messageActionLayoutButton.text = getString(R.string.basket_empty_button_text)
        messageActionLayoutButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).also {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    override fun showBasket(productBaseketCardList: MutableList<ProductBasketCard>) {
        with(basketRecyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@BasketActivity)
            basketAdapter = BasketRecyclerAdapter(productBaseketCardList).also {
                it.onProductNumberIncreasedListener = { position -> presenter?.productNumberIncreased(position) }
                it.onProductNumberDecreasedListener = { position -> presenter?.productNumberDecreased(position) }
            }
            adapter = basketAdapter
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            val itemTouchSimpleCallback =
                BasketRecyclerItemTouchHelper(
                    0,
                    ItemTouchHelper.LEFT,
                    this@BasketActivity
                )
            ItemTouchHelper(itemTouchSimpleCallback).attachToRecyclerView(this)
        }
    }

    private fun initActionBar() {
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSwiped(
        viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        direction: Int,
        position: Int
    ) {
        val holder = viewHolder as? BasketRecyclerAdapter.ViewHolder
        holder?.let { presenter?.removeProductFromBasket(position) }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun startOrderActivity() {
        val intent = Intent(this, OrderActivity::class.java)
        startActivity(intent)
    }

    override fun setBasketAmount(amount: Double) {
        val amountTitle = String.format(getString(R.string.price_format), amount)
        basketAmountTextView.text = amountTitle
    }

    override fun setBasketItemsNumber(itemsNumber: String) {
        basketItemsTextView.text = itemsNumber
    }

    override fun setBasketIsEmptyWarning(isEmpty: Boolean) {
        if (isEmpty) {
            basketListLayout.visibility = View.INVISIBLE
            basketMakeOrderButton.visibility = View.INVISIBLE
            basketEmptyLayout.visibility = View.VISIBLE
        } else {
            basketListLayout.visibility = View.VISIBLE
            basketMakeOrderButton.visibility = View.VISIBLE
            basketEmptyLayout.visibility = View.GONE
        }
        basketMakeOrderButton.isEnabled = !isEmpty
    }

    override fun removeProductCard(position: Int) {
        basketAdapter.removeItem(position)
        val undoTitle = getString(R.string.removed_item_snackbar)
        val snackBar = Snackbar.make(basketCoordinatorLayout, undoTitle, Snackbar.LENGTH_SHORT)
        snackBar.setAction(getString(R.string.undo_uppercase)) { presenter?.restoreProductCard() }
        snackBar.show()
    }

    override fun restoreSameProductsCard(deletedIndex: Int) {
        basketAdapter.restoreItem(deletedIndex)
    }

    override fun setSameProductsNumber(position: Int, number: Int) {
        val view = basketRecyclerView.layoutManager?.findViewByPosition(position)
        view?.let { basketAdapter.setProductsNumberByPosition(it, number, position) }
    }

    override fun setTotalProductPrice(position: Int, totalDiscountPrice: Double) {
        val view = basketRecyclerView.layoutManager?.findViewByPosition(position)
        view?.let { basketAdapter.updateCardTotalPrice(position, totalDiscountPrice, view) }
    }

    override fun setTotalProductPriceTitle(position: Int, totalPrice: Double, percentageDiscount: Double) {
        val view = basketRecyclerView.layoutManager?.findViewByPosition(position)
        view?.let { basketAdapter.updateCardTotalPriceTitle(position, totalPrice, view, percentageDiscount) }
    }

    override fun onResume() {
        super.onResume()
        presenter?.initProductListWithBasketInfo()
    }
}
