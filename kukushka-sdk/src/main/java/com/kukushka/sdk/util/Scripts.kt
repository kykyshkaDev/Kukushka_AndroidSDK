package com.kukushka.sdk.util

internal val addEventListenerScript = """
        window.addEventListener('message',(event) => {
            try {
                if (typeof event.data==='string') {
                    if (event.data.includes('surveyMaster')) {
                        var _data=JSON.parse(event.data);
                        if (_data.hasOwnProperty('surveyMaster')) {
                            Android.postSurveyMessage(event.data);
                        }
                    }
                } else {
                    Android.postCustomDataMessage(JSON.stringify(event.data));
                }
            } catch(e) {
                Android.postMessage('MESSAGE_ERROR');
                return;
            }
        }, 
        false
        );
    """.trimIndent()

internal val timerScript = """
    window.postMessage("getTime","*");
""".trimIndent()