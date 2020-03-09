package zyxel.com.multyproneo.event

/**
 * Created by LouisTien on 2019/5/24.
 */
class MainEvent
{
    data class SwitchToFrag(var frag: android.support.v4.app.Fragment)
    class ShowLoadingOnlyGrayBG()
    class ShowLoading()
    class HideLoading()
    class ShowBottomToolbar()
    class HideBottomToolbar()
    class SetHomeIconFocus()
    class StartGetDeviceInfoTask()
    class StartGetDeviceInfoOnceTask()
    class StopGetDeviceInfoTask()
    class StartGetWPSStatusTask()
    class StopGetWPSStatusTask()
    class StartGetSpeedTestStatusTask()
    class StopGetSpeedTestStatusTask()
    class EnterHomePage()
    class EnterDevicesPage()
    class EnterWiFiSettingsPage()
    class EnterDiagnosticPage()
    class EnterAccountPage()
    class EnterSearchGatewayPage()
    class ShowErrorMsgDialog(var msg: String, var requestCtxName: String)
    class ShowErrorMsgDialogCloud(var msg: String, var requestCtxName: String)
    class ShowToast(var msg: String, var requestCtxName: String)
}