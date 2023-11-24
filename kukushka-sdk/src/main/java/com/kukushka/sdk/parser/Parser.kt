package com.kukushka.sdk.parser

import com.kukushka.sdk.logger.taggedLogger
import com.kukushka.sdk.model.SurveyCallback
import com.kukushka.sdk.model.SurveyEvent
import com.kukushka.sdk.util.Environment
import org.json.JSONException
import org.json.JSONObject

private val logger by taggedLogger("${Environment.sdkName}:Parser")

internal fun parseToSurveyCallback(data: String?): SurveyCallback? {

    if (data == null) return null

    return try {
        val json = JSONObject(data)
        val surveyMaster = json.getJSONObject("surveyMaster")
        val event = surveyMaster.getString("event")
        val data = surveyMaster.getJSONObject("data").toString()

        val surveyEvent = SurveyEvent.from(event)

        val callback = SurveyCallback(
            event = surveyEvent,
            data = data
        )

        logger.i { "[SurveyCallback] successfully parsed | $callback" }

        callback
    } catch (e: JSONException) {
        logger.e { "Failed to parse to [SurveyCallback] | Exception: ${e.message}" }
        null
    }
}

internal fun parseCustomDataToSurveyCallback(data: String?): SurveyCallback? {

    if (data == null) return null

    return try {
        val json = JSONObject(data)
        val customData = json.getJSONObject("customData")
        val name = customData.getString("name")
        val data = customData.getJSONObject("data").toString()

        val customEvent = SurveyEvent.from(name)

        val callback = SurveyCallback(
            event = customEvent,
            data = data
        )

        logger.i { "[SurveyCallback] successfully parsed | $callback" }

        callback
    } catch (e: JSONException) {
        logger.e { "Failed to parse to [SurveyCallback] | Exception: ${e.message}" }
        null
    }
}