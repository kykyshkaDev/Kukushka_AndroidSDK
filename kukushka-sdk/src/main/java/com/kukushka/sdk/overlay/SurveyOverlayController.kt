package com.kukushka.sdk.overlay

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kukushka.sdk.R
import com.kukushka.sdk.logger.taggedLogger
import com.kukushka.sdk.util.Environment
import com.kukushka.sdk.util.timerScript


internal class SurveyOverlayController: OverlayController {

    private val logger by taggedLogger("${Environment.sdkName}:SurveyOverlayController")

    private var surveyWebView: WebView? = null

    var isWebViewAttached: Boolean = false
        private set

    private var callback: SurveyWebViewCallback? = null

    private var onAppear: () -> Unit = {}
    private var onDisappear: () -> Unit = {}

    private var savedWebViewState: Bundle = Bundle()

    var isShowing: Boolean = false
        private set

    override fun show() {
        surveyWebView?.visibility = View.VISIBLE
        isShowing = true
        onAppear.invoke()
        evaluateJavascript(timerScript)
    }

    override fun hide() {
        surveyWebView?.visibility = View.GONE
        isShowing = false
        onDisappear.invoke()
    }

    fun prepareWebView(url: String, callback: SurveyWebViewCallback) {
        this.callback = callback

        logger.i { "Preparing WebView" }

        surveyWebView?.configure()
        surveyWebView?.loadUrl(url)
    }

    fun attachWebView(activity: Activity, isVisible: Boolean) {
        logger.i { "Attaching WebView" }
        with(activity) {
            surveyWebView = WebView(activity)
                .apply {
                    id = R.id.web_view_overlay
                    visibility = if (isVisible) View.VISIBLE else View.GONE
                }

            val rootLayout = findViewById<ViewGroup>(android.R.id.content)
            rootLayout.addView(surveyWebView)
            isWebViewAttached = true
        }
    }

    fun restoreWebView(activity: Activity) {
        attachWebView(activity, true)
        surveyWebView?.restoreState(savedWebViewState)
        surveyWebView?.configure()
    }

    fun evaluateJavascript(script: String) {
        surveyWebView?.evaluateJavascript(script) { /* Ignore callback */ }
    }

    fun detachWebView(activity: Activity) {
        logger.i { "Detaching WebView" }

        if (isShowing) {
            surveyWebView?.saveState(savedWebViewState)
        }

        with(activity) {
            val rootLayout = findViewById<ViewGroup>(android.R.id.content)
            rootLayout.removeView(surveyWebView)
            surveyWebView = null
            isWebViewAttached = false
        }
    }

    fun onAppear(block: () -> Unit) {
        onAppear = block
    }

    fun onDisappear(block: () -> Unit) {
        onDisappear = block
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun WebView.configure() {
        // Basic features
        with(settings) {
            javaScriptEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            domStorageEnabled = true
        }

        // Disable zoom
        with(settings) {
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        // Disable scroll
        // setOnTouchListener { _, event -> event.action == MotionEvent.ACTION_MOVE }
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false

        // Attach client
        webViewClient = SurveyWebViewClient()

        // Attach JS interface
        surveyWebView?.addJavascriptInterface(
            SurveyJavaScriptListener(
                onMessagePosted = { data, type -> callback?.onReceiveMessage(data, type) }
            ),
            SurveyJavaScriptListener.INTERFACE_NAME
        )
    }

    private inner class SurveyWebViewClient: WebViewClient() {

        private val logger by taggedLogger("KukushkaSDK:SurveyOverlayController:SurveyWebViewClient")

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            logger.e { "onReceivedError: $errorCode" }
            surveyWebView?.loadUrl("about:blank")
            callback?.onError(errorCode.toString() + "\t" + description + "\t" + failingUrl)
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            val statusCode = errorResponse.statusCode.toString()
            logger.e { "onReceivedHttpError: $statusCode" }
            callback?.onHttpError(statusCode)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            callback?.onPageStarted(url)
            logger.i { "onPageStarted: $url" }
        }

        override fun onPageFinished(view: WebView, url: String) {
            callback?.onPageLoaded(url)
            logger.i { "onPageFinished: $url" }
        }
    }
}