package com.theappcapital.siriusratingexample.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.theappcapital.siriusratingexample.compose.ui.theme.AppColors
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleScreen(viewModel: ExampleViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("SiriusRating") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Section(title = "Conditions") { ConditionsSection(state) }

            Section {
                ValueRow(label = "Significant events", value = "${state.significantEventsCount}")
            }

            ButtonsSection(
                onTriggerSignificantEvent = viewModel::userDidSignificantEvent,
                onTestRequestPrompt = viewModel::showRequestPrompt,
                onResetAllTrackers = viewModel::resetAllTrackers,
            )

            Section(title = "Misc") {
                ValueRow(label = "App sessions", value = "${state.appSessionsCount}")
                RowDivider()
                ValueRow(label = "First use date", value = state.firstUseDate?.let {
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.LONG)
                        .withZone(ZoneId.systemDefault())
                        .format(it)
                } ?: "-")
            }

            Section(title = "User actions") {
                ValueRow(label = "Rated", value = "${state.ratedCount}")
                RowDivider()
                ValueRow(label = "Declined", value = "${state.declinedCount}")
                RowDivider()
                ValueRow(label = "Opted for reminder", value = "${state.optedInForReminderCount}")
            }
        }
    }
}

@Composable
private fun Section(
    title: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (title != null) {
            Text(
                text = title.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun ConditionsSection(state: ExampleUiState) {
    Column {
        state.conditionResults.forEachIndexed { index, result ->
            if (index > 0) RowDivider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                val tint = if (result.isSatisfied) AppColors.success else AppColors.danger
                Icon(
                    imageVector = if (result.isSatisfied) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = tint,
                )
                Spacer(Modifier.width(12.dp))
                Text(result.name)
            }
        }
        if (state.conditionResults.isNotEmpty()) RowDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("Result:", modifier = Modifier.weight(1f))
            Text(
                text = if (state.allConditionsMet) "Will show prompt" else "Will not show prompt",
                color = if (state.allConditionsMet) AppColors.success else AppColors.danger,
            )
        }
    }
}

@Composable
private fun ButtonsSection(
    onTriggerSignificantEvent: () -> Unit,
    onTestRequestPrompt: () -> Unit,
    onResetAllTrackers: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "(Prompt will trigger when it reached 5 significant events)",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Button(onClick = onTriggerSignificantEvent) {
            Text("Trigger significant event")
        }
        Button(onClick = onTestRequestPrompt) {
            Text("Test request prompt")
        }
        TextButton(onClick = onResetAllTrackers) {
            Text("Reset all trackers")
        }
    }
}

@Composable
private fun ValueRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(label)
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RowDivider() {
    Box(modifier = Modifier.padding(start = 16.dp)) {
        HorizontalDivider()
    }
}

