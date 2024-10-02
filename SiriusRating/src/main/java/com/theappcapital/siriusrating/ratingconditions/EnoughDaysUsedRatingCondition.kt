package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.DataStore
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset

class EnoughDaysUsedRatingCondition(private val totalDaysRequired: UInt) : RatingCondition {

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

        // Check if the app has been used long enough.
        val fromDate = LocalDate.from(firstUseDate.atZone(ZoneOffset.UTC)).atStartOfDay()
        val nowDate = LocalDate.now(ZoneOffset.UTC).atStartOfDay()
        val totalDaysUsed = Duration.between(fromDate, nowDate).toDays()

        return totalDaysUsed >= this.totalDaysRequired.toLong()
    }

}