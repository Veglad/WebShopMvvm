package com.example.vshcheglov.webshop.data.users

import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.entities.RealmOrder
import com.example.vshcheglov.webshop.data.entities.mappers.RealmOrderMapper
import com.example.vshcheglov.webshop.domain.Order
import io.realm.Realm
import io.realm.RealmList
import javax.inject.Inject

class UserStorage {

    @Inject
    lateinit var mapper: RealmOrderMapper

    init {
        App.appComponent.inject(this)
    }

    fun saveOrders(realmOrderList: MutableList<RealmOrder>) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { transactionRealm ->
                val managedOrderList = RealmList<RealmOrder>()
                managedOrderList.addAll(realmOrderList)
                transactionRealm.insertOrUpdate(realmOrderList)
            }
        }
    }

    fun clear() {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { transactionRealm ->
                transactionRealm.deleteAll()
            }
        }
    }

    fun getUserOrders(): MutableList<Order> {
        var realmOrderList: MutableList<Order> = mutableListOf()
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { transactionRealm ->
                val realmResults = transactionRealm.where(RealmOrder::class.java)
                    .findAll()
                realmOrderList = mutableListOf<Order>().apply {
                    for (realmResult in realmResults) {
                        add(mapper.map(realmResult))
                    }
                }
            }
        }

        return realmOrderList
    }
}