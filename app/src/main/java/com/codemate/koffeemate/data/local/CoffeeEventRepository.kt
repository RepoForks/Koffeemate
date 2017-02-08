package com.codemate.koffeemate.data.local

import com.codemate.koffeemate.data.models.CoffeeBrewingEvent
import com.codemate.koffeemate.data.models.User
import io.realm.Realm
import io.realm.Sort
import java.util.*

interface CoffeeEventRepository {
    fun recordBrewingEvent(user: User? = null): CoffeeBrewingEvent
    fun recordBrewingAccident(user: User): CoffeeBrewingEvent
    fun getAccidentCountForUser(user: User): Long

    fun getLastBrewingEvent(): CoffeeBrewingEvent?
    fun getLastBrewingAccident(): CoffeeBrewingEvent?
}

class RealmCoffeeEventRepository : CoffeeEventRepository {
    override fun recordBrewingEvent(user: User?) = with(Realm.getDefaultInstance()) {
        var event: CoffeeBrewingEvent? = null
        executeTransaction {
            event = newEvent(it).apply {
                time = System.currentTimeMillis()
                isSuccessful = true
                this.user = if (user != null) copyToRealmOrUpdate(user) else null
            }
        }

        val copy = copyFromRealm(event)
        close()
        return@with copy!!
    }

    override fun recordBrewingAccident(user: User) = with(Realm.getDefaultInstance()) {
        var accident: CoffeeBrewingEvent? = null
        executeTransaction {
            accident = newEvent(it).apply {
                time = System.currentTimeMillis()
                isSuccessful = false
                this.user = copyToRealmOrUpdate(user)
            }
        }

        val copy = copyFromRealm(accident)
        close()
        return@with copy!!
    }

    override fun getAccidentCountForUser(user: User) = with(Realm.getDefaultInstance()) {
        val count = where(CoffeeBrewingEvent::class.java)
                .equalTo("isSuccessful", false)
                .equalTo("user.id", user.id)
                .count()

        close()
        return@with count
    }


    override fun getLastBrewingEvent() = with(Realm.getDefaultInstance()) {
        val lastEvent = where(CoffeeBrewingEvent::class.java)
                .equalTo("isSuccessful", true)
                .findAllSorted("time", Sort.ASCENDING)
                .lastOrNull()
        val copy = copyFromRealm(lastEvent)

        close()
        return@with copy
    }

    override fun getLastBrewingAccident() = with(Realm.getDefaultInstance()) {
        val accident = where(CoffeeBrewingEvent::class.java)
                .equalTo("isSuccessful", false)
                .findAllSorted("time", Sort.ASCENDING)
                .lastOrNull()
        val copy = copyFromRealm(accident)

        close()
        return@with copy
    }

    private fun newEvent(realm: Realm) =
            realm.createObject(
                    CoffeeBrewingEvent::class.java,
                    UUID.randomUUID().toString()
            )
}