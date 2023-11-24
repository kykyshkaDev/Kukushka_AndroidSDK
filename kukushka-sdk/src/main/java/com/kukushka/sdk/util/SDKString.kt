package com.kukushka.sdk.util

typealias SDKString = String

internal fun SDKString.isInvalid() = isEmpty() || isBlank()

internal fun SDKString.isValid() = isNotEmpty() || isNotBlank()
