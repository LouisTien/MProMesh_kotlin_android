package zyxel.com.multyproneo.event

import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/5/29.
 */
class DialogEvent
{
    class OnPositiveBtn(var action: AppConfig.DialogAction = AppConfig.DialogAction.ACT_NONE, var block: Boolean = false)
    class OnCancelBtn(var action: AppConfig.DialogAction = AppConfig.DialogAction.ACT_NONE)
    class OnSlideListSelect(var data: DatabaseSiteInfoEntity)
    class OnOtherSiteSelect(var uid: String)
}