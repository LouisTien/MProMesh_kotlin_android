package zyxel.com.multyproneo.database

import android.provider.BaseColumns

/**
 * Created by LouisTien on 2019/6/18.
 */
class DatabaseContents : BaseColumns
{
    companion object
    {
        const val TABLE_NAME = "deviceinfo"
        const val MODEL = "model"
        const val VERSION = "version"
        const val IP = "ip"
        const val SERIAL = "serial"
        const val PASSWORD = "password"
        const val USERNAME = "username"
        const val USERDEFINENAME = "userdefinename"
        const val OTHER = "other"
    }
}