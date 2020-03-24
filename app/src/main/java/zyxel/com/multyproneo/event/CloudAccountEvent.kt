package zyxel.com.multyproneo.event

import zyxel.com.multyproneo.model.cloud.AllDeviceInfo

class CloudAccountEvent
{
    class OnSiteSelect(var uid: String)
    class OnSiteDelete(var info: AllDeviceInfo, var self: Boolean)
    class ConfirmSiteDelete(var info: AllDeviceInfo, var backup: Boolean)
}