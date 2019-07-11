package zyxel.com.multyproneo.util

import android.app.Activity
import zyxel.com.multyproneo.database.DatabaseHandler

/**
 * Created by LouisTien on 2019/6/20.
 */
object DatabaseUtil
{
    private lateinit var instance: DatabaseHandler

    fun getInstance(activity: Activity): DatabaseHandler?
    {
        instance = DatabaseHandler(activity)
        return instance
    }
}