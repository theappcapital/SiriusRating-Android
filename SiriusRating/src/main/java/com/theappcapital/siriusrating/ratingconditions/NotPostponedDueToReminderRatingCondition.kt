package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.DataStore
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * @property totalDaysBeforeReminding Once the rating alert is presented to the user, they might select
 * 'Remind me later'. This value specifies how many days Armchair will wait before reminding them.
 * A value of 0 disables reminders and removes the 'Remind me later' button.
 */
class NotPostponedDueToReminderRatingCondition(private val totalDaysBeforeReminding: UInt) : RatingCondition {

    /**
     * Validate whether the rating prompt is not postponed due to the opted-in reminder.
     *
     * @param dataStore
     * @return `true` if the user did not opt-in, or if the user did opted-in for a 'Remind me later' and the time
     * has come to remind the user. If any of these conditions fail, return `false`.
     */
    override fun isSatisfied(dataStore: DataStore): Boolean {
        // Check if the user opted-in for reminding it later.
        val mostRecentRemindMeLaterUserAction = dataStore.optedInForReminderUserActions.maxByOrNull { it.date } ?: run {
            // The user did not opt-in for a reminder, return `true`.
            return true
        }

        // Check if the app was used long enough after the user opted-in for the 'Remind me later'.
        val lastDateTheUserOptedInForReminder = mostRecentRemindMeLaterUserAction.date

        val fromDate = LocalDate.from(lastDateTheUserOptedInForReminder.atZone(ZoneOffset.UTC)).atStartOfDay()
        val nowDate = LocalDate.now(ZoneOffset.UTC).atStartOfDay()
        val totalDaysAfterUserOptedInForReminder = Duration.between(fromDate, nowDate).toDays().toUInt()

        return totalDaysAfterUserOptedInForReminder >= this.totalDaysBeforeReminding
    }

}