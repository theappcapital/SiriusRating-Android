package com.theappcapital.siriusrating.ratingconditions

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.theappcapital.siriusrating.datastores.InMemorySiriusRatingDataStore
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class EnoughAppSessionsRatingConditionTest {

    private lateinit var inMemorySiriusRatingDataStore: InMemorySiriusRatingDataStore

    @Before
    fun setUp() {
        this.inMemorySiriusRatingDataStore = InMemorySiriusRatingDataStore()
    }

    @Test
    fun test_condition_is_satisfied_when_app_sessions_count_is_equal_or_greater_than_the_required_app_sessions() {
        // Create the condition where we require the app to be 'used' 5 times.
        val enoughAppSessionsRatingCondition = EnoughAppSessionsRatingCondition(totalAppSessionsRequired = 5u)

        // Set the app sessions count to 5, equal to the required app sessions (5).
        this.inMemorySiriusRatingDataStore.appSessionsCount = 5u
        // The condition should be satisfied, because the app was 'used' 5 times.
        assertTrue(enoughAppSessionsRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))

        // Set the app sessions count to 6, greater than the required app sessions (5).
        this.inMemorySiriusRatingDataStore.appSessionsCount = 6u
        // The condition should be satisfied, because the app was 'used' 6 times.
        assertTrue(enoughAppSessionsRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_not_satisfied_when_app_sessions_count_is_lower_than_the_required_app_sessions() {
        // Create the condition where we require the app to be 'used' 5 times.
        val enoughAppSessionsRatingCondition = EnoughAppSessionsRatingCondition(totalAppSessionsRequired = 5u)

        // Set the app sessions count to 1, lower than the required app sessions (5).
        this.inMemorySiriusRatingDataStore.appSessionsCount = 5u
        // The condition should not be satisfied, because the total amount of app sessions done by the
        // user is 1, where 5 or greater is required to be satisfied.
        assertTrue(enoughAppSessionsRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

}