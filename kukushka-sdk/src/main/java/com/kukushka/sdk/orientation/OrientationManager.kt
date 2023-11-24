package com.kukushka.sdk.orientation

import android.app.Activity

interface OrientationManager {

    fun saveOrientationState(activity: Activity)

    fun resetToSavedOrientationState(activity: Activity)

    fun allowOrientationChange(activity: Activity)

    fun setAlbum(activity: Activity)

    fun setPortrait(activity: Activity)

}