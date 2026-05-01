package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.DataStore
import java.time.Duration
import java.time.Instant

class EnoughDaysUsedRatingCondition(private val totalDaysRequired: Int) : RatingCondition {

    /**
     * Validate whether the app is used long enough.
     *
     * @param dataStore
     * @return `true` if the app was used long enough. `false` if the
     * `firstUseDate` is nil or when the app was not used long enough.
     */
    override fun isSatisfied(dataStore: DataStore): Boolean {
        // The `firstUseDate` must exist, if not return `false`.
        val firstUseDate = dataStore.firstUseDate ?: return false

        // Elapsed full 24-hour periods since first use.
        val totalDaysUsed = Duration.between(firstUseDate, Instant.now()).toDays()

        return totalDaysUsed >= this.totalDaysRequired.toLong()
    }

}
