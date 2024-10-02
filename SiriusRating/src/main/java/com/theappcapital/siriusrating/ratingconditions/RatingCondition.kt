package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.SiriusRatingDataStore

interface RatingCondition {

    // Validate the condition.
    //
    // - Parameter dataStore: Use the data from the store to validate the condition.
    // - Returns: `true` when the condition is valid, else `false`.
    fun isSatisfied(dataStore: SiriusRatingDataStore): Boolean

}
