package com.kukushka.sdk.model

sealed class SurveyEvent(val tag: String) {

    object SurveyAvailable: SurveyEvent("onSurveyAvailable")

    object SurveyUnavailable: SurveyEvent("onSurveyUnavailable")

    object SurveyStart: SurveyEvent("onSurveyStart")

    object SurveySuccess: SurveyEvent("onSuccess")

    object SurveyFail: SurveyEvent("onFail")

    object LoadFail: SurveyEvent("onLoadFail")

    object PageReady: SurveyEvent("onPageReady")

    object LinkClicked: SurveyEvent("ctaLinkClicked")

    object None: SurveyEvent("None")

    companion object {
        fun from(value: String): SurveyEvent {
            return when(value) {
                "onSurveyAvailable" -> SurveyAvailable
                "onPageReady" -> PageReady
                "onSurveyUnavailable" -> SurveyUnavailable
                "onSurveyStart" -> SurveyStart
                "onSuccess" -> SurveySuccess
                "onFail" -> SurveyFail
                "onLoadFail" -> LoadFail
                "ctaLinkClicked" -> LinkClicked
                else -> None
            }
        }
    }
}
