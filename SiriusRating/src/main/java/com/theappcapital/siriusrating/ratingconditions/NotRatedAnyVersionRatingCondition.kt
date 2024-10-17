package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.DataStore
import java.lang.Math.max
import java.lang.Math.pow
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset

class NotRatedAnyVersionRatingCondition(
    private val daysAfterRatingToPromptUserAgain: Int,
    private val backOffFactor: Double? = null,
    private val maxRecurringPromptsAfterRating: Int
) : RatingCondition {

    /**
     * Validate whether the user didn't rate a version of the app. If the user did rate a version
     * of the app, validate if we can show the prompt again by checking the amount of days should
     * have passed after the user rated.
     *
     * @param dataStore
     * @return `true` if the user did not opt-in, or if the user did opted-in for a 'Remind me later' and the time
     * has come to remind the user. If any of these conditions fail, return `false`.
     */
    override fun isSatisfied(dataStore: DataStore): Boolean {
        val ratedUserActions = dataStore.ratedUserActions
        // If the the user didn't rate the app (yet), return `true`.
        val mostRecentRateUserAction = ratedUserActions.maxByOrNull { it.date } ?: return true

        // Minus one, because we want to know how many times the user rated after it initially rated.
        if ((ratedUserActions.count() - 1) > this.maxRecurringPromptsAfterRating.toInt()) {
            // We reached the maximum amount of times the user can rate, we do not want
            // to show the prompt anymore at this point, return `false`.
            return false
        }

        // Check if the app was used long enough after the most recent rate action:
        // 1. Get the total amount of days since the user last rated until now.
        val fromDate = LocalDate.from(mostRecentRateUserAction.date.atZone(ZoneOffset.UTC)).atStartOfDay()
        val nowDate = LocalDate.now(ZoneOffset.UTC).atStartOfDay()
        val totalDaysAfterUserRatedLast = Duration.between(fromDate, nowDate).toDays().toInt()

        val totalDaysAfterRatingToPromptUserAgain = this.calculatedTotalDaysToPromptUserAgain(
            daysAfterRatingToPromptUserAgain = this.daysAfterRatingToPromptUserAgain,
            backOffFactor = this.backOffFactor,
            timesRated = ratedUserActions.count().toInt()
        )

        // Check if the total days after the user last rated is greater than or equal to the total days
        // needed to 'wait' to show the prompt again. For example: The user rated last 3 days ago and
        // the total days after rating to show the prompt again is 7 days, then the prompt should not be shown.
        return totalDaysAfterUserRatedLast >= totalDaysAfterRatingToPromptUserAgain
    }

    fun calculatedTotalDaysToPromptUserAgain(
        daysAfterRatingToPromptUserAgain: Int,
        backOffFactor: Double?,
        timesRated: Int
    ): Int {
        // The `backOffFactor` must exist in order to make the back off calculation. If it does
        // not exist, return with the original amount of days.
        val backOffFactor = backOffFactor ?: return daysAfterRatingToPromptUserAgain

        // Formula: {days after rating to prompt user again} * ({back off factor} ^ ({times rated - 1}).
        // For example if the `daysAfterRatingToPromptUserAgain` is '7' and `backOffFactor` is '2.0', it will
        // calculate [7-days, 14-days, 28-days, …] for the accumulating amount of rates.
        return (daysAfterRatingToPromptUserAgain.toDouble() * pow(backOffFactor, max(0, timesRated.toInt() - 1).toDouble())).toInt()
    }

}