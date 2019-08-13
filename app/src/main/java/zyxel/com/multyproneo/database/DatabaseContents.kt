package zyxel.com.multyproneo.database

import android.provider.BaseColumns

/**
 * Created by LouisTien on 2019/6/18.
 */
object DatabaseContents : BaseColumns
{
    const val TABLE_NAME = "gatewayinfo"
    const val MODEL = "model"
    const val VERSION = "version"
    const val IP = "ip"
    const val MAC = "mac"
    const val PASSWORD = "password"
    const val USERNAME = "username"
    const val USERDEFINENAME = "userdefinename"
    const val OTHER = "other"
}