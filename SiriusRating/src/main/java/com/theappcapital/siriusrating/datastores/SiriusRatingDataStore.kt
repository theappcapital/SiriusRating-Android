package com.theappcapital.siriusrating.datastores

import com.theappcapital.siriusrating.SiriusRatingUserAction
import java.time.Instant

interface SiriusRatingDataStore {

    // The date the first app session took place.
    var firstUseDate: Instant?

    /// The total amount of app sessions.
    var appSessionsCount: UInt

    /// The total amount of significant events done.
    var significantEventCount: UInt

    /// Represents the previous or the current app version. This is used to check
    /// if we need to reset the counters.
    var previousOrCurrentAppVersion: String?

    /// The action that determines whether and when the user pressed the button to remind it later
    /// and on which app version that occurred.
    var optedInForReminderUserActions: List<SiriusRatingUserAction>

    /// The action that determines whether and when the user left a review
    /// and on which app version that occurred.
    var ratedUserActions: List<SiriusRatingUserAction>

    /// The action that determines whether and when the user declined a review request prompt
    /// and on which app version that occurred.
    var declinedToRateUserActions: List<SiriusRatingUserAction>

}