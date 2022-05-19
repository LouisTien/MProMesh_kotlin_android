package zyxel.com.multyproneo.util

import zyxel.com.multyproneo.model.AppUICustomInfo
import zyxel.com.multyproneo.model.RemoteManagementObject

/**
 * Created by LouisTien on 2019/6/17.
 */
object FeatureConfig
{
    var FeatureInfo = AppUICustomInfo()
    var FSecureStatus = false
    var hostNameReplaceStatus = false
    var internetBlockingStatus = true
    var remoteManagements = listOf(RemoteManagementObject())
}