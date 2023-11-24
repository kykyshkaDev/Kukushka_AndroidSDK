package com.kukushka.sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kukushka.sdk.model.SurveyEvent
import com.kukushka.sdk.orientation.OrientationControlStrategy
import com.kukushka.sdk.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ApplicationContent(
                    event = viewModel.event,
                    onHasSurvey = { viewModel.surveyMaster.hasSurvey() },
                    onShowSurvey = { viewModel.surveyMaster.showSurvey() },
                    onAppKeyIdChanged = { text ->
                        if (text.isNotBlank() && text.isNotEmpty()) {
                            viewModel.surveyMaster.setApiKey(text)
                        }
                    },
                    onUserIdChanged = { text ->
                        if (text.isNotBlank() && text.isNotEmpty()) {
                            viewModel.surveyMaster.setUserId(text)
                        }
                    }
                )
            }
        }
        viewModel.surveyMaster.run {
            attachToActivity(this@MainActivity)
            setOnSurveySuccessListener {
                dismissSurvey()
            }
            setOnSurveyFailListener {
                dismissSurvey()
            }
            setOrientationControlStrategy(OrientationControlStrategy.Auto)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.surveyMaster.detachFromActivity()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationContent(
    event: SurveyEvent = SurveyEvent.None,
    onHasSurvey: () -> Unit = {},
    onShowSurvey: () -> Unit = {},
    onUserIdChanged: (String) -> Unit = {},
    onAppKeyIdChanged: (String) -> Unit = {}
) {
    var userId by remember { mutableStateOf("userid") }
    var appKey by remember { mutableStateOf("gamedemo") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userId,
                onValueChange = { text ->
                    userId = text
                    onUserIdChanged(text)
                },
                placeholder = { Text("User id") }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = appKey,
                onValueChange = { text ->
                    appKey = text
                    onAppKeyIdChanged(text)
                },
                placeholder = { Text("App key") }
            )
            Text(
                text = event.tag
            )
            Button(onClick = onHasSurvey) {
                Text("hasSurvey()")
            }
            Button(onClick = onShowSurvey) {
                Text("showSurvey()")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        ApplicationContent()
    }
}