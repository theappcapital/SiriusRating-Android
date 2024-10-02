package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.SiriusRatingDataStore
import com.theappcapital.siriusrating.support.versionproviders.AppVersionProvider

/**
 * The rating condition that checks if the user didn't decline the current version of the app.
 * We do not want to prompt the user to rate the app again if it declined to rate the current
 * version of the app.
 *
 * @property appVersionProvider
 */
class NotRatedCurrentVersionRatingCondition(private val appVersionProvider: AppVersionProvider) : RatingCondition {

    /**
     * Validate whether the user didn't already rate the current version of the app.
     *
     * @param dataStore The data from the store to validate the condition.
     * @return `true` when the user didn't rate the current version of the app, else `false`.
     */
    override fun isSatisfied(dataStore: SiriusRatingDataStore): Boolean {
        val ratedUserActions = dataStore.ratedUserActions
        if (ratedUserActions.isEmpty()) {
            // The user didn't decline any rate prompt (yet), return `true`.
            return true
        }

        // The user rated a version of the app:
        // Check if the app version the user rated is equal to the current app version.
        val userRatedCurrentAppVersion = ratedUserActions.any {
            it.appVersion == this.appVersionProvider.appVersion
        }

        // Return `true` when the user didn't rate the current version of the app, else `false`.
        return !userRatedCurrentAppVersion
    }

}