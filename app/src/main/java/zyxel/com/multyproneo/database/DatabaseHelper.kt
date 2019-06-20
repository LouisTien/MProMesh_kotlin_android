package zyxel.com.multyproneo.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

/**
 * Created by LouisTien on 2019/6/18.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "multypro.db", null, 1)
{
    override fun onCreate(db: SQLiteDatabase?)
    {
        val INIT_TABLE = ("CREATE TABLE " + DatabaseContents.TABLE_NAME + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DatabaseContents.MODEL + " CHAR, "
                + DatabaseContents.VERSION + " CHAR, "
                + DatabaseContents.IP + " CHAR, "
                + DatabaseContents.SERIAL + " CHAR, "
                + DatabaseContents.PASSWORD + " CHAR, "
                + DatabaseContents.USERNAME + " CHAR, "
                + DatabaseContents.USERDEFINENAME + " CHAR, "
                + DatabaseContents.OTHER + " CHAR);")
        db?.execSQL(INIT_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        val DROP_TABLE = "DROP TABLE IF EXISTS " + DatabaseContents.TABLE_NAME
        db?.execSQL(DROP_TABLE)
        onCreate(db)
    }
}