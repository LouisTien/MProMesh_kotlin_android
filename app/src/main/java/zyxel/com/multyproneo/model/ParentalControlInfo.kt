package zyxel.com.multyproneo.model

import java.io.Serializable

data class ParentalControlInfo
(
        val Object: ParentalControlInfoObject = ParentalControlInfoObject(),
        val oper_status: String = "N/A",
        val requested_path: String = "N/A"
) : Serializable

data class ParentalControlInfoObject
(
        val Enable: Boolean = false,
        val Profile: MutableList<ParentalControlInfoProfile> = mutableListOf(ParentalControlInfoProfile())
) : Serializable

data class ParentalControlInfoProfile
(
        val ContentFilterList: String = "N/A",
        var Enable: Boolean = false,
        var MACAddressList: String = "N/A",
        var Name: String = "N/A",
        var Schedule: MutableList<ParentalControlInfoSchedule> = mutableListOf(ParentalControlInfoSchedule()),
        val ScheduleRuleList: String = "N/A",
        val _EmptyInstance: Boolean = false,
        var index: Int = 0
) : Serializable
{
    fun GetMACAddressList(): Array<String> = if(MACAddressList.isEmpty()) emptyArray() else MACAddressList.split(",").toTypedArray()
}

data class ParentalControlInfoSchedule
(
        var Days: String = "N/A",
        var TimeStartHour: Int = 0,
        var TimeStartMin: Int = 0,
        var TimeStopHour: Int = 0,
        var TimeStopMin: Int = 0
) : Serializable
{
    fun GetDaysInfo() : ParentalControlInfoScheduleDays
    {
        val Sat = 64
        val Fri = 32
        val Thu = 16
        val Wed = 8
        val Tue = 4
        val Mon = 2
        val Sun = 1

        var DaysInt = Days.toInt()
        val daysInfo = ParentalControlInfoScheduleDays()

        if(DaysInt >= Sat)
        {
            DaysInt -= Sat
            daysInfo.sat = true
        }

        if(DaysInt >= Fri)
        {
            DaysInt -= Fri
            daysInfo.fri = true
        }

        if(DaysInt >= Thu)
        {
            DaysInt -= Thu
            daysInfo.thu = true
        }

        if(DaysInt >= Wed)
        {
            DaysInt -= Wed
            daysInfo.wed = true
        }

        if(DaysInt >= Tue)
        {
            DaysInt -= Tue
            daysInfo.tue = true
        }

        if(DaysInt >= Mon)
        {
            DaysInt -= Mon
            daysInfo.mon = true
        }

        if(DaysInt >= Sun)
        {
            DaysInt -= Sun
            daysInfo.sun = true
        }

        return daysInfo
    }
}

data class ParentalControlInfoScheduleDays
(
        var sun: Boolean = false,
        var mon: Boolean = false,
        var tue: Boolean = false,
        var wed: Boolean = false,
        var thu: Boolean = false,
        var fri: Boolean = false,
        var sat: Boolean = false
) : Serializable