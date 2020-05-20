package zyxel.com.multyproneo.tool

import zyxel.com.multyproneo.util.AppConfig
import java.lang.Character
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by LouisTien on 2019/6/3.
 */
object SpecialCharacterHandler
{

    fun containsEmoji(source: String): Boolean
    {
        for(c in source)
        {
            when(c.toInt()) { Character.SURROGATE.toInt() or Character.OTHER_SYMBOL.toInt(), in 256 .. Int.MAX_VALUE -> return true }
        }
        return false
    }

    fun containsExcludeASCII(source: String): Boolean
    {
        val len = source.length
        for(i in 0 until len)
        {
            val codePoint = source[i]
            val cValue = codePoint.toInt()

            if(cValue == Character.SURROGATE.toInt() || cValue == Character.OTHER_SYMBOL.toInt())
                return true

            //check if characters is out of ASCII Extend Range
            if(cValue > 126 || cValue < 32)
                return true
        }
        return false
    }

    fun containsSpecialCharacter(str: String): Boolean
    {
        val p = Pattern.compile("[\"'`<>^\$]")
        val m = p.matcher(str)
        return m.find()
    }

    fun checkEmptyTextValue(srcStr: String): String
    {
        var desStr = "N/A"
        if(srcStr != "" && srcStr != " ") { desStr = srcStr }
        return desStr
    }

    fun handleSpecialCharacterForJSON(srcStr: String): String
    {
        var desStr = srcStr
        desStr = srcStr.replace("\\", "\\\\")
        desStr = desStr.replace("\"", "\\\"")
        return desStr
    }

    fun handleSpecialCharacterForWiFiQRCode(srcStr: String): String
    {
        var desStr = srcStr
        desStr = srcStr.replace("\\", "\\\\")
        //desStr = desStr.replace("\"", "\\\"")
        desStr = desStr.replace(";", "\\;")
        desStr = desStr.replace(",", "\\,")
        desStr = desStr.replace(":", "\\:")
        return desStr
    }
}