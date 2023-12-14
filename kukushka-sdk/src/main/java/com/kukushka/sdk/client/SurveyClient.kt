package com.kukushka.sdk.client

import com.kukushka.sdk.util.SDKString

internal interface SurveyClient {

    fun init(apiKey: SDKString)

    fun hasSurvey()

    fun showSurvey()

    fun dismissSurvey()

    fun dispose()

    fun setOnSurveyAvailableListener(block: () -> Unit)

    fun setOnSurveyUnavailableListener(block: () -> Unit)

    fun setOnSurveyStartListener(block: () -> Unit)

    fun setOnSurveySuccessListener(block: (n: Int) -> Unit)

    fun setOnSurveyFailListener(block: () -> Unit)

    fun setOnSurveyLoadFailListener(block: () -> Unit)

    class SurveyClientException(message: String): Exception(message)
}