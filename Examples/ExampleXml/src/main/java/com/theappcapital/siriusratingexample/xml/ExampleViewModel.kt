package com.theappcapital.siriusratingexample.xml

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theappcapital.siriusrating.SiriusRating
import com.theappcapital.siriusrating.ratingconditions.EnoughAppSessionsRatingCondition
import com.theappcapital.siriusrating.ratingconditions.EnoughDaysUsedRatingCondition
import com.theappcapital.siriusrating.ratingconditions.EnoughSignificantEventsRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotDeclinedToRateAnyVersionRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotDeclinedToRateCurrentVersionRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotPostponedDueToReminderRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotRatedAnyVersionRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotRatedCurrentVersionRatingCondition
import com.theappcapital.siriusrating.ratingconditions.RatingCondition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class ConditionResult(val name: String, val isSatisfied: Boolean)

data class ExampleUiState(
    val significantEventsCount: Int = 0,
    val appSessionsCount: Int = 0,
    val firstUseDate: Instant? = null,
    val conditionResults: List<ConditionResult> = emptyList(),
    val ratedCount: Int = 0,
    val declinedCount: Int = 0,
    val optedInForReminderCount: Int = 0,
) {
    val allConditionsMet: Boolean = conditionResults.isNotEmpty() && conditionResults.all { it.isSatisfied }
}

class ExampleViewModel(
    private val siriusRating: SiriusRating = SiriusRating.instance(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            SiriusRatingExampleApp.userDidInteractWithPrompt.collect { refresh() }
        }
    }

    fun userDidSignificantEvent() {
        if (siriusRating.dataStore.significantEventCount == 0) {
            siriusRating.resetUserActions()
        }
        siriusRating.userDidSignificantEvent()
        refresh()
    }

    fun showRequestPrompt() {
        siriusRating.showRequestPrompt()
    }

    fun resetAllTrackers() {
        siriusRating.resetAllTrackers()
        refresh()
    }

    fun refresh() {
        val dataStore = siriusRating.dataStore
        _uiState.value = ExampleUiState(
            significantEventsCount = dataStore.significantEventCount,
            appSessionsCount = dataStore.appSessionsCount,
            firstUseDate = dataStore.firstUseDate,
            conditionResults = siriusRating.ratingConditions.map { condition ->
                ConditionResult(
                    name = displayName(condition),
                    isSatisfied = condition.isSatisfied(dataStore),
                )
            },
            ratedCount = dataStore.ratedUserActions.size,
            declinedCount = dataStore.declinedToRateUserActions.size,
            optedInForReminderCount = dataStore.optedInForReminderUserActions.size,
        )
    }

    private fun displayName(condition: RatingCondition): String = when (condition) {
        is EnoughDaysUsedRatingCondition -> "Enough days used"
        is EnoughAppSessionsRatingCondition -> "Enough app sessions"
        is EnoughSignificantEventsRatingCondition -> "Enough significant events"
        is NotPostponedDueToReminderRatingCondition -> "Reminder wait period passed"
        is NotDeclinedToRateAnyVersionRatingCondition -> "Decline wait period passed"
        is NotDeclinedToRateCurrentVersionRatingCondition -> "Current version not declined"
        is NotRatedCurrentVersionRatingCondition -> "Current version not yet rated"
        is NotRatedAnyVersionRatingCondition -> "Rating wait period passed"
        else -> condition::class.simpleName ?: "Unknown"
    }
}
