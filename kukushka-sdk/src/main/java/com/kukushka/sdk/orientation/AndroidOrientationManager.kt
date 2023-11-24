package com.kukushka.sdk.orientation

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration


class AndroidOrientationManager: OrientationManager {

    private var savedOrientation = -1

    override fun saveOrientationState(activity: Activity) {
        savedOrientation = activity.resources.configuration.orientation
    }

    override fun resetToSavedOrientationState(activity: Activity) {

        if (savedOrientation == -1) return

        if (savedOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortrait(activity)
        } else {
            setAlbum(activity)
        }
    }

    override fun setAlbum(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun setPortrait(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
    }

    override fun allowOrientationChange(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}