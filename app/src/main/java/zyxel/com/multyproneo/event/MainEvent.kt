package zyxel.com.multyproneo.event

/**
 * Created by LouisTien on 2019/5/24.
 */
class MainEvent
{
    data class SwitchToFrag(var frag: android.support.v4.app.Fragment)
    class ShowLoading()
    class HideLoading()
    class ShowBottomToolbar()
    class HideBottomToolbar()
    class SetHomeIconFocus()
    class StartGetDeviceInfoTask()
    class StartGetDeviceInfoOnceTask()
    class StopGetDeviceInfoTask()
    class EnterHomePage()
    class EnterDevicesPage()
    class EnterWiFiSettingsPage()
    class EnterDiagnosticPage()
    class EnterAccountPage()
    class EnterSearchGatewayPage()
    class ShowToast(var msg: String, var requestCtxName: String)
}