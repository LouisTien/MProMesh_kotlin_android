package zyxel.com.multyproneo.tool

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ListAdapter
import android.widget.ListView
import zyxel.com.multyproneo.model.ParentalControlInfoProfile
import zyxel.com.multyproneo.model.ParentalControlInfoSchedule
import zyxel.com.multyproneo.model.RemoteManagementObject
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by LouisTien on 2019/6/11.
 */
object CommonTool
{
    private val TAG = "CommonTool"
    fun hideKeyboard(activity: Activity)
    {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = View(activity)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun formatData(dataFormat: String, timeStamp: Long): String = if(timeStamp.toInt() == 0) "N/A" else SimpleDateFormat(dataFormat, Locale.getDefault()).format(Date(timeStamp * 1000))

    fun checkIsTheSameDeviceMac(macA: String, macB: String): Boolean
    {
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

    @Throws(IOException::class)
    fun copyFile(sourceFile: File?, destFile: File)
    {
        if(!destFile.parentFile?.exists()!!) destFile.parentFile?.mkdirs()

        if(destFile.exists())
        {
            destFile.delete()
            destFile.createNewFile()
        }
        else
        {
            destFile.createNewFile()
        }

        var source: FileChannel? = null
        var destination: FileChannel? = null
        try
        {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination.transferFrom(source, 0, source.size())
        }
        finally
        {
            source?.close()
            destination?.close()
        }
    }

    //為listview動態設定高度（有多少條目就顯示多少條目）
    fun setListViewHeight(listView: ListView) : Int
    {
        //獲取listView的adapter
        val listAdapter: ListAdapter = listView.adapter ?: return 0
        var totalHeight = 0
        //listAdapter.getCount()返回資料項的數目
        var i = 0
        val len: Int = listAdapter.count
        while(i < len)
        {
            val listItem: View = listAdapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
            i++
        }
        // listView.getDividerHeight()獲取子項間分隔符佔用的高度
        // params.height最後得到整個ListView完整顯示需要的高度
        val params: ViewGroup.LayoutParams = listView.layoutParams
        params.height = totalHeight + listView.dividerHeight * (listAdapter.count - 1)
        listView.layoutParams = params

        return params.height
    }

    fun getSelectDays(scheduleInfo: ParentalControlInfoSchedule) : String
    {
        var resStr = ""

        with(scheduleInfo.GetDaysInfo())
        {
            when
            {
                sun && sat && !mon && !tue && !wed && !thu && !fri -> resStr = "Weekend"

                !sun && !sat && mon && tue && wed && thu && fri -> resStr = "Weekdays"

                sun && sat && mon && tue && wed && thu && fri -> resStr = "Everyday"

                else ->
                {
                    if(mon) resStr += "Mon,"
                    if(tue) resStr += "Tue,"
                    if(wed) resStr += "Wed,"
                    if(thu) resStr += "Thu,"
                    if(fri) resStr += "Fri,"
                    if(sat) resStr += "Sat,"
                    if(sun) resStr += "Sun,"

                    val dotIdx = resStr.lastIndexOf(",")
                    if(dotIdx > 0) resStr = resStr.substring(0, dotIdx)
                }
            }
        }

        return resStr
    }

    fun checkScheduleBlock(profileInfo: ParentalControlInfoProfile) : Boolean
    {
        if(GlobalData.parentalControlMasterSwitch && profileInfo.Enable)
        {
            var weekMatch = false
            var timeMatch = false
            profileInfo.Schedule.forEach {
                when(GlobalData.gatewaySystemDate.week)
                {
                    "Sun" -> weekMatch = it.GetDaysInfo().sun
                    "Mon" -> weekMatch = it.GetDaysInfo().mon
                    "Tue" -> weekMatch = it.GetDaysInfo().tue
                    "Wed" -> weekMatch = it.GetDaysInfo().wed
                    "Thu" -> weekMatch = it.GetDaysInfo().thu
                    "Fri" -> weekMatch = it.GetDaysInfo().fri
                    "Sat" -> weekMatch = it.GetDaysInfo().sat
                }
                val startTime = it.TimeStartHour * 60 + it.TimeStartMin
                val stopTime = it.TimeStopHour * 60 + it.TimeStopMin
                val nowTime = GlobalData.gatewaySystemDate.hour * 60 + GlobalData.gatewaySystemDate.min
                if(nowTime in startTime..stopTime) timeMatch = true
                if(weekMatch && timeMatch) return true
            }

            return false
        }
        else
            return false
    }

    fun getDate(): String
    {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dt = Date()
        return sdf.format(dt)
    }

    fun getWebGUIip(): Triple<String, String, String>
    {
        /* 決定gateway detail頁面，連到GUI的IP為何?
           Enable為true的object才要判斷 (HTTP or HTTPS一定會有一個為true，不然手機也連不進去)，
           Mode為 LAN_ONLY (HTTP or HTTPS一定會有一個為LAN_ONLY，不然手機也連不進去)，
           Name為HTTP or HTTPS，hyperlink為HTTPS或HTTP要參照Name欄位，
           如HTTPS與HTTP都為LAN_ONLY，則以HTTP為主
           符合上述條件的object，將其GUI ip + port 就是連線到GUI的IP，
           app上面只顯示IP就好，port不用顯示 */

        var protocol = "HTTP"
        var ip = ""
        var port = ""
        when(FeatureConfig.FeatureInfo.APPUICustomList.GW_LAN_IP_PORT)
        {
            true ->
            {
                val remoteManagements = arrayListOf<RemoteManagementObject>()
                for(item in FeatureConfig.remoteManagements)
                {
                    if(item.Enable && item.Mode.equals("LAN_ONLY", ignoreCase = false))
                        remoteManagements.add(item)
                }

                when
                {
                    remoteManagements.size == 1 ->
                    {
                        protocol = remoteManagements[0].Name
                        ip = remoteManagements[0].IPAddress
                        port = remoteManagements[0].Port.toString()
                    }

                    remoteManagements.size > 1 ->
                    {
                        for(obj in remoteManagements)
                        {
                            if(obj.Name.equals("HTTP", ignoreCase = false))
                            {
                                protocol = obj.Name
                                ip = obj.IPAddress
                                port = obj.Port.toString()
                                break
                            }
                        }
                    }
                }
            }

            false -> ip = GlobalData.getCurrentGatewayInfo().IP
        }
        return Triple(protocol, ip, port)
    }
}