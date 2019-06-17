package zyxel.com.multyproneo.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by LouisTien on 2019/5/22.
 */
class SharedPreferencesUtil<T>(val context: Context, val name: String, val default: T) : ReadWriteProperty<Any?, T>
{
    private val prefs: SharedPreferences by lazy{ context.getSharedPreferences("name_device_setting", Context.MODE_PRIVATE) }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = findPreference(name, default)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = putPreference(name, value)

    private fun <T> findPreference(name: String, default: T) : T = with(prefs)
    {
        val res: Any = when(default)
        {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("This type can not be saved into SharedPreferencesUtil")
        }
        return res as T
    }

    private fun <T> putPreference(name: String, value: T) = with(prefs.edit())
    {
        when(value)
        {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can not be saved into SharedPreferencesUtil")
        }.apply()
    }
}