package com.theappcapital.siriusratingexample.compose

import android.app.Application
import com.theappcapital.siriusrating.SiriusRating
import kotlinx.coroutines.flow.MutableSharedFlow

class SiriusRatingExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        SiriusRating.setup(application = this) {
            debugEnabled = true
            canPromptUserToRateOnLaunch = true
            daysUntilPrompt = 0
            appSessionsUntilPrompt = 0
            significantEventsUntilPrompt = 5
            daysBeforeReminding = 14
            maxPromptsAfterDeclining = 3
            didAgreeToRateHandler = { userDidInteractWithPrompt.tryEmit(Unit) }
            didOptInForReminderHandler = { userDidInteractWithPrompt.tryEmit(Unit) }
            didDeclineToRateHandler = { userDidInteractWithPrompt.tryEmit(Unit) }
        }
    }

    companion object {
        val userDidInteractWithPrompt = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    }
}
