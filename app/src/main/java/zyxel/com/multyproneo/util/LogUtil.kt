package zyxel.com.multyproneo.util

import android.util.Log
import zyxel.com.multyproneo.BuildConfig

/**
 * Created by LouisTien on 2019/5/28.
 */
class LogUtil
{
    companion object
    {
        fun d(tag: String, msg: String)
        {
            if(BuildConfig.DEBUG) Log.d(tag, msg)
        }

        fun e(tag: String, msg: String)
        {
            if(BuildConfig.DEBUG) Log.e(tag, msg)
        }
    }
}