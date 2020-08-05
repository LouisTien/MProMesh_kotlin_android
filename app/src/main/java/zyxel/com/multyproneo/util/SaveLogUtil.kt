package zyxel.com.multyproneo.util

import android.os.Build
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object SaveLogUtil
{
    var filePath: File? = null
    var fileFWLog: File? = null
    private lateinit var file: File
    private lateinit var formatterDate: SimpleDateFormat
    private lateinit var formatterSec: SimpleDateFormat
    private var fileName = ""

    fun init()
    {
        try
        {
            formatterDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val curDate = Date(System.currentTimeMillis())
            fileName = formatterDate.format(curDate)
            file = File(filePath, "$fileName.txt") // /storage/emulated/0/Android/data/zyxel.com.multyproneo/files/2020-07-06.txt
            val outputStream = FileOutputStream(file, true)
            formatterSec = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault())
            val curDateStr = formatterSec.format(curDate)
            val log = StringBuilder()
            log.append("\n\n\n--------------$curDateStr---------------\n\n")
            log.append("BRAND : ${Build.BRAND}\n")
            log.append("MANUFACTURER : ${Build.MANUFACTURER}\n")
            log.append("MODEL : ${Build.MODEL}\n")
            log.append("SDK VERSION : ${Build.VERSION.SDK_INT}\n")
            log.append("OS VERSION : ${Build.VERSION.RELEASE}\n\n")
            outputStream.write(log.toString().toByteArray())
            outputStream.close()
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun writeLog(tag: String, msg: String)
    {
        try
        {
            val curDate = Date(System.currentTimeMillis())
            val curDateStr = formatterSec.format(curDate)
            val outputStream = FileOutputStream(file, true)
            val log = StringBuilder()
            log.append("$curDateStr - [$tag] : $msg\n")
            outputStream.write(log.toString().toByteArray())
            outputStream.close()
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun deleteOldFiles()
    {
        val suffix = ".txt"
        val path = file.parent
        val sevenDayBefore = System.currentTimeMillis() - (AppConfig.DelDayBefore * 86400000)

        File(path).walkTopDown().forEach{
            if(it.isFile && it.name.endsWith(suffix))
            {
                val name = it.name.replace(suffix, "")
                val date = formatterDate.parse(name)
                val time = date.time
                if(time < sevenDayBefore) it.delete()
            }
        }
    }

    fun zipFiles(): File
    {
        val logFile = File(filePath?.parent, AppConfig.APP_LOG_NAME)
        if(logFile.exists())
            logFile.delete()

        val zipFile = ZipFile("${filePath?.parent}/${AppConfig.APP_LOG_NAME}")

        try
        {
            val suffix = ".txt"
            val path = file.parent
            val filesToAdd = ArrayList<File>()

            File(path).walkTopDown().forEach{
                if(it.isFile && it.name.endsWith(suffix))
                    filesToAdd.add(it)
            }

            if(GlobalData.logFileDeliver && fileFWLog != null)
                filesToAdd.add(fileFWLog!!)

            val parameters = ZipParameters()
            with(parameters)
            {
                compressionMethod = Zip4jConstants.COMP_DEFLATE
                compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL
                isEncryptFiles = true
                encryptionMethod = Zip4jConstants.ENC_METHOD_AES
                aesKeyStrength = Zip4jConstants.AES_STRENGTH_256
                setPassword(AppConfig.PASSWORD_FOR_ZIP_FILE)
            }

            zipFile.addFiles(filesToAdd, parameters)
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }

        return zipFile.file
    }

    fun createFWLogFile()
    {
        fileFWLog = File(filePath?.parent, AppConfig.FW_LOG_NAME) // /storage/emulated/0/Android/data/zyxel.com.multyproneo/files/recv_syslog.tar.gz
        removeFWLogFile()
    }

    fun removeFWLogFile() = fileFWLog?.delete()

    fun writeToFWLogFile(array: ByteArray) = fileFWLog?.appendBytes(array)
}