package com.kukushka.sdk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kukushka.sdk.client.SurveyMaster
import com.kukushka.sdk.model.SurveyEvent
import kotlinx.coroutines.launch

class MainActivityViewModel: ViewModel() {

    var surveyMaster: SurveyMaster = SurveyMaster()
        .apply {
            setUserId("userid")
            init("vIrUgLgPf6rDiAJ0bBpMBwOxAf7W24cXRKyEHSTEXMbYEUZJ8FSRRUbYsiuiubG5")
        }
        private set

    var event: SurveyEvent by mutableStateOf(SurveyEvent.None)
        private set

    init {
        viewModelScope.launch {
            surveyMaster.callbackFlow.collect { callback ->
                event = callback.event
            }
        }
    }
}