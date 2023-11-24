package com.kukushka.sdk.model

sealed class Platform(val platformId: Int) {

    object IOS: Platform(platformId = 0)

    object Android: Platform(platformId = 1)

    object Windows: Platform(platformId = 2)

    object Mac: Platform(platformId = 3)

    object Linux: Platform(platformId = 4)

    object Unknown: Platform(platformId = 5)

}
