package zyxel.com.multyproneo.database

import android.app.Activity
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import android.util.Base64
import zyxel.com.multyproneo.model.GatewayInfo
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
    private var gatewayInfoArrayListDB = arrayListOf<GatewayInfo>()

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
                DatabaseContents.MAC,
                DatabaseContents.PASSWORD,
                DatabaseContents.USERNAME,
                DatabaseContents.USERDEFINENAME,
                DatabaseContents.OTHER
        )
        cursor = db.query(DatabaseContents.TABLE_NAME, columns, null, null, null, null, null)
        activity.startManagingCursor(cursor)
    }

    fun getGatewayFromDB(): ArrayList<GatewayInfo>
    {
        openDatabase()
        getDatabaseCursor()
        gatewayInfoArrayListDB.clear()
        while(cursor.moveToNext())
        {
            val gatewayInfo = GatewayInfo()
            with(gatewayInfo)
            {
                IdInDB = cursor.getString(0)
                ModelName = cursor.getString(1)
                SoftwareVersion = cursor.getString(2)
                IP = cursor.getString(3)
                MAC = cursor.getString(4)
                Password = cursor.getString(5)
                UserName = cursor.getString(6)
                UserDefineName = cursor.getString(7)
                OtherInfo = if(cursor.getString(8) == null) "N/A" else cursor.getString(8)
            }
            gatewayInfoArrayListDB.add(gatewayInfo)
        }

        for(i in gatewayInfoArrayListDB.indices)
        {
            LogUtil.d(TAG, "gateway info on db id = ${gatewayInfoArrayListDB[i].IdInDB}")
            LogUtil.d(TAG, "gateway info on db model = ${gatewayInfoArrayListDB[i].ModelName}")
            LogUtil.d(TAG, "gateway info on db mac = ${gatewayInfoArrayListDB[i].MAC}")
            LogUtil.d(TAG, "gateway info on db password = ${gatewayInfoArrayListDB[i].Password}")
            LogUtil.d(TAG, "gateway info on db account = ${gatewayInfoArrayListDB[i].UserName}")
            LogUtil.d(TAG, "gateway info on db userDefineName = ${gatewayInfoArrayListDB[i].UserDefineName}")
            LogUtil.d(TAG, "gateway info on db OtherInfo = ${gatewayInfoArrayListDB[i].OtherInfo}")
        }

        return gatewayInfoArrayListDB
    }

    private fun addToDB(deviceInfo: GatewayInfo)
    {
        val db = dbhelper.writableDatabase
        val values = ContentValues()
        with(values)
        {
            put(DatabaseContents.MODEL, deviceInfo.ModelName)
            put(DatabaseContents.VERSION, deviceInfo.SoftwareVersion)
            put(DatabaseContents.IP, deviceInfo.IP)
            put(DatabaseContents.MAC, deviceInfo.MAC)
            put(DatabaseContents.PASSWORD, deviceInfo.Password)
            put(DatabaseContents.USERNAME, deviceInfo.UserName)
            put(DatabaseContents.USERDEFINENAME, deviceInfo.UserDefineName)
            put(DatabaseContents.OTHER, deviceInfo.OtherInfo)
        }
        db.insert(DatabaseContents.TABLE_NAME, null, values)
    }

    private fun replaceToDB(rowID: String, deviceInfo: GatewayInfo)
    {
        val db = dbhelper.writableDatabase
        val values = ContentValues()
        with(values)
        {
            put(DatabaseContents.MODEL, deviceInfo.ModelName)
            put(DatabaseContents.VERSION, deviceInfo.SoftwareVersion)
            put(DatabaseContents.IP, deviceInfo.IP)
            put(DatabaseContents.MAC, deviceInfo.MAC)
            put(DatabaseContents.PASSWORD, deviceInfo.Password)
            put(DatabaseContents.USERNAME, deviceInfo.UserName)
            put(DatabaseContents.USERDEFINENAME, deviceInfo.UserDefineName)
            put(DatabaseContents.OTHER, deviceInfo.OtherInfo)
        }
        db.update(DatabaseContents.TABLE_NAME, values, BaseColumns._ID + "=" + rowID, null)
    }

    private fun deleteToDB(id: String)
    {
        val db = dbhelper.writableDatabase
        db.delete(DatabaseContents.TABLE_NAME, BaseColumns._ID + "=" + id, null)
    }

    fun getDevicePasswordFromDB(mac: String): String
    {
        LogUtil.d(TAG,"getDevicePasswordFromDB, search mac:$mac")
        return getInformationFromDB(GETINFOFROMDB.INFO_PASSWORD, mac)
    }

    fun getDeviceUserNameFromDB(mac: String): String
    {
        LogUtil.d(TAG, "getDeviceUserNameFromDB, search mac:$mac")
        return getInformationFromDB(GETINFOFROMDB.INFO_USERNAME, mac)
    }

    fun getDeviceUserDefineNameFromDB(mac: String): String
    {
        LogUtil.d(TAG, "getDeviceUserDefineNameFromDB, search mac:$mac")
        return getInformationFromDB(GETINFOFROMDB.INFO_USERDEFINENAME, mac)
    }

    fun getInformationFromDB(infoTag: GETINFOFROMDB, mac: String): String
    {
        var retStr = ""
        var secretInfoStr = ""
        var isExist = false
        var IvAES = CryptTool.IvAESDefault
        var KeyAES = CryptTool.KeyAESDefault

        getGatewayFromDB()

        if(gatewayInfoArrayListDB.size != 0)
        {
            for(i in gatewayInfoArrayListDB.indices)
            {
                if(gatewayInfoArrayListDB[i].MAC.equals(mac, ignoreCase = true))
                {
                    secretInfoStr = gatewayInfoArrayListDB[i].OtherInfo
                    if(secretInfoStr != "N/A" && secretInfoStr.length >= 64)
                    {
                        KeyAES = secretInfoStr.substring(32, 48)
                        IvAES = secretInfoStr.substring(48, 64)
                    }
                    LogUtil.pd(TAG,"[DecryptAES]key:$KeyAES")
                    LogUtil.pd(TAG,"[DecryptAES]iv:$IvAES")

                    when(infoTag)
                    {
                        GETINFOFROMDB.INFO_PASSWORD -> retStr = gatewayInfoArrayListDB[i].Password
                        GETINFOFROMDB.INFO_USERNAME -> retStr = gatewayInfoArrayListDB[i].UserName
                        GETINFOFROMDB.INFO_USERDEFINENAME -> retStr = gatewayInfoArrayListDB[i].UserDefineName
                        else -> {}
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
                            IvAES.toByteArray(charset("UTF-8")),
                            KeyAES.toByteArray(charset("UTF-8")),
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

        LogUtil.d(TAG,"[getInformationFromDB]: infoTag:$infoTag, retStr:$retStr")

        return retStr
    }

    fun updateInformationToDB(gatewayInfo: GatewayInfo)
    {
        var isExist = false
        var encryptedPassword: String? = null
        var encryptedUserName: String? = null
        var encryptedUserDefineName: String? = null

        getGatewayFromDB()

        LogUtil.pd(TAG, "input password = " + gatewayInfo.Password)
        LogUtil.pd(TAG, "input userName = " + gatewayInfo.UserName)
        LogUtil.pd(TAG, "input userDefineName = " + gatewayInfo.UserDefineName)

        try
        {
            encryptedPassword = CryptTool.EncryptAES(
                    CryptTool.IvAES.toByteArray(charset("UTF-8")),
                    CryptTool.KeyAES.toByteArray(charset("UTF-8")),
                    gatewayInfo.Password.toByteArray(charset("UTF-8")))

            encryptedUserName = CryptTool.EncryptAES(
                    CryptTool.IvAES.toByteArray(charset("UTF-8")),
                    CryptTool.KeyAES.toByteArray(charset("UTF-8")),
                    gatewayInfo.UserName.toByteArray(charset("UTF-8")))

            encryptedUserDefineName = CryptTool.EncryptAES(
                    CryptTool.IvAES.toByteArray(charset("UTF-8")),
                    CryptTool.KeyAES.toByteArray(charset("UTF-8")),
                    gatewayInfo.UserDefineName.toByteArray(charset("UTF-8")))

            LogUtil.d(TAG,"encrypted password = $encryptedPassword")
            LogUtil.d(TAG,"encrypted userName = $encryptedUserName")
            LogUtil.d(TAG,"encrypted userDefineName = $encryptedUserDefineName")

            gatewayInfo.Password = encryptedPassword!!
            gatewayInfo.UserName = encryptedUserName!!
            gatewayInfo.UserDefineName = encryptedUserDefineName!!
            gatewayInfo.OtherInfo =
                    CryptTool.getRandomString(16) + //fake data
                    CryptTool.getRandomString(16) + //fake data
                    CryptTool.KeyAES +
                    CryptTool.IvAES +
                    CryptTool.getRandomString(16) + //fake data
                    CryptTool.getRandomString(16) + //fake data
                    CryptTool.getRandomString(16)   //fake data

            LogUtil.d(TAG,"input otherInfo = ${gatewayInfo.OtherInfo}")
        }
        catch(e: UnsupportedEncodingException)
        {
            e.printStackTrace();
        }

        if(gatewayInfoArrayListDB.size == 0)
        {
            LogUtil.d(TAG,"db data size is zero, add it")
            addToDB(gatewayInfo)
        }
        else if(gatewayInfoArrayListDB.size > 0)
        {
            LogUtil.d(TAG,"db data size is not zero, find if exist")
            var id = ""
            for(i in gatewayInfoArrayListDB.indices)
            {
                if(gatewayInfoArrayListDB[i].MAC.equals(gatewayInfo.MAC, ignoreCase = true))
                {
                    id = gatewayInfoArrayListDB[i].IdInDB
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

        gatewayInfo.Password = getDevicePasswordFromDB(gatewayInfo.MAC)
        gatewayInfo.UserName = getDeviceUserNameFromDB(gatewayInfo.MAC)
        gatewayInfo.UserDefineName = getDeviceUserDefineNameFromDB(gatewayInfo.MAC)
    }

    fun deleteInformationToDB(gatewayInfo: GatewayInfo)
    {
        getGatewayFromDB()

        for(i in gatewayInfoArrayListDB.indices)
        {
            if(gatewayInfoArrayListDB[i].MAC.equals(gatewayInfo.MAC, ignoreCase = true))
            {
                LogUtil.d(TAG,"delete id = " + gatewayInfoArrayListDB[i].IdInDB)
                deleteToDB(gatewayInfoArrayListDB[i].IdInDB)
            }
        }

        getGatewayFromDB()
    }
}