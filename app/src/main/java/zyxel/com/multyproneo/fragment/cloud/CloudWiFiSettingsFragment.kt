package zyxel.com.multyproneo.fragment.cloud

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.fragment_wifi_settings.*
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.P2PAccountApi
import zyxel.com.multyproneo.api.cloud.P2PWiFiSettingApi
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.dialog.QRCodeDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.MeshInfo
import zyxel.com.multyproneo.model.WiFiSettingInfo
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudWiFiSettingsFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var meshInfo: MeshInfo
    private lateinit var WiFiSettingInfoSet: WiFiSettingInfo
    private lateinit var WiFiQRCodeBitmap: Bitmap
    private lateinit var WiFiQRCodeBitmap5g: Bitmap
    private lateinit var guestWiFiQRCodeBitmap: Bitmap
    private var WiFiName = ""
    private var WiFiPwd = ""
    private var WiFiSecurity = ""
    private var WiFiName5g = ""
    private var WiFiPwd5g = ""
    private var WiFiSecurity5g = ""
    private var guestWiFiName = ""
    private var guestWiFiPwd = ""
    private var guestWiFiSecurity = ""
    private var showWiFiPed = false
    private var showWiFiPed5g = false
    private var showGuestWiFiPed = false
    private var guestWiFiStatus = false

    private val SECURITY_NONE = "none"
    private val SECURITY_WPA = "WPA"
    private val SECURITY_WPA2 = "WPA2"
    private val SECURITY_WPA2PSK = "WPA2PSK"
    private val SECURITY_WPAPSK = "WPAPSK"
    private val SECURITY_WEP64 = "WEP64Bits"
    private val SECURITY_WEP128 = "WEP128Bits"
    private val QRCODE_PIXEL = 150

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_wifi_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowCloudBottomToolbar())
        wifi_settings_wifi_area_linear.visibility = View.INVISIBLE
        getMeshInfoTask()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            wifi_settings_wifi_24g_password_show_image ->
            {
                wifi_settings_wifi_24g_password_text.transformationMethod = if(showWiFiPed) PasswordTransformationMethod() else null
                wifi_settings_wifi_24g_password_show_image.setImageDrawable(resources.getDrawable(if(showWiFiPed) R.drawable.icon_hide else R.drawable.icon_show))
                showWiFiPed = !showWiFiPed
            }

            wifi_settings_wifi_5g_password_show_image ->
            {
                wifi_settings_wifi_5g_password_text.transformationMethod = if(showWiFiPed5g) PasswordTransformationMethod() else null
                wifi_settings_wifi_5g_password_show_image.setImageDrawable(resources.getDrawable(if(showWiFiPed5g) R.drawable.icon_hide else R.drawable.icon_show))
                showWiFiPed5g = !showWiFiPed5g
            }

            wifi_settings_wifi_share_image ->
            {
                if(meshInfo.Object.Enable)
                    QRCodeDialog(activity!!, getString(R.string.qrcode_dialog_wifi_msg), WiFiQRCodeBitmap).show()
                else
                    QRCodeDialog(activity!!, getString(R.string.qrcode_dialog_wifi_msg), WiFiQRCodeBitmap, WiFiQRCodeBitmap5g).show()
            }

            wifi_settings_wifi_edit_image ->
            {
                val bundle = Bundle().apply{
                    putBoolean("GuestWiFiMode", false)
                    putBoolean("ShowOneSSID", meshInfo.Object.Enable)
                    putBoolean("Available5g", !WiFiSettingInfoSet.Object.X_ZYXEL_OneSSID.Enable)
                    putString("Name", WiFiName)
                    putString("Password", WiFiPwd)
                    putString("Security", WiFiSecurity)
                    putString("Name5g", WiFiName5g)
                    putString("Password5g", WiFiPwd5g)
                    putString("Security5g", WiFiSecurity5g)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudWiFiSettingsEditFragment().apply{ arguments = bundle }))
            }

            wifi_settings_guest_wifi_password_show_image ->
            {
                wifi_settings_guest_wifi_password_text.transformationMethod = if(showGuestWiFiPed) PasswordTransformationMethod() else null
                wifi_settings_guest_wifi_password_show_image.setImageDrawable(resources.getDrawable(if(showGuestWiFiPed) R.drawable.icon_hide else R.drawable.icon_show))
                showGuestWiFiPed = !showGuestWiFiPed
            }

            wifi_settings_guest_wifi_share_image -> QRCodeDialog(activity!!, getString(R.string.qrcode_dialog_guest_wifi_msg), guestWiFiQRCodeBitmap).show()

            wifi_settings_guest_wifi_edit_image ->
            {
                val bundle = Bundle().apply{
                    putBoolean("GuestWiFiMode", true)
                    putString("Name", guestWiFiName)
                    putString("Password", guestWiFiPwd)
                    putString("Security", guestWiFiSecurity)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudWiFiSettingsEditFragment().apply{ arguments = bundle }))
            }

            wifi_settings_guest_wifi_switch_image ->
            {
                guestWiFiStatus = !guestWiFiStatus

                setGuestWiFi24GEnableTask()

                val bundle = Bundle().apply{
                    putString("Title", "")
                    putString("Description", getString(R.string.loading_transition_please_wait))
                    putString("Sec_Description", getString(R.string.loading_transition_update_wifi_settings))
                    putInt("LoadingSecond", AppConfig.WiFiSettingTime)
                    putSerializable("Anim", AppConfig.LoadingAnimation.ANIM_REBOOT)
                    putSerializable("DesPage", AppConfig.LoadingGoToPage.FRAG_SEARCH)
                    putBoolean("ShowCountDownTimer", false)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudLoadingTransitionFragment().apply{ arguments = bundle }))

                /*LoadingTransitionDialog(
                        context!!,
                        "",
                        getString(R.string.loading_transition_please_wait),
                        getString(R.string.loading_transition_update_wifi_settings),
                        AppConfig.WiFiSettingTime,
                        AppConfig.LoadingAnimation.ANIM_REBOOT,
                        AppConfig.LoadingGoToPage.FRAG_SEARCH,
                        false).show()*/
            }
        }
    }

    private fun setClickListener()
    {
        wifi_settings_wifi_24g_password_show_image.setOnClickListener(clickListener)
        wifi_settings_wifi_5g_password_show_image.setOnClickListener(clickListener)
        wifi_settings_wifi_share_image.setOnClickListener(clickListener)
        wifi_settings_wifi_edit_image.setOnClickListener(clickListener)
        wifi_settings_guest_wifi_password_show_image.setOnClickListener(clickListener)
        wifi_settings_guest_wifi_share_image.setOnClickListener(clickListener)
        wifi_settings_guest_wifi_edit_image.setOnClickListener(clickListener)
        wifi_settings_guest_wifi_switch_image.setOnClickListener(clickListener)
    }

    private fun generateQRCode()
    {
        var WiFiInfo = ""
        if(WiFiSecurity == SECURITY_NONE || WiFiSecurity == "")
            WiFiInfo = "WIFI:T:nopass;S:$WiFiName;;"
        else if(WiFiSecurity == SECURITY_WEP128 || WiFiSecurity == SECURITY_WEP64)
            WiFiInfo = "WIFI:T:WEP;S:$WiFiName;P:$WiFiPwd;;"
        else
            WiFiInfo = "WIFI:T:WPA2;S:$WiFiName;P:$WiFiPwd;;"

        val bitMatrix = QRCodeWriter().encode(WiFiInfo, BarcodeFormat.QR_CODE, QRCODE_PIXEL, QRCODE_PIXEL)
        WiFiQRCodeBitmap = Bitmap.createBitmap(QRCODE_PIXEL, QRCODE_PIXEL, Bitmap.Config.ARGB_8888)
        for(i in 0 until QRCODE_PIXEL)
            for(j in 0 until QRCODE_PIXEL)
                WiFiQRCodeBitmap.setPixel(i, j, if(bitMatrix.get(i, j)) Color.BLACK else Color.WHITE)

        var WiFiInfo5g = ""
        if(WiFiSecurity5g == SECURITY_NONE || WiFiSecurity == "")
            WiFiInfo5g = "WIFI:T:nopass;S:$WiFiName5g;;"
        else if(WiFiSecurity5g == SECURITY_WEP128 || WiFiSecurity5g == SECURITY_WEP64)
            WiFiInfo5g = "WIFI:T:WEP;S:$WiFiName5g;P:$WiFiPwd5g;;"
        else
            WiFiInfo5g = "WIFI:T:WPA2;S:$WiFiName5g;P:$WiFiPwd5g;;"

        val bitMatrix5g = QRCodeWriter().encode(WiFiInfo5g, BarcodeFormat.QR_CODE, QRCODE_PIXEL, QRCODE_PIXEL)
        WiFiQRCodeBitmap5g = Bitmap.createBitmap(QRCODE_PIXEL, QRCODE_PIXEL, Bitmap.Config.ARGB_8888)
        for(i in 0 until QRCODE_PIXEL)
            for(j in 0 until QRCODE_PIXEL)
                WiFiQRCodeBitmap5g.setPixel(i, j, if(bitMatrix5g.get(i, j)) Color.BLACK else Color.WHITE)

        var guestWiFiInfo = ""
        if(guestWiFiSecurity == SECURITY_NONE || guestWiFiSecurity == "")
            guestWiFiInfo = "WIFI:T:nopass;S:$guestWiFiName;;"
        else if(guestWiFiSecurity == SECURITY_WEP128 || guestWiFiSecurity == SECURITY_WEP64)
            guestWiFiInfo = "WIFI:T:WEP;S:$guestWiFiName;P:$guestWiFiPwd;;"
        else
            guestWiFiInfo = "WIFI:T:WPA2;S:$guestWiFiName;P:$guestWiFiPwd;;"

        val bitMatrixGuest = QRCodeWriter().encode(guestWiFiInfo, BarcodeFormat.QR_CODE, QRCODE_PIXEL, QRCODE_PIXEL)
        guestWiFiQRCodeBitmap = Bitmap.createBitmap(QRCODE_PIXEL, QRCODE_PIXEL, Bitmap.Config.ARGB_8888)
        for(i in 0 until QRCODE_PIXEL)
            for(j in 0 until QRCODE_PIXEL)
                guestWiFiQRCodeBitmap.setPixel(i, j, if(bitMatrixGuest.get(i, j)) Color.BLACK else Color.WHITE)

    }

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        runOnUiThread{
            wifi_settings_wifi_area_linear.visibility = View.VISIBLE

            if(meshInfo.Object.Enable)
            {
                val lp_share = FrameLayout.LayoutParams(wifi_settings_wifi_share_image.layoutParams).apply{
                    gravity = Gravity.BOTTOM or Gravity.LEFT
                    setMargins(dip(20), 0, 0, dip(20))
                }
                wifi_settings_wifi_share_image.layoutParams = lp_share

                val lp_edit = FrameLayout.LayoutParams(wifi_settings_wifi_edit_image.layoutParams).apply{
                    gravity = Gravity.BOTTOM or Gravity.RIGHT
                    setMargins(0, 0, dip(20), dip(20))
                }
                wifi_settings_wifi_edit_image.layoutParams = lp_edit

                wifi_settings_wifi_area_frame.setBackgroundResource(R.drawable.card_wifibg)
                wifi_settings_wifi_5g_area_relative.visibility = View.GONE
                wifi_settings_wifi_24g_name_title_text.text = getString(R.string.wifi_settings_wifi_name)
                wifi_settings_wifi_24g_password_title_text.text = getString(R.string.wifi_settings_wifi_password)
                wifi_settings_wifi_24g_name_text.text = WiFiName
                wifi_settings_wifi_24g_password_text.text = WiFiPwd
            }
            else
            {
                val lp_share = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply{
                    gravity = Gravity.BOTTOM or Gravity.LEFT
                    setMargins(dip(17), 0, 0, dip(20))
                }
                wifi_settings_wifi_share_image.layoutParams = lp_share

                val lp_edit = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply{
                    gravity = Gravity.BOTTOM or Gravity.RIGHT
                    setMargins(0, 0, dip(17), dip(20))
                }
                wifi_settings_wifi_edit_image.layoutParams = lp_edit

                wifi_settings_wifi_area_frame.setBackgroundResource(R.drawable.card_wifibg_2)
                wifi_settings_wifi_24g_name_text.text = WiFiName
                wifi_settings_wifi_24g_password_text.text = WiFiPwd
                wifi_settings_wifi_5g_name_text.text = WiFiName5g
                wifi_settings_wifi_5g_password_text.text = WiFiPwd5g

                if(WiFiSettingInfoSet.Object.X_ZYXEL_OneSSID.Enable)
                {
                    wifi_settings_wifi_5g_area_relative.animate().alpha(0.4f)
                    wifi_settings_wifi_5g_password_show_image.isEnabled = false
                }
            }
            wifi_settings_guest_wifi_name_text.text = guestWiFiName
            wifi_settings_guest_wifi_password_text.text = guestWiFiPwd
            wifi_settings_guest_wifi_switch_image.setImageResource(if(guestWiFiStatus) R.drawable.switch_on else R.drawable.switch_off)
            generateQRCode()
        }
    }

    private fun getMeshInfoTask()
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        LogUtil.d(TAG,"getMeshInfoTask()")
        P2PWiFiSettingApi.GetMeshInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            meshInfo = Gson().fromJson(responseStr, MeshInfo::class.javaObjectType)
                            LogUtil.d(TAG,"meshInfo:$meshInfo")
                            getWiFiSettingInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()

    }

    private fun getWiFiSettingInfoTask()
    {
        LogUtil.d(TAG,"getWiFiSettingInfoTask()")
        P2PWiFiSettingApi.GetWiFiSettingInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            WiFiSettingInfoSet = Gson().fromJson(responseStr, WiFiSettingInfo::class.javaObjectType)
                            LogUtil.d(TAG,"wiFiSettingInfo:$WiFiSettingInfoSet")

                            WiFiName = WiFiSettingInfoSet.Object.SSID[0].SSID
                            WiFiPwd = WiFiSettingInfoSet.Object.AccessPoint[0].Security.KeyPassphrase
                            WiFiSecurity = WiFiSettingInfoSet.Object.AccessPoint[0].Security.ModeEnabled
                            WiFiName5g = WiFiSettingInfoSet.Object.SSID[4].SSID
                            WiFiPwd5g = WiFiSettingInfoSet.Object.AccessPoint[4].Security.KeyPassphrase
                            WiFiSecurity5g = WiFiSettingInfoSet.Object.AccessPoint[4].Security.ModeEnabled
                            guestWiFiName = WiFiSettingInfoSet.Object.SSID[1].SSID
                            guestWiFiPwd = WiFiSettingInfoSet.Object.AccessPoint[1].Security.KeyPassphrase
                            guestWiFiSecurity = WiFiSettingInfoSet.Object.AccessPoint[1].Security.ModeEnabled
                            guestWiFiStatus = WiFiSettingInfoSet.Object.SSID[1].Enable

                            GlobalBus.publish(MainEvent.HideLoading())
                            updateUI()
                        }
                        catch(e: JSONException)
                        {
                            GlobalBus.publish(MainEvent.HideLoading())
                            e.printStackTrace()
                        }
                    }
                }).execute()

    }

    private fun setGuestWiFi24GEnableTask()
    {
        val params = ",\"Enable\":\"$guestWiFiStatus\""

        P2PWiFiSettingApi.SetGuestWiFi24GInfo()
                .setRequestPageName(TAG)
                .setRequestPayload(params)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        setGuestWiFi5GEnableTask()
                    }
                }).execute()
    }

    private fun setGuestWiFi5GEnableTask()
    {
        val params = ",\"Enable\":\"$guestWiFiStatus\""

        P2PWiFiSettingApi.SetGuestWiFi5GInfo()
                .setRequestPageName(TAG)
                .setRequestPayload(params)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {

                    }
                }).execute()
    }
}