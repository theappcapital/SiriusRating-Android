package com.theappcapital.siriusrating.datastores

import com.theappcapital.siriusrating.UserAction
import java.time.Instant

class InMemoryDataStore : DataStore {

    override var firstUseDate: Instant? = null

    override var appSessionsCount: UInt = 0u

    override var significantEventCount: UInt = 0u

    override var previousOrCurrentAppVersion: String? = null

    override var optedInForReminderUserActions: List<UserAction> = listOf()

    override var ratedUserActions: List<UserAction> = listOf()

    override var declinedToRateUserActions: List<UserAction> = listOf()

}