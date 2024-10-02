package com.theappcapital.siriusratingexample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.theappcapital.siriusrating.SiriusRating
import com.theappcapital.siriusrating.ratingconditions.EnoughAppSessionsRatingCondition
import com.theappcapital.siriusrating.ratingconditions.EnoughDaysUsedRatingCondition
import com.theappcapital.siriusrating.ratingconditions.EnoughSignificantEventsRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotDeclinedToRateAnyVersionRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotPostponedDueToReminderRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotRatedAnyVersionRatingCondition
import com.theappcapital.siriusrating.ratingconditions.NotRatedCurrentVersionRatingCondition
import com.theappcapital.siriusrating.support.versionproviders.PackageInfoCompatAppVersionProvider
import com.theappcapital.siriusratingexample.ui.theme.SiriusRatingExampleTheme

class MainActivity : ComponentActivity() {

    private val homeViewModel by viewModels<HomeViewModel> {
        HomeViewModel.factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SiriusRating.setup(this) {
            debugEnabled(true)
            canPromptUserToRateOnLaunch(true)
            ratingConditions(
                // For demo purposes we do not use these rating conditions. They are however recommended for production.
                EnoughDaysUsedRatingCondition(totalDaysRequired = 0u),
                EnoughAppSessionsRatingCondition(totalAppSessionsRequired = 0u),
                // The prompt will trigger when it reached 5 significant events.
                EnoughSignificantEventsRatingCondition(significantEventsRequired = 5u),
                NotPostponedDueToReminderRatingCondition(totalDaysBeforeReminding = 14u),
                NotDeclinedToRateAnyVersionRatingCondition(daysAfterDecliningToPromptUserAgain = 30u, backOffFactor = 2.0, maxRecurringPromptsAfterDeclining = 2u),
                NotRatedCurrentVersionRatingCondition(appVersionProvider = PackageInfoCompatAppVersionProvider(context = activity)),
                NotRatedAnyVersionRatingCondition(daysAfterRatingToPromptUserAgain = 240u, maxRecurringPromptsAfterRating = UInt.MAX_VALUE)
            )
            didAgreeToRateHandler {
                Toast.makeText(this.activity, "You'll need to upload your app to Internal Testing on the Google Play Store to trigger the rate prompt.", Toast.LENGTH_LONG).show()
            }
            didOptInForReminderHandler {
                Toast.makeText(this.activity, "Did press reminder button.", Toast.LENGTH_SHORT).show()
            }
            didDeclineToRateHandler {
                Toast.makeText(this.activity, "Did press decline button.", Toast.LENGTH_SHORT).show()
            }
        }

        enableEdgeToEdge()
        setContent {
            SiriusRatingExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        HomeScreen(viewModel = homeViewModel)
                    }
                }
            }
        }
    }

}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val eventCount by viewModel.significantEventCount.observeAsState(0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(30.dp)
    ) {
        Text(
            text = "Significant events: $eventCount",
            textAlign = TextAlign.Center
        )
        Text(
            text = "(The prompt will trigger after 5 significant events, provided no user actions have been taken.)",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.userDidSignificantEvent()
        }) {
            Text("Trigger significant event")
        }

        Button(onClick = {
            viewModel.resetUsageTrackers()
            Toast.makeText(context, "Did reset usage trackers.", Toast.LENGTH_SHORT).show()
        }) {
            Text("Reset usage trackers")
        }

        Button(onClick = {
            viewModel.resetUserActions()
            Toast.makeText(context, "Did reset user actions.", Toast.LENGTH_SHORT).show()
        }) {
            Text("Reset user actions")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You'll need to upload your app to Internal Testing on the Google Play Store to trigger the rate prompt.",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            SiriusRating.instance().showRequestToRatePrompt()
        }) {
            Text("Test request prompt")
        }
    }
}

class HomeViewModel(private val siriusRating: SiriusRating) : ViewModel() {
    private val _significantEventCount = MutableLiveData(this.siriusRating.dataStore.significantEventCount)
    val significantEventCount: LiveData<UInt> = _significantEventCount

    fun userDidSignificantEvent() {
        this.siriusRating.userDidSignificantEvent()
        _significantEventCount.value = this.siriusRating.dataStore.significantEventCount
    }

    fun resetUsageTrackers() {
        this.siriusRating.resetUsageTrackers()
        _significantEventCount.value = this.siriusRating.dataStore.significantEventCount
    }

    fun resetUserActions() {
        this.siriusRating.resetUserActions()
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(siriusRating = SiriusRating.instance())
            }
        }
    }
}