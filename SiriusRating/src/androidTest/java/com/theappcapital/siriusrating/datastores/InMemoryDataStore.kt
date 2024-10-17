package com.theappcapital.siriusrating.datastores

import com.theappcapital.siriusrating.UserAction
import java.time.Instant

class InMemoryDataStore : DataStore {

    override var firstUseDate: Instant? = null

    override var appSessionsCount: Int = 0

    override var significantEventCount: Int = 0

    override var previousOrCurrentAppVersion: String? = null

    override var optedInForReminderUserActions: List<UserAction> = listOf()

    override var ratedUserActions: List<UserAction> = listOf()

    override var declinedToRateUserActions: List<UserAction> = listOf()

}