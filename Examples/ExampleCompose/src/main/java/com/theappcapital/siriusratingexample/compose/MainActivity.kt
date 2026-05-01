package com.theappcapital.siriusratingexample.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.theappcapital.siriusratingexample.compose.ui.theme.SiriusRatingExampleTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ExampleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SiriusRatingExampleTheme {
                ExampleScreen(viewModel = viewModel)
            }
        }
    }
}
