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

        close()
        return@with event!!
    }

    override fun recordBrewingAccident(user: User) = with(Realm.getDefaultInstance()) {
        var event: CoffeeBrewingEvent? = null
        executeTransaction {
            event = newEvent(it).apply {
                time = System.currentTimeMillis()
                isSuccessful = false
                this.user = copyToRealmOrUpdate(user)
            }
        }

        close()
        return@with event!!
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

        close()
        return@with lastEvent
    }

    override fun getLastBrewingAccident() = with(Realm.getDefaultInstance()) {
        val lastAccident = where(CoffeeBrewingEvent::class.java)
                .equalTo("isSuccessful", false)
                .findAllSorted("time", Sort.ASCENDING)
                .lastOrNull()

        close()
        return@with lastAccident
    }

    private fun newEvent(realm: Realm) =
            realm.createObject(
                    CoffeeBrewingEvent::class.java,
                    UUID.randomUUID().toString()
            )
}