package zyxel.com.multyproneo.util

import zyxel.com.multyproneo.model.AppFeatureInfoProfile

/**
 * Created by LouisTien on 2019/6/17.
 */
class FeatureConfig
{
    companion object
    {
        var supportAccountLogin = false
        var hostNameReplease = false
        var PCMode = 0 //1:black list, 0:white list
        var featureInfoProfile = AppFeatureInfoProfile()
    }
}