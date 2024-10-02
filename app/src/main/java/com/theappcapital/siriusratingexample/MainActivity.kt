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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    private val homeViewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SiriusRating.setup(this) {
            debugEnabled(true)
            ratingConditions(
                EnoughDaysUsedRatingCondition(totalDaysRequired = 0u),
                EnoughAppSessionsRatingCondition(totalAppSessionsRequired = 0u),
                EnoughSignificantEventsRatingCondition(significantEventsRequired = 5u),
                NotPostponedDueToReminderRatingCondition(totalDaysBeforeReminding = 14u),
                NotDeclinedToRateAnyVersionRatingCondition(daysAfterDecliningToPromptUserAgain = 30u, backOffFactor = 2.0, maxRecurringPromptsAfterDeclining = 2u),
                NotRatedCurrentVersionRatingCondition(appVersionProvider = PackageInfoCompatAppVersionProvider(context = activity)),
                NotRatedAnyVersionRatingCondition(daysAfterRatingToPromptUserAgain = 240u, maxRecurringPromptsAfterRating = UInt.MAX_VALUE)
            )
            didAgreeToRateHandler {
                Toast.makeText(this.activity, "Did press rate button.", Toast.LENGTH_SHORT).show()
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

@Preview(showBackground = true)
@Composable
fun SiriusRatingExamplePreview() {
    SiriusRatingExampleTheme {

    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
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
            text = "(Prompt will trigger when it reached 5 significant events)",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.incrementEventCount()
        }) {
            Text("Trigger significant event")
        }

        Button(onClick = {
            viewModel.resetEventCount()
        }) {
            Text("Reset all usage trackers")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You will need to upload your app to Internal Testing in Google Play Store to get the rate prompt.",
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
    // Backing field for the significant event count
    private val _significantEventCount = MutableLiveData(0)
    val significantEventCount: LiveData<Int> = _significantEventCount

    // Increment the significant event count
    fun incrementEventCount() {
        _significantEventCount.value = (_significantEventCount.value ?: 0) + 1
    }

    // Reset the significant event count to zero
    fun resetEventCount() {
        _significantEventCount.value = 0

        this.siriusRating.resetUsageTrackers()
    }
}