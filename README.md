# Kukushka Android SDK

**Kukushka SDK** makes it easy to give players survey tasks followed by a reward. The SDK can be installed and set up in minutes.

## Usage

**Just create SurveyMaster:**
```kotlin
val surveyMaster: SurveyMaster = SurveyMaster()
surveyMaster.setUserId("USER_ID")
surveyMaster.init("API_KEY")
```

Attach SurveyMaster to your activity
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { /* content */ }
    surveyMaster.attachToActivity(this@MainActivity)
}

override fun onDestroy() {
    super.onDestroy()
    surveyMaster.detachFromActivity()
}
```

Now you can call two main methods of Kykyshka SDK to preload and show Survey for user:
```kotlin
surveyMaster.hasSurvey()   // To Check and Preload Available Survey for this user
surveyMaster.showSurvey()  // To Show Survey for user
```
**SDK Callbacks:**<br/>
Kukushka SDK has a lot of different Callbacks for your game. Use this callbacks to detect Survey Complete or problems with loading.

| Callback             | Parameters                   | Usage                                                                  |
|----------------------|------------------------------|------------------------------------------------------------------------|
| **OnSurveyStart** | -                            | Called when user started survey                                        |
| **OnSurveyAvailable** | -                            | Called after preloading if surveys available                           |
| **OnSurveyUnavailable** | -                            | Called after preloading if surveys unavailable                         |
| **OnSurveySuccess** | **Bool** or **null**          | Called when user complete survey. May contain additional data.         |
| **OnFail**    | **Any** or **null** | Called when user got error in the survey. May contain additional data. |
| **OnLoadFail** | -                            | Called when Survey has loading error                                   |
| **OnError** | -                            | On General SDK Errors Callback   

**Callbacks Example:**
```swift
with(surveyMaster) {
    // Add Survey Callbacks
    setOnSurveyFailListener { Log.d(TAG, "Последний опрос не пройден") }
    setOnSurveyStartListener { Log.d(TAG, "Прохождение опроса началось") }
    setOnSurveyLoadFailListener { Log.d(TAG, "Ошибка загрузки") }
    setOnSurveySuccessListener { Log.d(TAG, "Последний опрос пройден успешно") }

    // Add Preloading Callbacks
    setOnSurveyAvailableListener { Log.d(TAG, "Подходящий опрос найден") }
    setOnSurveyUnavailableListener { Log.d(TAG, "Подходящий опрос не найден") }
}
```
Also callbacks events can be received as a flow:
```kotlin
scope.launch {
    surveyMaster.callbackFlow.collect { callback ->
        Log.d(TAG, callback.toString())
    }
}
```
**Screen Orientation:**

Surveys require showing only in portrait mode.

To control orientation you need to provide orientation control strategy.

```kotlin
surveyMaster.setOrientationControlStrategy(OrientationControlStrategy.Auto)
```

To change the configuration to portrait mode (if necessary), 2 callbacks are provided:
- onAppear - WebView is now shown and you need to change the orientation.
- onDisappear - WebView is no longer visible and you need to return to the previous.

Strategies:
- OrientationControlStrategy.Auto - Configuration change control will be provided by the SDK.
- OrientationControlStrategy.ManualWithOrientationManager - Required callbacks
must be provided by yourself. OrientationManager (a class inside the SDK, necessary to
simplify the process of working with orientations) will be provided to each callback
to simplify the process.
- OrientationControlStrategy.Manual - Required callbacks must be provided by yourself.
The SDK does not provide auxiliary ways to control orientation.
- OrientationControlStrategy.Ignore - Ignore configuration changes callback (not recommended).

Please, note: If your application is running in landscape mode, don't forget that the class
may be destroyed as a result of a configuration change.

## Example

To run the example project, clone the repo, and run `app` module from the root directory.

## Requirements

- Android SDK 21+

## Installation

Kukushka Android SDK is available through [Maven Central](https://central.sonatype.com/). To install
it, simply add the following line to your Podfile:

```kotlin
implementation("com.kukushka.sdk:kukusha-android-sdk:<version>")
```

## Author

Name, contacts

## License

Kukushka Android SDK is available under the MIT license. See the LICENSE file for more info.
