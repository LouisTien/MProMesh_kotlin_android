package zyxel.com.multyproneo.util

import android.util.Log
import zyxel.com.multyproneo.BuildConfig

/**
 * Created by LouisTien on 2019/5/28.
 */
object LogUtil
{
    fun d(tag: String, msg: String)
    {
        if(BuildConfig.DEBUG) Log.d(tag, msg)
        if(AppConfig.SaveLog) SaveLogUtil.writeLog(tag, msg)
    }

    fun e(tag: String, msg: String)
    {
        if(BuildConfig.DEBUG) Log.e(tag, msg)
        if(AppConfig.SaveLog) SaveLogUtil.writeLog(tag, msg)
    }
}