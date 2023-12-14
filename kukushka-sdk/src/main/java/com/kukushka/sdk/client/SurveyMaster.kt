package com.kukushka.sdk.client

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.kukushka.sdk.logger.taggedLogger
import com.kukushka.sdk.model.Platform
import com.kukushka.sdk.model.SurveyCallback
import com.kukushka.sdk.model.SurveyEvent
import com.kukushka.sdk.orientation.AndroidOrientationManager
import com.kukushka.sdk.orientation.OrientationControlStrategy
import com.kukushka.sdk.overlay.SurveyJavaScriptListener
import com.kukushka.sdk.overlay.SurveyOverlayController
import com.kukushka.sdk.overlay.SurveyWebViewCallback
import com.kukushka.sdk.parser.parseCustomDataToSurveyCallback
import com.kukushka.sdk.parser.parseToSurveyCallback
import com.kukushka.sdk.util.Environment
import com.kukushka.sdk.util.SDKString
import com.kukushka.sdk.util.addEventListenerScript
import com.kukushka.sdk.util.isInvalid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class SurveyMaster: SurveyClient {

    private val logger by taggedLogger("${Environment.sdkName}:SurveyMaster")

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var pageReadyFlow = MutableStateFlow(false)

    private val _callbackFlow = MutableSharedFlow<SurveyCallback>()
    val callbackFlow = _callbackFlow.asSharedFlow()

    private var activity: Activity? = null

    private val overlayController by lazy { SurveyOverlayController() }
    private val orientationManager by lazy { AndroidOrientationManager() }

    private var platform: Platform = Platform.Android

    private var userId: SDKString = ""
    private var apiKey: SDKString = ""

    private var hasSurveyCalledAt: Long = 0

    private var isHasShowSurveyCalled = false
    private var isShowSurveyCalled = false

    private var onLinkClicked: (data: String) -> Unit = { data ->
        val link = try { JSONObject(data).getString("link") } catch (e: JSONException) { "about:blank" }
        val uri = Uri.parse(link)
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        requireActivity().startActivity(browserIntent)
    }

    private var onSurveyAvailable: () -> Unit = {}
    private var onSurveyUnavailable: () -> Unit = {}
    private var onSurveyStart: () -> Unit = {}
    private var onSurveySuccess: (n: Int) -> Unit = {}
    private var onSurveyFail: () -> Unit = {}
    private var onLoadFail: () -> Unit = {}

    /**
     * Sets a observers for events.
     * If [apiKey] wasn't provided - SDK will be considered uninitialized
     *
     * @param apiKey
     * @throws IllegalArgumentException
     */
    override fun init(apiKey: SDKString) {

        logger.i { "Initializing ${Environment.sdkName}" }

        setApiKey(apiKey)

        attachOnPageReadyObserver()
        attachSurveyCallbackObserver()
    }

    /**
     * @param userId
     * @throws IllegalArgumentException
     */
    fun setUserId(userId: SDKString) {

        if (userId.isInvalid()) {
            throw IllegalArgumentException("User id must be not empty or blank")
        }

        this.userId = userId
    }

    /**
     * @param apiKey
     * @throws IllegalArgumentException
     */
    fun setApiKey(apiKey: SDKString) {

        if (apiKey.isInvalid()) {
            throw IllegalArgumentException("Api key must be not empty or blank")
        }

        this.apiKey = apiKey
    }

    fun setPlatform(platform: Platform) {
        this.platform = platform
    }

    /**
     * Checks whether there are currently surveys suitable for this [userId].
     *
     * The function selects a survey for the user and, if successful, calls [onSurveyAvailable],
     * notifying the application about the presence of a survey. If the survey cannot be selected,
     * [onSurveyUnavailable] is called, reporting the absence of a survey.
     *
     * @throws [SurveyClient.SurveyClientException] if SDK is not initialized
     */
    override fun hasSurvey() {
        checkIsInitialized()
        resetVariables()
        logger.i { "Checking for surveys" }
        isHasShowSurveyCalled = true
        hasSurveyCalledAt = System.currentTimeMillis()
        overlayController.attachWebView(requireActivity(), false)
        overlayController.prepareWebView(buildSurveyUrl(), SurveyWebViewCallbackImpl())
    }

    /**
     * Shows the survey if onPageReady callback was received. If not, when onPageReady
     * will received, WebView will be shown immediately.
     *
     * @throws [SurveyClient.SurveyClientException] if SDK is not initialized
     */
    override fun showSurvey() {
        checkIsInitialized()
        checkSurveyLifetime()
        logger.i { "Showing the survey" }
        isShowSurveyCalled = true
        if (!overlayController.isWebViewAttached) {
            overlayController.attachWebView(requireActivity(), false)
            overlayController.prepareWebView(buildSurveyUrl(), SurveyWebViewCallbackImpl())
        }
        if (pageReadyFlow.value && isShowSurveyCalled) {
            overlayController.show()
        }
    }

    /**
     * Detaching WebView (if it attached) from activity.
     * Resets state of [SurveyMaster] to default.
     *
     * @throws [SurveyClient.SurveyClientException] if SDK is not initialized
     */
    override fun dismissSurvey() {
        checkIsInitialized()
        logger.i { "Dismissing the survey" }
        resetVariables()
        overlayController.hide()
        overlayController.detachWebView(requireActivity())
    }

    override fun setOnSurveyAvailableListener(block: () -> Unit) {
        onSurveyAvailable = block
    }

    override fun setOnSurveyUnavailableListener(block: () -> Unit) {
        onSurveyUnavailable = block
    }

    override fun setOnSurveyStartListener(block: () -> Unit) {
        onSurveyStart = block
    }

    override fun setOnSurveySuccessListener(block: (n: Int) -> Unit) {
        onSurveySuccess = block
    }

    override fun setOnSurveyFailListener(block: () -> Unit) {
        onSurveyFail = block
    }

    override fun setOnSurveyLoadFailListener(block: () -> Unit) {
        onLoadFail = block
    }

    override fun dispose() {
        detachFromActivity()
        scope.cancel()
    }

    fun setOnLinkClickedListener(block: (data: String) -> Unit) {
        onLinkClicked = block
    }

    /**
     * Surveys require showing only in portrait mode.
     *
     * To change the configuration to portrait mode (if necessary), 2 callbacks are provided:
     * onAppear - WebView is now shown and you need to change the orientation
     * onDisappear - WebView is no longer visible and you need to return to the previous orientation
     *
     * If your application is running in landscape mode, don't forget that the class
     * may be destroyed as a result of a configuration change.
     *
     * @param strategy Activity configuration change control strategy
     *
     * [OrientationControlStrategy.Auto] - Configuration change control will be provided by the SDK.
     * [OrientationControlStrategy.ManualWithOrientationManager] - Required callbacks
     * must be provided by yourself. OrientationManager (a class inside the SDK, necessary to
     * simplify the process of working with orientations) will be provided to each callback
     * to simplify the process.
     * [OrientationControlStrategy.Manual] - Required callbacks must be provided by yourself.
     * The SDK does not provide auxiliary ways to control orientation.
     * [OrientationControlStrategy.Ignore] - Ignore configuration changes callback (not recommended).
     * */
    fun setOrientationControlStrategy(strategy: OrientationControlStrategy) {
        when(strategy) {
            is OrientationControlStrategy.Auto -> {
                overlayController.onAppear {
                    with(orientationManager) {
                        saveOrientationState(requireActivity())
                        setPortrait(requireActivity())
                    }
                }
                overlayController.onDisappear {
                    with(orientationManager) {
                        resetToSavedOrientationState(requireActivity())
                        allowOrientationChange(requireActivity())
                    }
                }
            }
            is OrientationControlStrategy.ManualWithOrientationManager -> {
                with(overlayController) {
                    onAppear { strategy.onAppear(orientationManager) }
                    onDisappear { strategy.onDisappear(orientationManager) }
                }
            }
            is OrientationControlStrategy.Manual -> {
                with(overlayController) {
                    onAppear(strategy.onAppear)
                    onDisappear(strategy.onDisappear)
                }
            }
            is OrientationControlStrategy.Ignore -> {
                with(overlayController) {
                    onAppear { /* Ignore onAppear event */ }
                    onDisappear { /* Ignore onDisappear event */ }
                }
            }
        }
    }

    /**
     * A WebView will be displayed on top of the passed activity.
     * If by the time the main SDK methods are called, the activity is not passed,
     * an [IllegalArgumentException] will be thrown.
     *
     * If the activity was recreated as a result of a configuration change,
     * the WebView will be immediately attached again on top of the activity and the
     * state of the WebView will be restored. To ensure the safety of maintaining the state,
     * it is recommended to store the [SurveyMaster] class
     * in a place that is undergoing a configuration change.
     * For example in ViewModel.
     */
    fun attachToActivity(activity: Activity) {
        this.activity = activity
        if (overlayController.isShowing) {
            overlayController.restoreWebView(requireActivity())
        }
    }

    /**
     * If WebView shown, it will be immediately detached.
     */
    fun detachFromActivity() {
        overlayController.detachWebView(requireActivity())
        this.activity = null
    }

    private fun checkSurveyLifetime() {

        if (hasSurveyCalledAt == 0L) return

        val currentTimeInSeconds = System.currentTimeMillis() / 1000
        val hasSurveyCalledAtInSeconds = hasSurveyCalledAt / 1000
        val differenceInSeconds = currentTimeInSeconds - hasSurveyCalledAtInSeconds

        if (differenceInSeconds > 10 * 60 /* 10 minutes */) {
            throw SurveyClient.SurveyClientException("Survey lifetime exceeded (10 minutes)")
        } else {
            /* Reset timer */
            hasSurveyCalledAt = 0
        }
    }

    private fun resetVariables() {
        hasSurveyCalledAt = 0
        isHasShowSurveyCalled = false
        isShowSurveyCalled = false
        pageReadyFlow.update { false }
    }

    private fun attachOnPageReadyObserver() {
        pageReadyFlow
            .onEach { isReady -> if (isReady && isShowSurveyCalled) overlayController.show() }
            .launchIn(scope)
    }

    private fun attachSurveyCallbackObserver() {
        callbackFlow
            .onStart { logger.i { "Survey callback observer attached" } }
            /* Ignore callback because that was triggered without calling hasSurvey() */
            .filter { surveyCallback ->
                !(!isHasShowSurveyCalled && surveyCallback.event is SurveyEvent.SurveyAvailable)
            }
            .onEach { callback -> handleCallback(callback) }
            .onCompletion { logger.i { "Survey callback observer detached" } }
            .launchIn(scope)
    }

    private fun handleCallback(surveyCallback: SurveyCallback) {
        logger.i { "Handling survey callback..." }
        val data = surveyCallback.data

        when(surveyCallback.event) {

            SurveyEvent.SurveyAvailable -> { onSurveyAvailable.invoke() }

            SurveyEvent.LoadFail -> { onLoadFail.invoke() }

            SurveyEvent.SurveyFail -> { onSurveyFail.invoke() }

            SurveyEvent.SurveyStart -> { onSurveyStart.invoke() }

            SurveyEvent.SurveySuccess -> {
                val n = try { JSONObject(data).getJSONObject("body").getInt("nq") } catch (e: Exception) { null } ?: -1
                onSurveySuccess.invoke(n)
            }

            SurveyEvent.SurveyUnavailable -> { onSurveyUnavailable.invoke() }

            SurveyEvent.PageReady -> { pageReadyFlow.update { true } }

            SurveyEvent.LinkClicked -> { onLinkClicked(data) }

            SurveyEvent.None -> Unit
        }
    }

    private fun sendCallback(surveyCallback: SurveyCallback?) {

        if (surveyCallback == null) return

        // Skip unrecognized events
        if (surveyCallback.event == SurveyEvent.None) return

        // Ignore callback because that was triggered without calling hasSurvey()
        if (!isHasShowSurveyCalled && surveyCallback.event is SurveyEvent.SurveyAvailable) return

        scope.launch {
            _callbackFlow.emit(surveyCallback)
        }
    }

    private inner class SurveyWebViewCallbackImpl: SurveyWebViewCallback {

        override fun onError(errorData: String?) {

        }

        override fun onHttpError(httpStatus: String?) {

        }

        override fun onPageStarted(url: String?) {
            overlayController.evaluateJavascript(addEventListenerScript)
        }

        override fun onPageLoaded(url: String?) {

        }

        override fun onReceiveMessage(data: String, type: SurveyJavaScriptListener.MessageType) {
            when(type) {
                is SurveyJavaScriptListener.MessageType.Survey -> {
                    val surveyCallback = parseToSurveyCallback(data)
                    sendCallback(surveyCallback)
                }
                is SurveyJavaScriptListener.MessageType.CustomData -> {
                    val customCallback = parseCustomDataToSurveyCallback(data)
                    sendCallback(customCallback)
                }
            }
        }
    }

    private fun requireActivity() = requireNotNull(activity) { "Activity must be non-null" }

    private fun checkIsInitialized() {
        if (apiKey.isInvalid()) {
            throw SurveyClient.SurveyClientException("SDK not initialized")
        }
    }

    private fun buildSurveyUrl(): String {
        return Environment.surveyUrl +
                "?isWebView=1" +
                "&send_from_page=1" +
                "&platform=${platform.platformId}" +
                "&gid=${apiKey}" +
                "&uid=${userId}" +
                "&version=${Environment.sdkVersion}" +
                "&lastSurvey=-1"
    }
}