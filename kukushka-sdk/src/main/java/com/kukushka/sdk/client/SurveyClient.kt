package com.kukushka.sdk.client

import com.kukushka.sdk.util.SDKString

internal interface SurveyClient {

    fun init(apiKey: SDKString)

    fun hasSurvey()

    fun showSurvey()

    fun dismissSurvey()

    fun dispose()

    fun setOnSurveyAvailableListener(block: (data: String) -> Unit)

    fun setOnSurveyUnavailableListener(block: (data: String) -> Unit)

    fun setOnSurveyStartListener(block: (data: String) -> Unit)

    fun setOnSurveySuccessListener(block: (data: String) -> Unit)

    fun setOnSurveyFailListener(block: (data: String) -> Unit)

    fun setOnSurveyLoadFailListener(block: (data: String) -> Unit)

    class SurveyClientException(message: String): Exception(message)
}