package com.sigma.vshcheglov.webshop.data.users

import com.sigma.vshcheglov.webshop.data.entities.RealmUserCredentials
import io.realm.Realm

class UserCredentialsStorage {
    fun saveUserCredentials(realmUserCredentials: RealmUserCredentials) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { transactionRealm ->
                transactionRealm.insertOrUpdate(realmUserCredentials)
            }
        }
    }

    fun containsUserCredentials(): Boolean {
        var containsCredentials = false
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { transactionRealm ->
                containsCredentials = transactionRealm.where(RealmUserCredentials::class.java)
                    .findAll().isNotEmpty()
            }
        }

        return containsCredentials
    }

    fun getUserCredentials(): RealmUserCredentials? {
        var realmUserCredentials: RealmUserCredentials? = null
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { transactionRealm ->
                val realmResults = transactionRealm.where(RealmUserCredentials::class.java)
                    .findFirst()
                realmResults?.let {
                    realmUserCredentials = RealmUserCredentials(realmResults.email, realmResults.encryptedPassword)
                }
            }
        }

        return realmUserCredentials
    }

    fun deleteUserCredentials() {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { transactionRealm ->
                val realmResults = transactionRealm.where(RealmUserCredentials::class.java)
                    .findFirst()
                realmResults?.deleteFromRealm()
            }
        }
    }
}