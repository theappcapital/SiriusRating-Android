package com.theappcapital.siriusrating.datastores

import com.theappcapital.siriusrating.SiriusRatingUserAction
import java.time.Instant

class InMemorySiriusRatingDataStore : SiriusRatingDataStore {

    override var firstUseDate: Instant? = null

    override var appSessionsCount: UInt = 0u

    override var significantEventCount: UInt = 0u

    override var previousOrCurrentAppVersion: String? = null

    override var optedInForReminderUserActions: List<SiriusRatingUserAction> = listOf()

    override var ratedUserActions: List<SiriusRatingUserAction> = listOf()

    override var declinedToRateUserActions: List<SiriusRatingUserAction> = listOf()

}