package zyxel.com.multyproneo.util

import android.app.Activity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by LouisTien on 2019/6/17.
 */
object OUIUtil
{
    private const val OUI_URL = "https://linuxnet.ca/ieee/oui/nmap-mac-prefixes"
    private const val OUI_FILENAME = "/oui.txt"

    fun getOUI(activity: Activity, Mac: String): String
    {
        var oui = ""
        val mac = Mac

        try
        {
            val br = BufferedReader(FileReader(activity.filesDir.absolutePath + OUI_FILENAME))
            val macArr = mac.split(":").toTypedArray()
            val newMac = macArr[0] + macArr[1] + macArr[2]
            /*BufferedReader(FileReader(activity.filesDir.absolutePath + OUI_FILENAME)).use{ r ->
                r.lineSequence().forEach{
                    var lineArr = it.split("\t")
                    if(lineArr[0].toLowerCase().equals(newMac.toLowerCase()))
                    {
                        for(i in 1 until lineArr.size)
                            oui += lineArr[i] + ""

                    }
                }
            }*/
            while(true)
            {
                val line = br.readLine() ?: break
                val lineArr = line.split("\t")
                if(lineArr[0].toLowerCase().equals(newMac.toLowerCase()))
                {
                    for(i in 1 until lineArr.size)
                        oui += lineArr[i] + ""
                    break
                }
            }

            br.close()
        }
        catch(e: IOException)
        {
            e.printStackTrace()
        }
        return oui
    }

    fun executeGetMacOUITask(activity: Activity)
    {
        var connect = false
        val sb = StringBuffer()

        doAsync{
            var conn: HttpURLConnection? = null
            try
            {
                conn = URL(OUI_URL).openConnection() as HttpURLConnection
                with(conn)
                {
                    connectTimeout = 20000
                    readTimeout = 60000
                    connect()
                }

                if(conn.responseCode == HttpURLConnection.HTTP_OK)
                {
                    val br = BufferedReader(InputStreamReader(conn.inputStream))
                    while(true)
                    {
                        val line = br.readLine() ?: break
                        sb.append(line + "\n")
                    }
                    br.close()
                }
                connect = true
            }
            catch(e: IOException)
            {
                e.printStackTrace()
                connect = false
            }
            finally
            {
                conn?.disconnect()
            }

            uiThread{
                if(connect)
                {
                    try
                    {
                        checkDirExist(activity.filesDir.absolutePath!!)
                        val bw = BufferedWriter(FileWriter(activity.filesDir.absolutePath + OUI_FILENAME, false))
                        bw.write(sb.toString())
                        bw.close()
                    }
                    catch(e: IOException)
                    {
                        e.printStackTrace()
                    }
                }

                val ouiTxt = File(activity.filesDir.absolutePath + OUI_FILENAME)
                if(!ouiTxt.exists())
                {
                    try
                    {
                        val rawID = activity.resources.getIdentifier("oui", "raw", activity.packageName)
                        val inputStream = activity.resources.openRawResource(rawID)
                        val outputStream = BufferedOutputStream(FileOutputStream(ouiTxt))
                        val bytesArray = ByteArray(inputStream!!.available())
                        var bytesRead = -1
                        while(true)
                        {
                            bytesRead = inputStream.read(bytesArray)
                            if(bytesRead == -1) break
                            outputStream.write(bytesArray, 0, bytesRead)
                        }
                        outputStream.close()
                        inputStream.close()
                    }
                    catch(e: IOException)
                    {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun checkDirExist(path: String)
    {
        val file = File(path)
        if(!file.exists())
            file.mkdir()
    }
}