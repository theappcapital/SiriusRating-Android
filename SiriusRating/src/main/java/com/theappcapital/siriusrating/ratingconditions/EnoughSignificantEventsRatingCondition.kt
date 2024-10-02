package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.SiriusRatingDataStore

class EnoughSignificantEventsRatingCondition(private val significantEventsRequired: UInt) : RatingCondition {

    /**
     * Validate whether the user has done enough significant events.
     *
     * @param dataStore
     * @return `true` if the user has done enough significant events, else `false`.
     */
    override fun isSatisfied(dataStore: SiriusRatingDataStore): Boolean {
        // The total amount of significant events done by the user.
        val significantEventCount = dataStore.significantEventCount

        return significantEventCount >= this.significantEventsRequired
    }

}