package com.codemate.brewflop.data.local

import com.codemate.brewflop.data.local.models.CoffeeBrewingEvent
import io.realm.Realm
import io.realm.Sort
import java.util.*

class RealmCoffeeEventRepository : CoffeeEventRepository {
    override fun recordBrewingAccident(userId: String): CoffeeBrewingEvent {
        val realm = Realm.getDefaultInstance()
        var event = CoffeeBrewingEvent()

        realm.executeTransaction {
            event = realm.createObject(CoffeeBrewingEvent::class.java, UUID.randomUUID().toString())
            with(event) {
                time = System.currentTimeMillis()
                isSuccessful = false
                event.userId = userId
            }
        }

        realm.close()
        return event
    }

    override fun recordBrewingEvent(): CoffeeBrewingEvent {
        val realm = Realm.getDefaultInstance()
        var event = CoffeeBrewingEvent()

        realm.executeTransaction {
            event = realm.createObject(CoffeeBrewingEvent::class.java, UUID.randomUUID().toString())

            with(event) {
                time = System.currentTimeMillis()
                isSuccessful = true
            }
        }

        realm.close()
        return event
    }

    override fun getAccidentCountForUser(userId: String) =
            Realm.getDefaultInstance()
                .where(CoffeeBrewingEvent::class.java)
                .equalTo("isSuccessful", false)
                .equalTo("userId", userId)
                .count()

    override fun getLastBrewingEvent(): CoffeeBrewingEvent? {
        return Realm.getDefaultInstance()
                .where(CoffeeBrewingEvent::class.java)
                .findAllSorted("time", Sort.DESCENDING)
                .firstOrNull()
    }
}