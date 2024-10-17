package com.theappcapital.siriusrating.datastores

import com.theappcapital.siriusrating.UserAction
import java.time.Instant

interface DataStore {

    // The date the first app session took place.
    var firstUseDate: Instant?

    /// The total amount of app sessions.
    var appSessionsCount: Int

    /// The total amount of significant events done.
    var significantEventCount: Int

    /// Represents the previous or the current app version. This is used to check
    /// if we need to reset the counters.
    var previousOrCurrentAppVersion: String?

    /// The action that determines whether and when the user pressed the button to remind it later
    /// and on which app version that occurred.
    var optedInForReminderUserActions: List<UserAction>

    /// The action that determines whether and when the user left a review
    /// and on which app version that occurred.
    var ratedUserActions: List<UserAction>

    /// The action that determines whether and when the user declined a review request prompt
    /// and on which app version that occurred.
    var declinedToRateUserActions: List<UserAction>

}