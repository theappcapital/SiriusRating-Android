package com.theappcapital.siriusrating.ratingconditions

import com.theappcapital.siriusrating.datastores.DataStore

/**
 * A rating condition defined by a closure. Use this for simple, inline conditions
 * that don't warrant a dedicated class.
 */
class ClosureRatingCondition(private val closure: (dataStore: DataStore) -> Boolean) : RatingCondition {

    override fun isSatisfied(dataStore: DataStore): Boolean = this.closure(dataStore)

}
