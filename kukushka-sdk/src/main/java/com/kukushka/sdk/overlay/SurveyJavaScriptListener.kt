package com.kukushka.sdk.overlay

import android.webkit.JavascriptInterface
import com.kukushka.sdk.logger.taggedLogger
import com.kukushka.sdk.util.Environment

internal class SurveyJavaScriptListener(
    private val onMessagePosted: (data: String, type: MessageType) -> Unit
) {
    private val logger by taggedLogger("${Environment.sdkName}:SurveyJavaScriptListener")

    @JavascriptInterface
    fun postSurveyMessage(data: String) {
        logger.i { "SurveyMessage | data = $data" }
        onMessagePosted.invoke(data, MessageType.Survey)
    }

    @JavascriptInterface
    fun postCustomDataMessage(data: String) {
        logger.i { "CustomDataMessage | data = $data" }
        onMessagePosted.invoke(data, MessageType.CustomData)
    }

    sealed class MessageType {
        object Survey: MessageType()
        object CustomData: MessageType()
    }

    companion object {
        const val INTERFACE_NAME = "Android"
    }
}