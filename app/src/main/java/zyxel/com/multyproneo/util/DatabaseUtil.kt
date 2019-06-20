package zyxel.com.multyproneo.util

import android.app.Activity
import zyxel.com.multyproneo.database.DatabaseHandler

/**
 * Created by LouisTien on 2019/6/20.
 */
class DatabaseUtil
{
    companion object
    {
        private var DBHandler: DatabaseHandler? = null

        fun getDBHandler(activity: Activity): DatabaseHandler?
        {
            if(DBHandler == null)
                DBHandler = DatabaseHandler(activity)

            return DBHandler
        }
    }
}