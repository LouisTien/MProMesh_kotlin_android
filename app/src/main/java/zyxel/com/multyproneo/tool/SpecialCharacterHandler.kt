package zyxel.com.multyproneo.tool

import java.lang.Character
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by LouisTien on 2019/6/3.
 */
class SpecialCharacterHandler
{
    companion object
    {
        var filterCharacter = ""

        fun containsSpecialCharacter(source: String): Matcher? = Pattern.compile(filterCharacter).matcher(source)

        fun containsEmoji(source: String): Boolean
        {
            for(c in source)
            {
                when(c.toInt()) { Character.SURROGATE.toInt() or Character.OTHER_SYMBOL.toInt(), in 256 .. Int.MAX_VALUE -> return true }
            }
            return false
        }

        fun checkEmptyTextValue(srcStr: String): String
        {
            var desStr = "N/A"
            if(srcStr != "" && srcStr != " ") { desStr = srcStr }
            return desStr
        }
    }
}