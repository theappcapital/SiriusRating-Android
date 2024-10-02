package com.theappcapital.siriusrating

import android.app.Activity
import com.theappcapital.siriusrating.datastores.SharedPreferencesDataStore
import com.theappcapital.siriusrating.datastores.DataStore
import com.theappcapital.siriusrating.prompts.presenters.GooglePlayRatePromptPresenter
import com.theappcapital.siriusrating.prompts.presenters.RatePromptPresenter
import com.theappcapital.siriusrating.prompts.presenters.RequestToRatePromptPresenter
import com.theappcapital.siriusrating.prompts.presenters.StyleOneRequestToRatePromptPresenter
import com.theappcapital.siriusrating.ratingconditions.EnoughAppSessionsRatingCondition
import com.theappcapital.siriusrating.ratingconditions.EnoughDaysUsedRatingCondition
import com.theappcapital.siriusrating.ratingconditions.EnoughSignificantEventsRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotDeclinedToRateAnyVersionRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotPostponedDueToReminderRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotRatedAnyVersionRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotRatedCurrentVersionRatingCondition
import com.theappcapital.siriusrating.ratingconditions.RatingCondition
import com.theappcapital.siriusrating.support.versionproviders.AppVersionProvider
import com.theappcapital.siriusrating.support.versionproviders.PackageInfoCompatAppVersionProvider

class SiriusRatingBuilder(val activity: Activity) {

    private var appVersionProvider: AppVersionProvider? = null

    private var dataStore: DataStore? = null

    private var requestToRatePromptPresenter: RequestToRatePromptPresenter? = null

    private var ratePromptPresenter: RatePromptPresenter? = null

    var debugEnabled: Boolean = false
        private set

    var ratingConditions: List<RatingCondition> = listOf(
        EnoughDaysUsedRatingCondition(totalDaysRequired = 30u),
        EnoughAppSessionsRatingCondition(totalAppSessionsRequired = 15u),
        EnoughSignificantEventsRatingCondition(significantEventsRequired = 20u),
        NotPostponedDueToReminderRatingCondition(totalDaysBeforeReminding = 7u),
        // NotDeclinedToRateCurrentVersionRatingCondition(),
        NotDeclinedToRateAnyVersionRatingCondition(daysAfterDecliningToPromptUserAgain = 30u, backOffFactor = 2.0, maxRecurringPromptsAfterDeclining = 2u),
        NotRatedCurrentVersionRatingCondition(appVersionProvider = PackageInfoCompatAppVersionProvider(context = activity)),
        NotRatedAnyVersionRatingCondition(daysAfterRatingToPromptUserAgain = 240u, maxRecurringPromptsAfterRating = UInt.MAX_VALUE)
    )
        private set

    var appName: String? = null
        private set

    var canPromptUserToRateOnLaunch: Boolean = false
        private set

    var canOptInForReminder: Boolean = true
        private set

    var didOptInForReminderHandler: (() -> Unit)? = null
        private set

    var didDeclineToRateHandler: (() -> Unit)? = null
        private set

    var didAgreeToRateHandler: (() -> Unit)? = null
        private set

    var needsResetTrackers: ((DataStore, AppVersionProvider) -> Boolean) = { _, _ ->
        false
    }

    fun appVersionProvider(appVersionProvider: AppVersionProvider) = apply {
        this.appVersionProvider = appVersionProvider
    }

    fun dataStore(dataStore: DataStore) = apply {
        this.dataStore = dataStore
    }

    fun requestToRatePromptPresenter(requestToRatePromptPresenter: RequestToRatePromptPresenter) = apply {
        this.requestToRatePromptPresenter = requestToRatePromptPresenter
    }

    fun ratePromptPresenter(ratePromptPresenter: RatePromptPresenter) = apply {
        this.ratePromptPresenter = ratePromptPresenter
    }

    fun debugEnabled(debugEnabled: Boolean) = apply {
        this.debugEnabled = debugEnabled
    }

    fun ratingConditions(vararg ratingConditions: RatingCondition) = apply {
        this.ratingConditions = ratingConditions.toList()
    }

    fun appName(appName: String) = apply {
        this.appName = appName
    }

    fun canPromptUserToRateOnLaunch(canPromptUserToRateOnLaunch: Boolean) = apply {
        this.canPromptUserToRateOnLaunch = canPromptUserToRateOnLaunch
    }

    fun canOptInForReminder(canOptInForReminder: Boolean) = apply {
        this.canOptInForReminder = canOptInForReminder
    }

    fun didOptInForReminderHandler(didOptInForReminderHandler: (() -> Unit)?) = apply {
        this.didOptInForReminderHandler = didOptInForReminderHandler
    }

    fun didDeclineToRateHandler(didDeclineToRateHandler: (() -> Unit)?) = apply {
        this.didDeclineToRateHandler = didDeclineToRateHandler
    }

    fun didAgreeToRateHandler(didAgreeToRateHandler: (() -> Unit)?) = apply {
        this.didAgreeToRateHandler = didAgreeToRateHandler
    }

    fun getAppVersionProvider(): AppVersionProvider {
        return this.appVersionProvider ?: PackageInfoCompatAppVersionProvider(context = activity)
    }

    fun getDataStore(): DataStore {
        return this.dataStore ?: SharedPreferencesDataStore(context = activity)
    }

    fun getRequestToRatePromptPresenter(): RequestToRatePromptPresenter {
        return this.requestToRatePromptPresenter ?: StyleOneRequestToRatePromptPresenter(activity = activity, appName = appName, canOptInForReminder = canOptInForReminder)
    }

    fun getRatePromptPresenter(): RatePromptPresenter {
        return this.ratePromptPresenter ?: GooglePlayRatePromptPresenter(activity = activity)
    }

    fun build() = SiriusRating(builder = this)

}