package com.theappcapital.siriusrating.ratingconditions

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.theappcapital.siriusrating.UserAction
import com.theappcapital.siriusrating.datastores.InMemoryDataStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@RunWith(AndroidJUnit4ClassRunner::class)
class NotDeclinedToRateAnyVersionRatingConditionTest {

    private lateinit var inMemorySiriusRatingDataStore: InMemoryDataStore

    @Before
    fun setUp() {
        this.inMemorySiriusRatingDataStore = InMemoryDataStore()
    }

    @Test
    fun test_condition_is_satisfied_when_the_user_did_not_decline_to_rate_any_version_of_the_app() {
        val notDeclinedToRateAnyVersionRatingCondition = NotDeclinedToRateAnyVersionRatingCondition(daysAfterDecliningToPromptUserAgain = 180, maxRecurringPromptsAfterDeclining = 1)

        // By default the user has not declined the rating prompt.
        // The condition should be satisfied, because the user did not decline the rating prompt.
        assertTrue(notDeclinedToRateAnyVersionRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))

        // Just an extra check: Set the user actions to an empty list (which it should already be by default).
        // Setting it to an empty list means the user has not (yet) declined.
        this.inMemorySiriusRatingDataStore.declinedToRateUserActions = listOf()

        // The condition should be satisfied, because the user did not decline.
        assertTrue(notDeclinedToRateAnyVersionRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_satisfied_when_the_user_did_decline_and_the_days_to_show_prompt_have_passed() {
        // Create a condition where the user can be prompted again after 180 days when it declined.
        val notDeclinedToRateAnyVersionRatingCondition = NotDeclinedToRateAnyVersionRatingCondition(daysAfterDecliningToPromptUserAgain = 180, maxRecurringPromptsAfterDeclining = 1)

        // Set the user actions where the user declined to rate the app 180 days ago (180 days is required to show the prompt again).
        this.inMemorySiriusRatingDataStore.declinedToRateUserActions = listOf(UserAction(appVersion = "0.1-version", date = LocalDateTime.now().minusDays(180).toInstant(ZoneOffset.UTC)))

        // The condition should be satisfied, because the total amount of days (180) have passed since the user declined last.
        assertTrue(notDeclinedToRateAnyVersionRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_not_satisfied_when_the_max_amount_of_recurring_prompts_was_reached() {
        // Create a condition where the user can only be prompted 2 times after it initially got prompted and declined.
        val notDeclinedToRateAnyVersionRatingCondition = NotDeclinedToRateAnyVersionRatingCondition(daysAfterDecliningToPromptUserAgain = 10, maxRecurringPromptsAfterDeclining = 2)

        // Set the user actions where the user already declined 3 times.
        this.inMemorySiriusRatingDataStore.declinedToRateUserActions = listOf(
            UserAction(appVersion = "0.1-version", date = Instant.now()),
            UserAction(appVersion = "0.2-version", date = Instant.now()),
            UserAction(appVersion = "0.3-version", date = Instant.now())
        )

        // The condition should not be satisfied, because we reached the max amount of recurring prompts (3 is greater than 2).
        assertFalse(notDeclinedToRateAnyVersionRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_not_satisfied_because_user_did_decline_but_the_days_to_show_the_recurring_prompt_have_not_yet_passed() {
        // Create a condition where the user can be prompted again after 180 days when it declined.
        val notDeclinedToRateAnyVersionRatingCondition = NotDeclinedToRateAnyVersionRatingCondition(daysAfterDecliningToPromptUserAgain = 180, maxRecurringPromptsAfterDeclining = 1)

        // The user declined to rate the app 90 days ago (180 days is required to show the prompt again).
        this.inMemorySiriusRatingDataStore.declinedToRateUserActions = listOf(UserAction(appVersion = "0.1-version", date = LocalDateTime.now().minusDays(90).toInstant(ZoneOffset.UTC)))

        // The condition is should not be satisfied; the user did decline to rate, but only a total of 90 days have
        // passed instead of the 180 days required to show the prompt.
        assertFalse(notDeclinedToRateAnyVersionRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

}
