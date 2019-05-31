package zyxel.com.multyproneo.event

import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/5/29.
 */
class DialogEvent
{

    class OnPositiveBtn(var action: AppConfig.Companion.DialogAction = AppConfig.Companion.DialogAction.ACT_NONE, var block: Boolean = false)
    class OnSlideListSelect(var data: String)
}