package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.DataStore

/**
 * The rating conditions that checks if the app has been used long enough.
 *
 * @property totalAppSessionsRequired An example of a 'use' would be if the user launched the app. Bringing the app
 * into the foreground (on devices that support it) would also be considered a 'use'. Users need to 'use' the app this many times before
 * before they will be prompted to rate it.
 */
class EnoughAppSessionsRatingCondition(private val totalAppSessionsRequired: UInt) : RatingCondition {

    /**
     * Validate whether the app has been used enough times.
     *
     * @param dataStore
     * @return `true` if the app as been used enough times, else `false`.
     */
    override fun isSatisfied(dataStore: DataStore): Boolean {
        // The total amount of times the app has been opened.
        val appSessionsCount = dataStore.appSessionsCount

        return appSessionsCount >= this.totalAppSessionsRequired
    }

}