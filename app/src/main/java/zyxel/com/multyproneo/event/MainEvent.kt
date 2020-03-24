package zyxel.com.multyproneo.event

/**
 * Created by LouisTien on 2019/5/24.
 */
class MainEvent
{
    data class SwitchToFrag(var frag: android.support.v4.app.Fragment)
    class ShowLoadingOnlyGrayBG()
    class ShowLoading()
    class ShowHintLoading(var hint: String)
    class HideLoading()
    class ShowBottomToolbar()
    class ShowCloudBottomToolbar()
    class HideBottomToolbar()
    class SetHomeIconFocus()
    class SetCloudHomeIconFocus()
    class StartGetDeviceInfoTask()
    class StartGetDeviceInfoOnceTask()
    class StopGetDeviceInfoTask()
    class StartGetWPSStatusTask()
    class StartCloudGetWPSStatusTask()
    class StopGetWPSStatusTask()
    class StopCloudGetWPSStatusTask()
    class StartGetSpeedTestStatusTask()
    class StopGetSpeedTestStatusTask()
    class EnterHomePage()
    class EnterDevicesPage()
    class EnterWiFiSettingsPage()
    class EnterDiagnosticPage()
    class EnterAccountPage()
    class EnterCloudHomePage()
    class EnterCloudDevicesPage()
    class EnterCloudWiFiSettingsPage()
    class EnterCloudDiagnosticPage()
    class EnterCloudSettingsPage()
    class EnterSearchGatewayPage()
    class ShowErrorMsgDialog(var msg: String, var requestCtxName: String)
    class ShowErrorMsgDialogCloud(var msg: String, var requestCtxName: String)
    class ShowToast(var msg: String, var requestCtxName: String)
}