package zyxel.com.multyproneo.tool

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import zyxel.com.multyproneo.util.LogUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by LouisTien on 2019/6/11.
 */
object CommonTool
{
    private val TAG = javaClass.simpleName
    fun hideKeyboard(activity: Activity)
    {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = View(activity)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun formatData(dataFormat: String, timeStamp: Long): String = if(timeStamp.toInt() == 0) "N/A" else SimpleDateFormat(dataFormat, Locale.getDefault()).format(Date(timeStamp * 1000))

    fun checkIsTheSameDeviceMac(macA: String, macB: String): Boolean {
        /*
        對只差最後一碼的4台extender來說, mac address分配如下:
        11:22:33:44:55:60 ~ 11:22:33:44:55:63
        11:22:33:44:55:64 ~ 11:22:33:44:55:67
        11:22:33:44:55:68 ~ 11:22:33:44:55:6b
        11:22:33:44:55:6c ~ 11:22:33:44:55:6f

        故找尋時, 除了先比前面一不一樣外(app已支援), 若都相同的話, 再比最後一碼.
        0~3是同一台, 4~7是同一台, 8~b是同一台, c~f是同一台.
        可以完全解掉問題.
         */

        var subMacA = ""
        var lastSubMacA = ""
        var subMacB = ""
        var lastSubMacB = ""

        if(macA.contains(":")
                && ( (macA.lastIndexOf(":") + 2) == (macA.length - 1)) )
        {
            subMacA = macA.substring(0, macA.lastIndexOf(":") + 2)
            lastSubMacA = macA.substring(macA.lastIndexOf(":") + 2, macA.length)
            LogUtil.d(TAG,"subMacA:$subMacA, lastSubMacA:$lastSubMacA")
        }

        if(macB.contains(":")
                && ( (macB.lastIndexOf(":") + 2) == (macB.length - 1)) )
        {
            subMacB = macB.substring(0, macB.lastIndexOf(":") + 2)
            lastSubMacB = macB.substring(macB.lastIndexOf(":") + 2, macB.length)
            LogUtil.d(TAG,"subMacB:$subMacB, lastSubMacB:$lastSubMacB")
        }

        if(subMacA != "" && subMacB != "")
        {
            if(subMacA.equals(subMacB, ignoreCase = true))
            {
                val lastSubMacAValue = Integer.decode("0x$lastSubMacA")
                val lastSubMacBValue = Integer.decode("0x$lastSubMacB")
                LogUtil.d(TAG,"lastSubMacAValue:$lastSubMacAValue")
                LogUtil.d(TAG,"lastSubMacBValue:$lastSubMacBValue")

                //0~3是同一台, 4~7是同一台, 8~b是同一台, c~f是同一台.
                if((lastSubMacAValue in 0..3) && (lastSubMacBValue in 0..3))
                    return true

                if((lastSubMacAValue in 4..7) && (lastSubMacBValue in 4..7))
                    return true

                if((lastSubMacAValue in 8..11) && (lastSubMacBValue in 8..11))
                    return true

                if((lastSubMacAValue in 12..15) && (lastSubMacBValue in 12..15))
                    return true
            }
        }
        return false
    }
}