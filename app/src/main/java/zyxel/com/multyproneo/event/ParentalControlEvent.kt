package zyxel.com.multyproneo.event

import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.model.ParentalControlInfoSchedule
import zyxel.com.multyproneo.util.AppConfig

class ParentalControlEvent
{
    class SelectParentalControlDeviceComplete()
    class ScheduleSet(var action: AppConfig.ScheduleAction, var index: Int, var scheduleInfo: ParentalControlInfoSchedule)
    class ScheduleMenu(var index: Int, var scheduleInfo: ParentalControlInfoSchedule)
    class ScheduleAct(var action: AppConfig.ScheduleAction, var index: Int, var scheduleInfo: ParentalControlInfoSchedule)
    class DeviceSelect()
    class DeviceDelete(var device: DevicesInfoObject)
    class DeviceMenu(var device: DevicesInfoObject)
    class HandleProfilePhoto(var action: AppConfig.ProfilePhotoAction)
}