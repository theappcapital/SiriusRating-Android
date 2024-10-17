package com.theappcapital.siriusrating.datastores

import android.content.Context
import android.content.SharedPreferences
import com.theappcapital.siriusrating.UserAction
import com.theappcapital.siriusrating.support.InstantIso8601Serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.Instant

class SharedPreferencesDataStore(private val sharedPreferences: SharedPreferences) : DataStore {

    private val json = Json {
        serializersModule = SerializersModule {
            contextual(InstantIso8601Serializer)
        }
    }

    constructor(context: Context) : this(context.getSharedPreferences("SiriusRatingSharedPreferences", Context.MODE_PRIVATE))

    override var firstUseDate: Instant?
        get() {
            return this.sharedPreferences.getString("first_use_date", null)?.toLongOrNull()?.let {
                Instant.ofEpochMilli(it)
            }
        }
        set(value) {
            val epochMilliseconds = value?.toEpochMilli().toString()
            this.sharedPreferences.edit().putString("first_use_date", epochMilliseconds).apply()
        }

    override var appSessionsCount: Int
        get() {
            return this.sharedPreferences.getInt("app_sessions_count", 0).toInt()
        }
        set(value) {
            this.sharedPreferences.edit().putInt("app_sessions_count", value.toInt()).apply()
        }

    override var significantEventCount: Int
        get() {
            return this.sharedPreferences.getInt("significant_event_count", 0).toInt()
        }
        set(value) {
            this.sharedPreferences.edit().putInt("significant_event_count", value.toInt()).apply()
        }

    override var previousOrCurrentAppVersion: String?
        get() {
            return this.sharedPreferences.getString("previous_or_current_app_version", null)
        }
        set(value) {
            this.sharedPreferences.edit().putString("previous_or_current_app_version", value).apply()
        }

    override var optedInForReminderUserActions: List<UserAction>
        get() {
            return this.sharedPreferences.getString("opted_in_for_reminder_user_actions", null)?.let {
                this.json.decodeFromString(it)
            } ?: listOf()
        }
        set(value) {
            val jsonString = this.json.encodeToString(value)
            this.sharedPreferences.edit().putString("opted_in_for_reminder_user_actions", jsonString).apply()
        }

    override var ratedUserActions: List<UserAction>
        get() {
            return this.sharedPreferences.getString("rated_user_actions", null)?.let {
                this.json.decodeFromString(it)
            } ?: listOf()
        }
        set(value) {
            val jsonString = this.json.encodeToString(value)
            this.sharedPreferences.edit().putString("rated_user_actions", jsonString).apply()
        }

    override var declinedToRateUserActions: List<UserAction>
        get() {
            return this.sharedPreferences.getString("declined_to_rate_user_actions", null)?.let {
                this.json.decodeFromString(it)
            } ?: listOf()
        }
        set(value) {
            val jsonString = this.json.encodeToString(value)
            this.sharedPreferences.edit().putString("declined_to_rate_user_actions", jsonString).apply()
        }

    override fun toString(): String {
        return """<${super.toString()}
                    - 'firstUseDate': ${this.firstUseDate})
                    - 'appSessionsCount': ${this.appSessionsCount})
                    - 'significantEventCount': ${this.significantEventCount})
                    - 'previousOrCurrentAppVersion': ${this.previousOrCurrentAppVersion})
                    - 'optedInForReminderUserActions': ${this.optedInForReminderUserActions})
                    - 'ratedUserActions': ${this.ratedUserActions})
                    - 'declinedToRateUserActions': ${this.declinedToRateUserActions})>
            """
    }
}