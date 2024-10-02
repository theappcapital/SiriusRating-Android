package com.theappcapital.siriusrating.ratingconditions

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.theappcapital.siriusrating.UserAction
import com.theappcapital.siriusrating.datastores.InMemoryDataStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneOffset

@RunWith(AndroidJUnit4ClassRunner::class)
class NotPostponedDueToReminderRatingConditionTest {

    private lateinit var inMemorySiriusRatingDataStore: InMemoryDataStore

    @Before
    fun setUp() {
        this.inMemorySiriusRatingDataStore = InMemoryDataStore()
    }

    @Test
    fun test_condition_is_satisfied_when_the_user_did_not_opt_in_for_reminder() {
        val remindMeLaterRatingCondition = NotPostponedDueToReminderRatingCondition(totalDaysBeforeReminding = 1u)

        // By default the user did not opted-in for a reminder.
        // The condition should be satisfied, because the user did not opt-in for a reminder.
        assertTrue(remindMeLaterRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))

        // Just an extra check: Set the user actions to an empty list (which is should already be by default).
        // Setting it to an empty list means it has not (yet) opted-in for a reminder.
        this.inMemorySiriusRatingDataStore.optedInForReminderUserActions = listOf()

        // The condition should be satisfied, because the user did not opt-in for a reminder.
        assertTrue(remindMeLaterRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_satisfied_when_the_user_opted_in_for_a_reminder_and_days_before_reminding_was_reached() {
        // Create the condition where we set the total days before reminding to 1.
        val remindMeLaterRatingCondition = NotPostponedDueToReminderRatingCondition(totalDaysBeforeReminding = 1u)

        // Set the action where the user did opt-in for a reminder yesterday.
        val dateTheUserOptedInForAReminder = LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC)
        this.inMemorySiriusRatingDataStore.optedInForReminderUserActions = listOf(UserAction(appVersion = "0.1-anyversion", date = dateTheUserOptedInForAReminder))

        // The condition should be satisfied, because the user opted-in for a reminder yesterday and the total days
        // before reminding is 1: The date 'today' (now) and yesterday is a 1 day difference.
        assertTrue(remindMeLaterRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_not_satisfied_when_the_user_opted_in_for_reminder_but_days_before_reminding_was_not_reached() {
        // Create the condition where we set the total days before reminding to 2.
        val totalDaysBeforeReminding = 2u
        val remindMeLaterRatingCondition = NotPostponedDueToReminderRatingCondition(totalDaysBeforeReminding = totalDaysBeforeReminding)

        // Create a random date that is 'now' or ((24 * totalDaysBeforeReminding) - 1) hours in the past.
        // For example: If the `totalDaysBeforeReminding` is 2, it will create a date in the past between 0 and 47 hours.
        val dateTheUserOptedInForAReminder = LocalDateTime.now().minusHours((0 until (24 * totalDaysBeforeReminding.toInt() - 1)).random().toLong()).toInstant(ZoneOffset.UTC)
        this.inMemorySiriusRatingDataStore.optedInForReminderUserActions = listOf(UserAction(appVersion = "0.1-anyversion", date = dateTheUserOptedInForAReminder))

        // The condition should not be satisfied, because the user opted-in for a reminder between 0 and 47 hours ago and
        // the total days before reminding is 2 (48 hours): The total days before reminding was not (yet) reached.
        assertFalse(remindMeLaterRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

}