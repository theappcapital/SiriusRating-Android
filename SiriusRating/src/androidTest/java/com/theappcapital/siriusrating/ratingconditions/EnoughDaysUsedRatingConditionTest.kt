package com.theappcapital.siriusrating.ratingconditions

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.theappcapital.siriusrating.datastores.InMemoryDataStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneOffset

@RunWith(AndroidJUnit4ClassRunner::class)
class EnoughDaysUsedRatingConditionTest {

    private lateinit var inMemorySiriusRatingDataStore: InMemoryDataStore

    @Before
    fun setUp() {
        this.inMemorySiriusRatingDataStore = InMemoryDataStore()
    }

    @Test
    fun test_condition_is_satisfied_when_the_app_was_used_enough_days() {
        // Create the condition where we require the app to be used for at least 2 days.
        val enoughDaysUsedRatingCondition = EnoughDaysUsedRatingCondition(totalDaysRequired = 2u)

        // Set the first use date to 3 days ago.
        this.inMemorySiriusRatingDataStore.firstUseDate = LocalDateTime.now().minusDays(3).toInstant(ZoneOffset.UTC)

        // The condition should be satisfied, because the total days required is 2 and the date the app was used
        // for the first time 3 days ago.
        assertTrue(enoughDaysUsedRatingCondition.isSatisfied(dataStore = this.inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_not_satisfied_when_first_use_date_is_null() {
        // Create the condition where we require the app to be used for at least 2 days.
        val enoughDaysUsedRatingCondition = EnoughDaysUsedRatingCondition(totalDaysRequired = 2u)

        // Set our first use date to null.
        this.inMemorySiriusRatingDataStore.firstUseDate = null

        // The condition should not be satisfied, because we require `firstUseDate` to exist.
        assertFalse(enoughDaysUsedRatingCondition.isSatisfied(dataStore = this.inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_not_satisfied_when_app_was_not_used_enough_days() {
        // Create the condition where we require the app to be used for at least 2 days.
        val enoughDaysUsedRatingCondition = EnoughDaysUsedRatingCondition(totalDaysRequired = 2u)

        // Set the first use date to 1 days ago.
        this.inMemorySiriusRatingDataStore.firstUseDate = LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC)

        // The condition should not be satisfied, because the total days used required to satisfy is 2 and the date the app was used
        // for the first time was 1 days ago.
        assertFalse(enoughDaysUsedRatingCondition.isSatisfied(dataStore = this.inMemorySiriusRatingDataStore))
    }

}