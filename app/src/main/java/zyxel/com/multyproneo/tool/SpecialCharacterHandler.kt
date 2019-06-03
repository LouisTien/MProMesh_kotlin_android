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
        public var filterCharacter = ""

        public fun containsSpecialCharacter(source: String): Matcher? = Pattern.compile(filterCharacter).matcher(source)

        public fun containsEmoji(source: String): Boolean
        {
            for (c in source)
            {
                when (c.toInt()) { Character.SURROGATE.toInt() or Character.OTHER_SYMBOL.toInt(), in 256 .. Int.MAX_VALUE -> return true }
            }
            return false
        }

        public fun checkEmptyTextValue(srcStr: String): String
        {
            var desStr = "N/A"
            if (!srcStr.equals("") && !srcStr.equals(" ")) { desStr = srcStr }
            return desStr
        }
    }
}