package com.kukushka.sdk.logger

import android.util.Log
import com.kukushka.sdk.util.Environment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal fun taggedLogger(tag: String): LoggerDelegate = LoggerDelegate(tag)

internal class LoggerDelegate(
    tag: String,
    isDebug: Boolean = Environment.isDebug
): ReadOnlyProperty<Any?, Logger> {

    private val logger by lazy { Logger(tag, isDebug) }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Logger = logger

}

internal class Logger(
    private val tag: String,
    private val isDebug: Boolean
) {

    fun i(msg: String) {
        Log.i(tag, msg)
    }

    fun e(msg: String) {
        Log.e(tag, msg)
    }

    fun v(msg: String) {
        Log.v(tag, msg)
    }

    fun d(msg: String) {
        if (!isDebug) return
        Log.d(tag, msg)
    }

    fun w(msg: String) {
        Log.w(tag, msg)
    }

    fun i(block: () -> String) {
        Log.i(tag, block())
    }

    fun e(block: () -> String) {
        Log.e(tag, block())
    }

    fun v(block: () -> String) {
        Log.v(tag, block())
    }

    fun d(block: () -> String) {
        if (!isDebug) return
        Log.d(tag, block())
    }

    fun w(block: () -> String) {
        Log.w(tag, block())
    }
}
