package com.kukushka.sdk.orientation

sealed class OrientationControlStrategy {

    data class ManualWithOrientationManager(
        val onAppear: (OrientationManager) -> Unit,
        val onDisappear: (OrientationManager) -> Unit
    ): OrientationControlStrategy()

    data class Manual(
        val onAppear: () -> Unit,
        val onDisappear: () -> Unit
    ): OrientationControlStrategy()

    object Auto: OrientationControlStrategy()

    object Ignore: OrientationControlStrategy()

}
