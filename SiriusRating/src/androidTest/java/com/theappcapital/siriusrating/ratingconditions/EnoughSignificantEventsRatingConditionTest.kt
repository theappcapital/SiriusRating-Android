package com.theappcapital.siriusrating.ratingconditions

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.theappcapital.siriusrating.datastores.InMemoryDataStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class EnoughSignificantEventsRatingConditionTest {

    private lateinit var inMemorySiriusRatingDataStore: InMemoryDataStore

    @Before
    fun setUp() {
        this.inMemorySiriusRatingDataStore = InMemoryDataStore()
    }

    @Test
    fun test_condition_is_satisfied_when_significant_event_count_is_equal_or_greater_than_the_required_amount_of_significant_events() {
        // Create the condition where we require the user to have done at least 5 significant events.
        val enoughSignificantEventsRatingCondition = EnoughSignificantEventsRatingCondition(significantEventsRequired = 5u)

        // Set our significant use count to 5, equal to the required significant events (5).
        this.inMemorySiriusRatingDataStore.significantEventCount = 5u
        // Condition should be satisfied, because the user has done 5 significant events.
        assertTrue(enoughSignificantEventsRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))

        // Set our significant use count to be 6, greater than the required significant events (5).
        this.inMemorySiriusRatingDataStore.significantEventCount = 6u
        // The condition should be satisfied, because the user has done 6 significant events.
        assertTrue(enoughSignificantEventsRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_not_satisfied_when_significant_event_count_is_lower_than_the_required_amount_of_significant_events() {
        // Create the condition where we require the user to have done at least 5 significant events.
        val enoughSignificantEventsRatingCondition = EnoughSignificantEventsRatingCondition(significantEventsRequired = 5u)

        // Set our significant use count to 1, lower than the required significant events (5).
        this.inMemorySiriusRatingDataStore.significantEventCount = 1u
        // The condition should not be satisfied, because the total amount of significant events done by the
        // user is 1, where 5 or greater is required to be satisfied.
        assertFalse(enoughSignificantEventsRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

}