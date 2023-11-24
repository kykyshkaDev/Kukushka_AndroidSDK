package com.kukushka.sdk.overlay

internal interface SurveyWebViewCallback {

    fun onError(errorData: String?)

    fun onHttpError(httpStatus: String?)

    fun onPageStarted(url: String?)

    fun onPageLoaded(url: String?)

    fun onReceiveMessage(data: String, type: SurveyJavaScriptListener.MessageType)

}