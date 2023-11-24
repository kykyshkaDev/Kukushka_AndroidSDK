package com.kukushka.sdk.storage

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private const val PREFS_NAME = "kukushka_sdk_prefs_delegate"

internal fun Context.sharedString(name: String) = SharedStringDelegate(this, name)

internal fun Context.sharedInt(name: String) = SharedIntDelegate(this, name)

internal fun Context.sharedBoolean(name: String) = SharedBooleanDelegate(this, name)

internal fun Context.sharedLong(name: String) = SharedLongDelegate(this, name)

internal class SharedLongDelegate(
    context: Context,
    private val name: String,
    private val defaultValue: Long = 0L
): ReadWriteProperty<Any?, Long>, SharedDelegate(context) {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        return sharedPreferences.getLong(name, defaultValue)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        sharedPreferences.edit().putLong(name, value).apply()
    }
}

internal class SharedBooleanDelegate(
    context: Context,
    private val name: String,
    private val defaultValue: Boolean = false
): ReadWriteProperty<Any?, Boolean>, SharedDelegate(context) {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return sharedPreferences.getBoolean(name, defaultValue)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        sharedPreferences.edit().putBoolean(name, value).apply()
    }
}

internal class SharedStringDelegate(
    context: Context,
    private val name: String,
    private val defaultValue: String = ""
): ReadWriteProperty<Any?, String>, SharedDelegate(context) {

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return sharedPreferences.getString(name, defaultValue) ?: defaultValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        sharedPreferences.edit().putString(name, value).apply()
    }
}

internal class SharedIntDelegate(
    context: Context,
    private val name: String,
    private val defaultValue: Int = 0
): ReadWriteProperty<Any?, Int>, SharedDelegate(context) {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return sharedPreferences.getInt(name, defaultValue)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        sharedPreferences.edit().putInt(name, value).apply()
    }
}

internal abstract class SharedDelegate(context: Context) {

    protected val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}