package com.theappcapital.siriusratingexample.xml

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.theappcapital.siriusratingexample.xml.databinding.ActivityMainBinding
import com.theappcapital.siriusratingexample.xml.databinding.CellConditionBinding
import com.theappcapital.siriusratingexample.xml.databinding.CellDividerBinding
import com.theappcapital.siriusratingexample.xml.databinding.CellValueBinding
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ExampleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        applyWindowInsets()

        bindSections()
        bindButtons()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refresh()
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = bars.top)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    private fun bindSections() {
        binding.conditionsHeader.root.setText(R.string.title_conditions)
        binding.miscHeader.root.setText(R.string.title_misc)
        binding.userActionsHeader.root.setText(R.string.title_user_actions)

        binding.significantEventsCell.label.setText(R.string.label_significant_events)
        binding.appSessionsCell.label.setText(R.string.label_app_sessions)
        binding.firstUseDateCell.label.setText(R.string.label_first_use_date)
        binding.ratedCell.label.setText(R.string.label_rated)
        binding.declinedCell.label.setText(R.string.label_declined)
        binding.optedForReminderCell.label.setText(R.string.label_opted_for_reminder)
    }

    private fun bindButtons() {
        binding.triggerSignificantEventButton.setOnClickListener {
            viewModel.userDidSignificantEvent()
        }
        binding.testRequestPromptButton.setOnClickListener {
            viewModel.showRequestPrompt()
        }
        binding.resetAllTrackersButton.setOnClickListener {
            viewModel.resetAllTrackers()
        }
    }

    private fun render(state: ExampleUiState) {
        renderConditions(state)
        binding.significantEventsCell.value.text = state.significantEventsCount.toString()
        binding.appSessionsCell.value.text = state.appSessionsCount.toString()
        binding.firstUseDateCell.value.text = state.firstUseDate?.let {
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.LONG)
                .withZone(ZoneId.systemDefault())
                .format(it)
        } ?: getString(R.string.placeholder_dash)
        binding.ratedCell.value.text = state.ratedCount.toString()
        binding.declinedCell.value.text = state.declinedCount.toString()
        binding.optedForReminderCell.value.text = state.optedInForReminderCount.toString()
    }

    private fun renderConditions(state: ExampleUiState) {
        val container = binding.conditionsContainer
        container.removeAllViews()

        val inflater = LayoutInflater.from(this)
        state.conditionResults.forEachIndexed { index, result ->
            if (index > 0) {
                container.addView(CellDividerBinding.inflate(inflater, container, false).root)
            }
            val cell = CellConditionBinding.inflate(inflater, container, false)
            cell.label.text = result.name
            cell.icon.setImageResource(
                if (result.isSatisfied) R.drawable.ic_check_circle_filled else R.drawable.ic_cancel_filled
            )
            cell.icon.imageTintList = ContextCompat.getColorStateList(
                this,
                if (result.isSatisfied) R.color.accent_green else R.color.accent_red,
            )
            container.addView(cell.root)
        }

        if (state.conditionResults.isNotEmpty()) {
            container.addView(CellDividerBinding.inflate(inflater, container, false).root)
        }

        val resultCell = CellValueBinding.inflate(inflater, container, false)
        resultCell.label.setText(R.string.label_result)
        resultCell.value.text = getString(
            if (state.allConditionsMet) R.string.label_will_show_prompt else R.string.label_will_not_show_prompt
        )
        resultCell.value.setTextColor(
            ContextCompat.getColor(
                this,
                if (state.allConditionsMet) R.color.accent_green else R.color.accent_red,
            )
        )
        container.addView(resultCell.root)
    }

}
