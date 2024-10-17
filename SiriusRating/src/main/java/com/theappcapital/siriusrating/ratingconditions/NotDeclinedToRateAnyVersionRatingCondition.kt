package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.DataStore
import java.lang.Math.max
import java.lang.Math.pow
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset

class NotDeclinedToRateAnyVersionRatingCondition(
    private val daysAfterDecliningToPromptUserAgain: Int,
    private val backOffFactor: Double? = null,
    private val maxRecurringPromptsAfterDeclining: Int
) : RatingCondition {

    /**
     * Validate whether the rating prompt is not postponed due to the opted-in reminder.
     *
     * @param dataStore
     * @return `true` if the user did not opt-in, or if the user did opted-in for a 'Remind me later' and the time
     * has come to remind the user. If any of these conditions fail, return `false`.
     */
    override fun isSatisfied(dataStore: DataStore): Boolean {
        val declinedToRateUserActions = dataStore.declinedToRateUserActions
        // If the the user didn't decline to rate the app (yet), return `true`.
        val mostRecentDeclinedToRateUserAction = declinedToRateUserActions.maxByOrNull { it.date } ?: return true

        if ((declinedToRateUserActions.count() - 1) > this.maxRecurringPromptsAfterDeclining) {
            // We reached the maximum amount of times the user can decline, we do not want
            // to show the prompt anymore at this point, return `false`.
            return false
        }

        // Check if the app was used long enough after the most recent decline action:
        // 1. Get the total amount of days since the user last declined until now.
        val fromDate = LocalDate.from(mostRecentDeclinedToRateUserAction.date.atZone(ZoneOffset.UTC)).atStartOfDay()
        val nowDate = LocalDate.now(ZoneOffset.UTC).atStartOfDay()
        val totalDaysAfterUserDeclinedLast = Duration.between(fromDate, nowDate).toDays().toInt()

        // 2. Calculate the number of days it takes to show the prompt again after the user declined.
        val totalDaysAfterDecliningToPromptUserAgain = this.calculatedTotalDaysToPromptUserAgain(
            daysAfterDecliningToPromptUserAgain = this.daysAfterDecliningToPromptUserAgain,
            backOffFactor = this.backOffFactor,
            timesDeclined = declinedToRateUserActions.count()
        )

        // Check if the total days after the user declined is greater than or equal
        // to the total days we needed to wait to show the prompt again.
        return totalDaysAfterUserDeclinedLast >= totalDaysAfterDecliningToPromptUserAgain
    }

    fun calculatedTotalDaysToPromptUserAgain(
        daysAfterDecliningToPromptUserAgain: Int,
        backOffFactor: Double?,
        timesDeclined: Int
    ): Int {
        // The `backOffFactor` must exist in order to make the back off calculation. If it does
        // not exist, return with the original amount of days.
        val backOffFactor = backOffFactor ?: return daysAfterDecliningToPromptUserAgain

        // Formula: {days after declining to prompt user again} * ({back off factor} ^ ({times declined - 1}).
        // For example if the `daysAfterDecliningToPromptUserAgain` is '7' and `backOffFactor` is '2.0', it will
        // calculate [7-days, 14-days, 28-days, …] for the accumulating amount of declines.
        return (daysAfterDecliningToPromptUserAgain.toDouble() * pow(backOffFactor, max(0, timesDeclined - 1).toDouble())).toInt()
    }

}