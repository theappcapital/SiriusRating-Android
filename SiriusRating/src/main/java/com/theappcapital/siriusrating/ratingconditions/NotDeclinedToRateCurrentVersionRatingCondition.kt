package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.DataStore
import com.theappcapital.siriusrating.support.versionproviders.AppVersionProvider

/**
 * The rating condition that checks if the user didn't decline the current version of the app.
 * We do not want to prompt the user to rate the app again if it declined to rate the current
 * version of the app.
 *
 * @property appVersionProvider
 */
class NotDeclinedToRateCurrentVersionRatingCondition(private val appVersionProvider: AppVersionProvider) : RatingCondition {

    /**
     * Validate whether the rating prompt is not postponed due to the opted-in reminder.
     *
     * @param dataStore
     * @return `true` if the user did not opt-in, or if the user did opted-in for a 'Remind me later' and the time
     * has come to remind the user. If any of these conditions fail, return `false`.
     */
    override fun isSatisfied(dataStore: DataStore): Boolean {
        val declinedToRateUserActions = dataStore.declinedToRateUserActions
        if (declinedToRateUserActions.isEmpty()) {
            // The user didn't decline any rate prompt (yet), return `true`.
            return true
        }

        // Check if the app was used long enough after the user opted-in for the 'Remind me later'.
        val userDeclinedToRateCurrentAppVersion = declinedToRateUserActions.any { it.appVersion == this.appVersionProvider.appVersion }

        return !userDeclinedToRateCurrentAppVersion
    }

}