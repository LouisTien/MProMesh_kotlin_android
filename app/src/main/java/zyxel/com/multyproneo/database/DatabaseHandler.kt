package zyxel.com.multyproneo.database

import android.app.Activity
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import android.util.Base64
import zyxel.com.multyproneo.model.GatewayProfile
import zyxel.com.multyproneo.tool.CryptTool
import zyxel.com.multyproneo.util.LogUtil
import java.io.UnsupportedEncodingException
import java.util.ArrayList

/**
 * Created by LouisTien on 2019/6/19.
 */
class DatabaseHandler(private var activity: Activity)
{
    private val TAG = javaClass.simpleName
    private lateinit var dbhelper: DatabaseHelper
    private lateinit var cursor: Cursor
    private var gatewayProfileArrayListDB = arrayListOf<GatewayProfile>()

    enum class GETINFOFROMDB
    {
        INFO_USERNAME,
        INFO_PASSWORD,
        INFO_USERDEFINENAME,
        INFO_OTHER
    }

    private fun openDatabase()
    {
        dbhelper = DatabaseHelper(activity)
    }

    private fun closeDatabase()
    {
        activity.stopManagingCursor(cursor)
        dbhelper.close()
    }

    private fun getDatabaseCursor()
    {
        val db = dbhelper.readableDatabase
        val columns = arrayOf(
                BaseColumns._ID,
                DatabaseContents.MODEL,
                DatabaseContents.VERSION,
                DatabaseContents.IP,
                DatabaseContents.SERIAL,
                DatabaseContents.PASSWORD,
                DatabaseContents.USERNAME,
                DatabaseContents.USERDEFINENAME,
                DatabaseContents.OTHER
        )
        cursor = db.query(DatabaseContents.TABLE_NAME, columns, null, null, null, null, null)
        activity.startManagingCursor(cursor)
    }

    fun getGatewayFromDB(): ArrayList<GatewayProfile>
    {
        openDatabase()
        getDatabaseCursor()
        gatewayProfileArrayListDB.clear()
        while(cursor.moveToNext())
        {
            val gatewayProfile = GatewayProfile()
            with(gatewayProfile)
            {
                idInDB = cursor.getString(0)
                modelName = cursor.getString(1)
                firmwareVersion = cursor.getString(2)
                IP = cursor.getString(3)
                serial = cursor.getString(4)
                password = cursor.getString(5)
                userName = cursor.getString(6)
                userDefineName = cursor.getString(7)
            }
            gatewayProfileArrayListDB.add(gatewayProfile)
        }

        for(i in gatewayProfileArrayListDB.indices)
        {
            LogUtil.d(TAG, "gateway profile on db id = " + gatewayProfileArrayListDB[i].idInDB)
            LogUtil.d(TAG, "gateway profile on db model = " + gatewayProfileArrayListDB[i].modelName)
            LogUtil.d(TAG, "gateway profile on db serial = " + gatewayProfileArrayListDB[i].serial)
            LogUtil.d(TAG, "gateway profile on db password = " + gatewayProfileArrayListDB[i].password)
            LogUtil.d(TAG, "gateway profile on db username = " + gatewayProfileArrayListDB[i].userName)
            LogUtil.d(TAG, "gateway profile on db userDefineName = " + gatewayProfileArrayListDB[i].userDefineName)
        }

        return gatewayProfileArrayListDB
    }

    private fun addToDB(deviceInfo: GatewayProfile)
    {
        val db = dbhelper.writableDatabase
        val values = ContentValues()
        with(values)
        {
            put(DatabaseContents.MODEL, deviceInfo.modelName)
            put(DatabaseContents.VERSION, deviceInfo.firmwareVersion)
            put(DatabaseContents.IP, deviceInfo.IP)
            put(DatabaseContents.SERIAL, deviceInfo.serial)
            put(DatabaseContents.PASSWORD, deviceInfo.password)
            put(DatabaseContents.USERNAME, deviceInfo.userName)
            put(DatabaseContents.USERDEFINENAME, deviceInfo.userDefineName)
        }
        db.insert(DatabaseContents.TABLE_NAME, null, values)
    }

    private fun replaceToDB(rowID: String, deviceInfo: GatewayProfile)
    {
        val db = dbhelper.writableDatabase
        val values = ContentValues()
        with(values)
        {
            put(DatabaseContents.MODEL, deviceInfo.modelName)
            put(DatabaseContents.VERSION, deviceInfo.firmwareVersion)
            put(DatabaseContents.IP, deviceInfo.IP)
            put(DatabaseContents.SERIAL, deviceInfo.serial)
            put(DatabaseContents.PASSWORD, deviceInfo.password)
            put(DatabaseContents.USERNAME, deviceInfo.userName)
            put(DatabaseContents.USERDEFINENAME, deviceInfo.userDefineName)
        }
        db.update(DatabaseContents.TABLE_NAME, values, BaseColumns._ID + "=" + rowID, null)
    }

    private fun deleteToDB(id: String)
    {
        val db = dbhelper.writableDatabase
        db.delete(DatabaseContents.TABLE_NAME, BaseColumns._ID + "=" + id, null)
    }

    fun getDevicePasswordFromDB(serial: String): String
    {
        LogUtil.d(TAG,"getDevicePasswordFromDB, search serial:$serial")
        return getInformationFromDB(GETINFOFROMDB.INFO_PASSWORD, serial)
    }

    fun getDeviceUserNameFromDB(serial: String): String
    {
        LogUtil.d(TAG, "getDeviceUserNameFromDB, search serial:$serial")
        return getInformationFromDB(GETINFOFROMDB.INFO_USERNAME, serial)
    }

    fun getDeviceUserDefineNameFromDB(serial: String): String
    {
        LogUtil.d(TAG, "getDeviceUserDefineNameFromDB, search serial:$serial")
        return getInformationFromDB(GETINFOFROMDB.INFO_USERDEFINENAME, serial)
    }

    fun getInformationFromDB(infoTag: GETINFOFROMDB, serial: String): String
    {
        var retStr = ""
        var isExist = false

        getGatewayFromDB()

        if(gatewayProfileArrayListDB.size != 0)
        {
            for(i in gatewayProfileArrayListDB.indices)
            {
                if(gatewayProfileArrayListDB[i].serial.equals(serial, ignoreCase = true))
                {
                    when(infoTag)
                    {
                        DatabaseHandler.GETINFOFROMDB.INFO_PASSWORD -> retStr = gatewayProfileArrayListDB[i].password
                        DatabaseHandler.GETINFOFROMDB.INFO_USERNAME -> retStr = gatewayProfileArrayListDB[i].userName
                        DatabaseHandler.GETINFOFROMDB.INFO_USERDEFINENAME -> retStr = gatewayProfileArrayListDB[i].userDefineName
                    }
                    isExist = true
                    break
                }
                else
                    isExist = false
            }

            if(isExist)
            {
                var decryptedData: String? = null
                try
                {
                    decryptedData = CryptTool.DecryptAES(
                            CryptTool.IvAES.toByteArray(charset("UTF-8")),
                            CryptTool.KeyAES.toByteArray(charset("UTF-8")),
                            Base64.decode(retStr.toByteArray(charset("UTF-8")), Base64.DEFAULT))
                }
                catch(e: UnsupportedEncodingException)
                {
                    e.printStackTrace()
                }

                if(decryptedData != null)
                    retStr = decryptedData
            }
        }

        return retStr
    }

    fun updateInformationToDB(gatewayInfo: GatewayProfile)
    {
        var isExist = false
        var encryptedPassword: String? = null
        var encryptedUserName: String? = null
        var encryptedUserDefineName: String? = null

        getGatewayFromDB()

        LogUtil.d(TAG, "input password = " + gatewayInfo.password)
        LogUtil.d(TAG, "input userName = " + gatewayInfo.userName)
        LogUtil.d(TAG, "input userDefineName = " + gatewayInfo.userDefineName)

        try
        {
            encryptedPassword = CryptTool.EncryptAES(
                    CryptTool.IvAES.toByteArray(charset("UTF-8")),
                    CryptTool.KeyAES.toByteArray(charset("UTF-8")),
                    gatewayInfo.password.toByteArray(charset("UTF-8")))

            encryptedUserName = CryptTool.EncryptAES(
                    CryptTool.IvAES.toByteArray(charset("UTF-8")),
                    CryptTool.KeyAES.toByteArray(charset("UTF-8")),
                    gatewayInfo.userName.toByteArray(charset("UTF-8")))

            encryptedUserDefineName = CryptTool.EncryptAES(
                    CryptTool.IvAES.toByteArray(charset("UTF-8")),
                    CryptTool.KeyAES.toByteArray(charset("UTF-8")),
                    gatewayInfo.userDefineName.toByteArray(charset("UTF-8")))
        }
        catch(e: UnsupportedEncodingException)
        {
            e.printStackTrace();
        }

        LogUtil.d(TAG,"encrypted password = $encryptedPassword")
        LogUtil.d(TAG,"encrypted userName = $encryptedUserName")
        LogUtil.d(TAG,"encrypted userDefineName = $encryptedUserDefineName")

        gatewayInfo.password = encryptedPassword!!
        gatewayInfo.userName = encryptedUserName!!
        gatewayInfo.userDefineName = encryptedUserDefineName!!

        if(gatewayProfileArrayListDB.size == 0)
        {
            LogUtil.d(TAG,"db data size is zero, add it")
            addToDB(gatewayInfo)
        }
        else if(gatewayProfileArrayListDB.size > 0)
        {
            LogUtil.d(TAG,"db data size is not zero, find if exist")
            var id = ""
            for(i in gatewayProfileArrayListDB.indices)
            {
                if(gatewayProfileArrayListDB[i].serial.equals(gatewayInfo.serial, ignoreCase = true))
                {
                    id = gatewayProfileArrayListDB[i].idInDB
                    isExist = true
                    break
                }
            }

            if(isExist)
            {
                LogUtil.d(TAG,"find exist data in db, update it")
                replaceToDB(id, gatewayInfo)
            }
            else
            {
                LogUtil.d(TAG,"does not find exist data in db, add it")
                addToDB(gatewayInfo)
            }
        }

        getGatewayFromDB()

        gatewayInfo.password = getDevicePasswordFromDB(gatewayInfo.serial)
        gatewayInfo.userName = getDeviceUserNameFromDB(gatewayInfo.serial)
        gatewayInfo.userDefineName = getDeviceUserDefineNameFromDB(gatewayInfo.serial)
    }

    fun deleteInformationToDB(gatewayInfo: GatewayProfile)
    {
        getGatewayFromDB()

        for(i in gatewayProfileArrayListDB.indices)
        {
            if(gatewayProfileArrayListDB[i].serial.equals(gatewayInfo.serial, ignoreCase = true))
            {
                LogUtil.d(TAG,"delete id = " + gatewayProfileArrayListDB[i].idInDB)
                deleteToDB(gatewayProfileArrayListDB[i].idInDB)
            }
        }

        getGatewayFromDB()
    }
}