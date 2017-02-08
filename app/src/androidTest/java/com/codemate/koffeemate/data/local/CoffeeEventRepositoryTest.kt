/*
 * Copyright 2016 Codemate Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codemate.koffeemate.data.local

import com.codemate.koffeemate.data.models.CoffeeBrewingEvent
import com.codemate.koffeemate.data.models.User
import io.realm.Realm
import io.realm.RealmConfiguration
import org.hamcrest.core.IsEqual.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class CoffeeEventRepositoryTest {
    val testConfig: RealmConfiguration = RealmConfiguration.Builder()
            .name("test.realm")
            .build()

    lateinit var coffeeEventRepository: RealmCoffeeEventRepository

    @Before
    fun setUp() {
        Realm.setDefaultConfiguration(testConfig)

        val realm = Realm.getDefaultInstance()
        realm.executeTransaction(Realm::deleteAll)
        realm.close()

        coffeeEventRepository = RealmCoffeeEventRepository()
    }

    @After
    fun tearDown() {
        Realm.deleteRealm(testConfig)
    }

    @Test
    fun recordBrewingEvent_PersistsEventsInDatabase() {
        coffeeEventRepository.recordBrewingEvent()
        coffeeEventRepository.recordBrewingEvent()
        coffeeEventRepository.recordBrewingEvent()

        assertThat(coffeeEventCount(), equalTo(3L))
    }

    @Test
    fun recordBrewingEvent_WithUserId_SavesUserId() {
        coffeeEventRepository.recordBrewingEvent(User(id = "abc123"))

        assertThat(coffeeEventRepository.getLastBrewingEvent()!!.user!!.id, equalTo("abc123"))
    }

    @Test
    fun getLastBrewingEvent_ReturnsLastBrewingEvent() {
        coffeeEventRepository.recordBrewingEvent()
        coffeeEventRepository.recordBrewingEvent()

        val lastEvent = coffeeEventRepository.recordBrewingEvent()
        assertThat(coffeeEventRepository.getLastBrewingEvent()!!.id, equalTo(lastEvent.id))
    }

    @Test
    fun getLastBrewingEvent_WhenHavingAccidentsAndSuccessfulEvents_ReturnsOnlyLastBrewingEvent() {
        val lastSuccessfulEvent = coffeeEventRepository.recordBrewingEvent()
        coffeeEventRepository.recordBrewingAccident(User())

        assertThat(coffeeEventRepository.getLastBrewingEvent()!!.user, equalTo(lastSuccessfulEvent.user))
    }

    @Test
    fun getLastBrewingAccident_ReturnsLastBrewingAccident() {
        val user = User(id = "abc123")
        coffeeEventRepository.recordBrewingAccident(user)
        coffeeEventRepository.recordBrewingAccident(user)

        val lastAccident = coffeeEventRepository.recordBrewingAccident(user)
        assertThat(coffeeEventRepository.getLastBrewingAccident()!!.id, equalTo(lastAccident.id))
        assertThat(coffeeEventRepository.getLastBrewingAccident()!!.user!!.id, equalTo(lastAccident.user!!.id))
    }

    @Test
    fun getAccidentCountForUser_ReturnsAccidentCountForThatSpecificUser() {
        val user = User(id = "abc123")
        assertThat(coffeeEventRepository.getAccidentCountForUser(user), equalTo(0L))

        coffeeEventRepository.recordBrewingAccident(user)
        coffeeEventRepository.recordBrewingAccident(user)
        coffeeEventRepository.recordBrewingAccident(user)

        val otherUser = User(id = "someotherid")
        coffeeEventRepository.recordBrewingAccident(otherUser)
        coffeeEventRepository.recordBrewingAccident(otherUser)

        assertThat(coffeeEventRepository.getAccidentCountForUser(user), equalTo(3L))
    }

    private fun coffeeEventCount() = with(Realm.getDefaultInstance()) {
        val count = where(CoffeeBrewingEvent::class.java)
                .equalTo("isSuccessful", true)
                .count()
        close()

        return@with count
    }
}