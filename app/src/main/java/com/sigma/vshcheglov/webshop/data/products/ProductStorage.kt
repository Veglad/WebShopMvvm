package com.sigma.vshcheglov.webshop.data.products

import com.sigma.vshcheglov.webshop.App
import com.sigma.vshcheglov.webshop.data.entities.RealmProduct
import com.sigma.vshcheglov.webshop.data.entities.mappers.RealmProductMapper
import com.sigma.vshcheglov.webshop.domain.Product
import io.realm.Realm
import io.realm.RealmList
import javax.inject.Inject

class ProductStorage {

    @Inject
    lateinit var productMapper: RealmProductMapper

    init {
        App.appComponent.inject(this)
    }

    fun saveProductsToDb(productList: MutableList<Product>) {
        val realmProductList = mutableListOf<RealmProduct>().apply {
            for (product in productList) {
                add(productMapper.map(product))
            }
        }

        Realm.getDefaultInstance().use { realm ->
            realm.executeTransactionAsync { transactionRealm ->
                val managedProductList = RealmList<RealmProduct>()
                managedProductList.addAll(realmProductList)
                transactionRealm.insertOrUpdate(realmProductList)
            }
        }
    }

    fun getProductsFromDb(isPromotional: Boolean = false): MutableList<Product> {

        var productList: MutableList<Product> = mutableListOf()
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { transactionRealm ->
                val managedProducts = if (!isPromotional) {
                    transactionRealm.where(RealmProduct::class.java).findAll()
                } else {
                    transactionRealm.where(RealmProduct::class.java)
                        .greaterThan("percentageDiscount", 0).findAll()
                }

                productList = mutableListOf<Product>().apply {
                    for (realmProduct in managedProducts) {
                        add(productMapper.map(realmProduct))
                    }
                }
            }
        }

        return productList
    }
}