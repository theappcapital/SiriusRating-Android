package com.theappcapital.siriusrating

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.theappcapital.siriusrating.SiriusRating.Companion.setup
import com.theappcapital.siriusrating.datastores.DataStore
import com.theappcapital.siriusrating.datastores.SharedPreferencesDataStore
import com.theappcapital.siriusrating.prompts.presenters.GooglePlayRatePromptPresenter
import com.theappcapital.siriusrating.prompts.presenters.RatePromptPresenter
import com.theappcapital.siriusrating.prompts.presenters.RequestPromptPresenter
import com.theappcapital.siriusrating.prompts.presenters.StyleOneRequestPromptPresenter
import com.theappcapital.siriusrating.ratingconditions.ClosureRatingCondition
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
import java.lang.ref.WeakReference
import java.time.Instant

class SiriusRating private constructor(
    private val application: Application,
    private val appVersionProvider: AppVersionProvider,
    val dataStore: DataStore,
    private val requestPromptPresenter: RequestPromptPresenter,
    private val ratePresenter: RatePromptPresenter,
    private val debugEnabled: Boolean,
    val ratingConditions: List<RatingCondition>,
    private val canPromptUserToRateOnLaunch: Boolean,
    private val didOptInForReminderHandler: (() -> Unit)?,
    private val didDeclineToRateHandler: (() -> Unit)?,
    private val didAgreeToRateHandler: (() -> Unit)?,
    private val needsResetTrackers: (DataStore, AppVersionProvider) -> Boolean
) {

    /** Weak reference to the currently resumed Activity, tracked via [activityLifecycleCallbacks]. */
    private var currentActivityRef: WeakReference<Activity>? = null

    private val currentActivity: Activity?
        get() = currentActivityRef?.get()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            userDidLaunchApp()
        }
    }

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // Empty.
        }

        override fun onActivityStarted(activity: Activity) {
            // Empty.
        }

        override fun onActivityResumed(activity: Activity) {
            currentActivityRef = WeakReference(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            if (currentActivity === activity) {
                currentActivityRef = null
            }
        }

        override fun onActivityStopped(activity: Activity) {
            // Empty.
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // Empty.
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (currentActivity === activity) {
                currentActivityRef = null
            }
        }

    }

    val ratingConditionsHaveBeenMet: Boolean
        get() {
            if (this.ratingConditions.isEmpty()) {
                this.debugLog("No rating conditions have been found to validate.")
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

    init {
        this.application.registerActivityLifecycleCallbacks(this.activityLifecycleCallbacks)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this.lifecycleObserver)
    }

    private fun removeObservers() {
        this.application.unregisterActivityLifecycleCallbacks(this.activityLifecycleCallbacks)

        ProcessLifecycleOwner.get().lifecycle.removeObserver(this.lifecycleObserver)
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

    private fun beforeIncrementingUsageCounter() {
        // 1. Check if we need to reset all the trackers. For example: an app publisher may want to reset
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
        this.showRequestPromptIfConditionsHaveBeenMet()
    }

    private fun incrementAppSessionsCountAndRate() {
        this.incrementAppSessionsCount()
        this.showRequestPromptIfConditionsHaveBeenMet()
    }

    fun resetUsageTrackers() {
        this.dataStore.firstUseDate = null
        this.dataStore.appSessionsCount = 0
        this.dataStore.significantEventCount = 0

        this.debugLog("Reset usage trackers.")
    }

    fun resetUserActions() {
        this.dataStore.ratedUserActions = emptyList()
        this.dataStore.optedInForReminderUserActions = emptyList()
        this.dataStore.declinedToRateUserActions = emptyList()

        this.debugLog("Reset user actions.")
    }

    fun resetAllTrackers() {
        this.resetUsageTrackers()
        this.resetUserActions()

        this.dataStore.previousOrCurrentAppVersion = null

        this.debugLog("Reset all trackers.")
    }

    private fun resetTrackersIfNeeded() {
        if (this.needsResetTrackers(this.dataStore, this.appVersionProvider)) {
            this.resetAllTrackers()
        }
    }

    private fun showRequestPromptIfConditionsHaveBeenMet() {
        if (this.ratingConditionsHaveBeenMet) {
            this.debugLog("All rating conditions have been met, show the prompt that requests the user to rate.")

            this.showRequestPrompt()
        }
    }

    fun showRequestPrompt() {
        // This is silly, but it sometimes bugs the dialog when
        // immediately showing the dialog after dismissing an activity.
        Handler(Looper.getMainLooper()).postDelayed(1000) {
            val activity = this.currentActivity ?: run {
                this.debugLog("Cannot show request prompt: no foreground Activity.")
                return@postDelayed
            }

            this.requestPromptPresenter.show(
                activity = activity,
                didAgreeToRateHandler = {
                    this.showRatePrompt()

                    // Assume this version is rated. There is no API to tell if the user actually rated.
                    val ratedUserAction = UserAction(appVersion = this.appVersionProvider.appVersion, date = Instant.now())
                    this.dataStore.ratedUserActions += ratedUserAction

                    this.didAgreeToRateHandler?.invoke()
                },
                didOptInForReminderHandler = {
                    val optedInForReminderUserAction = UserAction(appVersion = this.appVersionProvider.appVersion, date = Instant.now())
                    this.dataStore.optedInForReminderUserActions += optedInForReminderUserAction

                    this.didOptInForReminderHandler?.invoke()
                },
                didDeclineToRateHandler = {
                    val declinedToRateUserAction = UserAction(appVersion = this.appVersionProvider.appVersion, date = Instant.now())
                    this.dataStore.declinedToRateUserActions += declinedToRateUserAction

                    this.didDeclineToRateHandler?.invoke()
                }
            )
        }
    }

    fun showRatePrompt() {
        Handler(Looper.getMainLooper()).post {
            val activity = this.currentActivity ?: run {
                this.debugLog("Cannot show rate prompt: no foreground Activity.")
                return@post
            }
            this.ratePresenter.show(activity = activity)
        }
    }

    private fun debugLog(log: String) {
        if (this.debugEnabled) {
            Log.d("SiriusRating", log)
        }
    }

    /**
     * A configuration object for setting up [SiriusRating].
     *
     * Use this with the closure-based [SiriusRating.setup] method:
     * ```
     * SiriusRating.setup(this) {
     *     daysUntilPrompt = 14
     *     appSessionsUntilPrompt = 10
     *     significantEventsUntilPrompt = 5
     *     debugEnabled = true
     * }
     * ```
     *
     * All properties have sensible defaults.
     */
    class Configuration internal constructor(internal val application: Application) {

        // MARK: - Prompt Thresholds

        /** The number of days the app must be installed before prompting. Default: `30`. */
        var daysUntilPrompt: Int = 30

        /** The number of app sessions (launches or foreground entries) before prompting. Default: `15`. */
        var appSessionsUntilPrompt: Int = 15

        /** The number of significant events before prompting. Default: `20`. */
        var significantEventsUntilPrompt: Int = 20

        // MARK: - Reminder Behavior

        /** The number of days to wait before reminding a user who chose "Remind me later." Default: `7`. */
        var daysBeforeReminding: Int = 7

        // MARK: - Decline Behavior

        /** The number of days to wait before prompting again after the user declines. Default: `30`. */
        var daysAfterDecliningToPromptAgain: Int = 30

        /**
         * The back-off multiplier applied to [daysAfterDecliningToPromptAgain] for each successive
         * decline. For example, with a value of `2.0` and a base of 30 days, successive prompts
         * occur at 30, 60, 120 days, etc. Set to `null` to disable back-off. Default: `2.0`.
         */
        var declineBackOffFactor: Double? = 2.0

        /** The maximum number of times the user can be re-prompted after declining. Default: `2`. */
        var maxPromptsAfterDeclining: Int = 2

        // MARK: - Re-prompt After Rating

        /** The number of days to wait before prompting a user who has already rated. Default: `240`. */
        var daysAfterRatingToPromptAgain: Int = 240

        /** The maximum number of times the user can be re-prompted after rating. Default: [Int.MAX_VALUE]. */
        var maxPromptsAfterRating: Int = Int.MAX_VALUE

        // MARK: - Debug

        /** When `true`, SiriusRating prints diagnostic information to logcat. Default: `false`. */
        var debugEnabled: Boolean = false

        // MARK: - Launch Behavior

        /**
         * When `true`, SiriusRating will check conditions and potentially show the prompt on app
         * launch or when the app enters the foreground. Default: `false`.
         */
        var canPromptUserToRateOnLaunch: Boolean = false

        // MARK: - Providers and Presenters
        //
        // These defaults are initialized lazily on first access so that assigning a custom value
        // in the configure block never pays the cost of constructing the default.

        private var _appVersionProvider: AppVersionProvider? = null

        /** The provider that supplies the current app version string. */
        var appVersionProvider: AppVersionProvider
            get() = this._appVersionProvider ?: PackageInfoCompatAppVersionProvider(context = application).also { this._appVersionProvider = it }
            set(value) {
                this._appVersionProvider = value
            }

        private var _dataStore: DataStore? = null

        /** The data store for persisting usage trackers and user actions. */
        var dataStore: DataStore
            get() = this._dataStore ?: SharedPreferencesDataStore(context = application).also { this._dataStore = it }
            set(value) {
                this._dataStore = value
            }

        private var _requestPromptPresenter: RequestPromptPresenter? = null

        /** The presenter for the initial "Would you like to rate?" prompt. */
        var requestPromptPresenter: RequestPromptPresenter
            get() = this._requestPromptPresenter ?: StyleOneRequestPromptPresenter().also { this._requestPromptPresenter = it }
            set(value) {
                this._requestPromptPresenter = value
            }

        private var _ratePresenter: RatePromptPresenter? = null

        /** The presenter for the actual rating interface (e.g., the Google Play in-app review flow). */
        var ratePresenter: RatePromptPresenter
            get() = this._ratePresenter ?: GooglePlayRatePromptPresenter().also { this._ratePresenter = it }
            set(value) {
                this._ratePresenter = value
            }

        // MARK: - Callbacks

        /** Called when the user taps "Remind me later." */
        var didOptInForReminderHandler: (() -> Unit)? = null

        /** Called when the user declines to rate. */
        var didDeclineToRateHandler: (() -> Unit)? = null

        /** Called when the user agrees to rate. */
        var didAgreeToRateHandler: (() -> Unit)? = null

        /**
         * Return `true` to reset all usage trackers (e.g., on a new major version).
         * SiriusRating never resets trackers by default.
         */
        var needsResetTrackers: (DataStore, AppVersionProvider) -> Boolean = { _, _ -> false }

        // MARK: - Custom Rating Conditions

        /**
         * Additional rating conditions appended to the default conditions built from the simple
         * properties above.
         */
        var additionalConditions: List<RatingCondition> = emptyList()

        /**
         * When set, this lambda is used as an inline rating condition. It is appended to the
         * conditions list alongside [additionalConditions].
         */
        var customCondition: ((DataStore) -> Boolean)? = null

        internal fun buildRatingConditions(): List<RatingCondition> {
            val conditions = mutableListOf<RatingCondition>(
                EnoughDaysUsedRatingCondition(totalDaysRequired = this.daysUntilPrompt),
                EnoughAppSessionsRatingCondition(totalAppSessionsRequired = this.appSessionsUntilPrompt),
                EnoughSignificantEventsRatingCondition(significantEventsRequired = this.significantEventsUntilPrompt),
                NotPostponedDueToReminderRatingCondition(totalDaysBeforeReminding = this.daysBeforeReminding),
                NotDeclinedToRateAnyVersionRatingCondition(
                    daysAfterDecliningToPromptUserAgain = this.daysAfterDecliningToPromptAgain,
                    backOffFactor = this.declineBackOffFactor,
                    maxRecurringPromptsAfterDeclining = this.maxPromptsAfterDeclining
                ),
                NotRatedCurrentVersionRatingCondition(appVersionProvider = this.appVersionProvider),
                NotRatedAnyVersionRatingCondition(
                    daysAfterRatingToPromptUserAgain = this.daysAfterRatingToPromptAgain,
                    maxRecurringPromptsAfterRating = this.maxPromptsAfterRating
                )
            )

            conditions.addAll(this.additionalConditions)

            this.customCondition?.let { conditions.add(ClosureRatingCondition(closure = it)) }

            return conditions
        }
    }

    companion object {

        @Volatile
        private var _instance: SiriusRating? = null

        /** The singleton instance. Call [setup] first, or this throws. */
        fun instance(): SiriusRating {
            return _instance ?: throw IllegalStateException("[SiriusRating] Singleton not yet initialized. Run setup() first.")
        }

        /**
         * Set up the singleton instance for [SiriusRating].
         *
         * ```
         * SiriusRating.setup(this) {
         *     daysUntilPrompt = 14
         *     appSessionsUntilPrompt = 10
         *     significantEventsUntilPrompt = 5
         *     debugEnabled = true
         * }
         * ```
         *
         * This is a convenience overload that uses the Activity's [Application].
         * The library itself never holds a reference to the Activity.
         */
        fun setup(activity: Activity, configure: (Configuration.() -> Unit)? = null) {
            setup(application = activity.application, configure = configure)
        }

        /**
         * Set up the singleton instance for [SiriusRating].
         *
         * Prefer this overload when calling from `Application.onCreate()`.
         */
        fun setup(application: Application, configure: (Configuration.() -> Unit)? = null) {
            synchronized(this) {
                // Remove observers from previous instance to prevent duplicate event counting.
                _instance?.removeObservers()

                val configuration = Configuration(application = application)
                configure?.invoke(configuration)

                _instance = SiriusRating(
                    application = application,
                    appVersionProvider = configuration.appVersionProvider,
                    dataStore = configuration.dataStore,
                    requestPromptPresenter = configuration.requestPromptPresenter,
                    ratePresenter = configuration.ratePresenter,
                    debugEnabled = configuration.debugEnabled,
                    ratingConditions = configuration.buildRatingConditions(),
                    canPromptUserToRateOnLaunch = configuration.canPromptUserToRateOnLaunch,
                    didOptInForReminderHandler = configuration.didOptInForReminderHandler,
                    didDeclineToRateHandler = configuration.didDeclineToRateHandler,
                    didAgreeToRateHandler = configuration.didAgreeToRateHandler,
                    needsResetTrackers = configuration.needsResetTrackers
                )
            }
        }
    }

}
