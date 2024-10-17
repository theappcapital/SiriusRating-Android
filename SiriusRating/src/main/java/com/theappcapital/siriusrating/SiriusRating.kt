package com.theappcapital.siriusrating

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.theappcapital.siriusrating.datastores.DataStore
import com.theappcapital.siriusrating.prompts.presenters.RatePromptPresenter
import com.theappcapital.siriusrating.prompts.presenters.RequestToRatePromptPresenter
import com.theappcapital.siriusrating.ratingconditions.RatingCondition
import com.theappcapital.siriusrating.support.versionproviders.AppVersionProvider
import java.time.Instant


fun setupSiriusRating(activity: Activity, block: SiriusRatingBuilder.() -> Unit) = SiriusRating.setup(activity = activity, block = block)

class SiriusRating {

    private val appVersionProvider: AppVersionProvider

    val dataStore: DataStore

    private val requestToRatePromptPresenter: RequestToRatePromptPresenter

    private val ratePromptPresenter: RatePromptPresenter

    private val debugEnabled: Boolean

    private val ratingConditions: List<RatingCondition>

    private val canPromptUserToRateOnLaunch: Boolean

    private val didOptInForReminderHandler: (() -> Unit)?

    private val didDeclineToRateHandler: (() -> Unit)?

    private val didAgreeToRateHandler: (() -> Unit)?

    private val needsResetTrackers: ((DataStore, AppVersionProvider) -> Boolean)

    val ratingConditionsHaveBeenMet: Boolean
        get() {
            if (this.ratingConditions.isEmpty()) {
                this.debugLog("No rating conditions have been found to validate.")
                // We have no rating conditions to validate, return `true`.
                return true
            }

            return this.ratingConditions.all { ratingCondition ->
                val isRatingConditionValid = ratingCondition.isSatisfied(this.dataStore)

                if (!isRatingConditionValid) {
                    this.debugLog("The `${ratingCondition::class.simpleName}` was not satisfied.")
                }

                isRatingConditionValid
            }
        }

    constructor(activity: Activity) : this(defaultBuilder(activity = activity))

    constructor(builder: SiriusRatingBuilder) {
        this.appVersionProvider = builder.getAppVersionProvider()
        this.dataStore = builder.getDataStore()
        this.requestToRatePromptPresenter = builder.getRequestToRatePromptPresenter()
        this.ratePromptPresenter = builder.getRatePromptPresenter()
        this.debugEnabled = builder.debugEnabled
        this.ratingConditions = builder.ratingConditions
        this.canPromptUserToRateOnLaunch = builder.canPromptUserToRateOnLaunch
        this.didOptInForReminderHandler = builder.didOptInForReminderHandler
        this.didDeclineToRateHandler = builder.didDeclineToRateHandler
        this.didAgreeToRateHandler = builder.didAgreeToRateHandler
        this.needsResetTrackers = builder.needsResetTrackers
    }

    constructor(activity: Activity, block: SiriusRatingBuilder.() -> SiriusRatingBuilder) : this(SiriusRatingBuilder(activity).apply { block() })

    init {
        this.setupObservers()
    }

    private fun setupObservers() {
        Handler(Looper.getMainLooper()).post {
            ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

                /**
                 * Notifies that `ON_START` event occurred.
                 * This method will be called after the [LifecycleOwner]'s `onStart` method returns.
                 *
                 * @param owner the component, whose state was changed
                 */
                override fun onStart(owner: LifecycleOwner) {
                    userDidLaunchApp()
                }

            })
        }
    }

    fun userDidSignificantEvent(canPromptUserToRate: Boolean = true) {
        if (canPromptUserToRate) {
            this.incrementSignificantEventAndRate()
        } else {
            this.incrementSignificantEventCount()
        }
    }

    fun userDidLaunchApp() {
        if (this.canPromptUserToRateOnLaunch) {
            this.incrementAppSessionsCountAndRate()
        } else {
            this.incrementAppSessionsCount()
        }
    }

    /// Called before incrementing any usage counter.
    private fun beforeIncrementingUsageCounter() {
        // 1. Check if we need to reset all the trackers. For example: An app publisher may want to reset
        // the usage counters when the app is on a different (new major) version.
        this.resetTrackersIfNeeded()

        // 2. Reset the app version if we are on a different version than the previous stored version.
        if (this.dataStore.previousOrCurrentAppVersion != this.appVersionProvider.appVersion) {
            this.dataStore.previousOrCurrentAppVersion = this.appVersionProvider.appVersion
        }

        // 3. If the first use date is not (yet) set, set it to 'now'.
        if (this.dataStore.firstUseDate == null) {
            this.dataStore.firstUseDate = Instant.now()
        }
    }

    private fun incrementAppSessionsCount() {
        this.beforeIncrementingUsageCounter()

        // Increment the app session count.
        this.dataStore.appSessionsCount += 1

        this.debugLog("Incremented app session count to: ${this.dataStore.appSessionsCount}.")
        this.debugLog("Currently stored data: ${this.dataStore}.")
    }

    private fun incrementSignificantEventCount() {
        this.beforeIncrementingUsageCounter()

        // Increment the significant event count.
        this.dataStore.significantEventCount += 1

        this.debugLog("Incremented significant event count to: ${this.dataStore.significantEventCount}.")
        this.debugLog("Currently stored data: ${this.dataStore}.")
    }

    private fun incrementSignificantEventAndRate() {
        this.incrementSignificantEventCount()
        this.showRequestRatePromptIfConditionsHaveBeenMet()
    }

    private fun incrementAppSessionsCountAndRate() {
        this.incrementAppSessionsCount()
        this.showRequestRatePromptIfConditionsHaveBeenMet()
    }

    fun resetUsageTrackers() {
        this.dataStore.firstUseDate = null
        this.dataStore.appSessionsCount = 0
        this.dataStore.significantEventCount = 0

        this.debugLog("Resetted usage counters.")
    }

    fun resetUserActions() {
        this.dataStore.ratedUserActions = listOf()
        this.dataStore.optedInForReminderUserActions = listOf()
        this.dataStore.declinedToRateUserActions = listOf()

        this.debugLog("Resetted user actions.")
    }

    /// Reset all values that are tracked to return to it's initial state.
    fun resetAllTrackers() {
        this.resetUsageTrackers()
        this.resetUserActions()

        this.dataStore.previousOrCurrentAppVersion = null

        this.debugLog("Resetted all trackers.")
    }

    private fun resetTrackersIfNeeded() {
        if (this.needsResetTrackers(this.dataStore, this.appVersionProvider)) {
            this.resetAllTrackers()
        }
    }

    private fun showRequestRatePromptIfConditionsHaveBeenMet() {
        if (this.ratingConditionsHaveBeenMet) {
            this.debugLog("All rating conditions have been met, show the prompt that requests the user to rate.")

            this.showRequestToRatePrompt()
        }
    }

    fun showRequestToRatePrompt() {
        // This is silly, but it sometimes bugs the dialog when
        // immediately showing the dialog after dismissing an activity.
        Handler(Looper.getMainLooper()).postDelayed(1000) {
            this.requestToRatePromptPresenter.show(
                didAgreeToRateHandler = {
                    this.showRatePrompt()

                    // For now just assume that the user rated.
                    val ratedUserAction = UserAction(appVersion = this.appVersionProvider.appVersion, date = Instant.now())
                    this.dataStore.ratedUserActions = this.dataStore.ratedUserActions + ratedUserAction

                    this.didAgreeToRateHandler?.invoke()
                },
                didOptInForReminderHandler = {
                    val optedInForReminderUserAction = UserAction(appVersion = this.appVersionProvider.appVersion, date = Instant.now())
                    this.dataStore.optedInForReminderUserActions = this.dataStore.optedInForReminderUserActions + optedInForReminderUserAction

                    this.didOptInForReminderHandler?.invoke()
                },
                didDeclineHandler = {
                    val declinedToRateUserAction = UserAction(appVersion = this.appVersionProvider.appVersion, date = Instant.now())
                    this.dataStore.declinedToRateUserActions = this.dataStore.declinedToRateUserActions + declinedToRateUserAction

                    this.didDeclineToRateHandler?.invoke()
                }
            )
        }
    }

    fun showRatePrompt() {
        Handler(Looper.getMainLooper()).post {
            this.ratePromptPresenter.show()
        }
    }

    // MARK: Debug

    private fun debugLog(log: String) {
        if (this.debugEnabled) {
            Log.d("SiriusRating", log)
        }
    }

    companion object {

        private fun defaultBuilder(activity: Activity) = SiriusRatingBuilder(activity = activity)

        /** Private property to the convenience singleton to use the SiriusRating instance process-wide. */
        @Volatile
        private var instance: SiriusRating? = null

        /** The getter for the convenience singleton to use the SiriusRating instance process-wide. */
        fun instance(): SiriusRating {
            synchronized(this) {
                if (instance == null) {
                    throw RuntimeException("[SiriusRating] Singleton not yet initialized. Run setup(builder:) first.")
                }
                return instance!!
            }
        }

        fun setup(activity: Activity, block: (SiriusRatingBuilder.() -> Unit)? = null) {
            val builder = SiriusRatingBuilder(activity)
            block?.let { builder.apply(it) }
            setup(builder)
        }

        fun setup(builder: SiriusRatingBuilder) {
            if (instance != null) {
                Log.d("SiriusRating", "The instance can only be setup once.")
                return
            }

            instance = SiriusRating(builder = builder)
        }
    }

}
