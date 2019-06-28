package zyxel.com.multyproneo.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.fragment_wifi_settings.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.dialog.QRCodeDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/6/12.
 */
class WiFiSettingsFragment : Fragment()
{
    private lateinit var WiFiQRCodeBitmap: Bitmap
    private lateinit var guestWiFiQRCodeBitmap: Bitmap
    private var WiFiName = ""
    private var WiFiPwd = ""
    private var WiFiSecurity = ""
    private var guestWiFiName = ""
    private var guestWiFiPwd = ""
    private var guestWiFiSecurity = ""
    private var showWiFiPed = false
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
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
        getGatewayInfoTask()
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
            wifi_settings_wifi_password_show_image ->
            {
                wifi_settings_wifi_password_text.transformationMethod = if (showWiFiPed) PasswordTransformationMethod() else null
                wifi_settings_wifi_password_show_image.setImageDrawable(resources.getDrawable(if (showWiFiPed) R.drawable.icon_hide else R.drawable.icon_show))
                showWiFiPed = !showWiFiPed
            }

            wifi_settings_wifi_share_image -> QRCodeDialog(activity!!, getString(R.string.qrcode_dialog_wifi_msg), WiFiQRCodeBitmap).show()

            wifi_settings_wifi_edit_image ->
            {
                val bundle = Bundle().apply{
                    putBoolean("GuestWiFiMode", false)
                    putString("Name", WiFiName)
                    putString("Password", WiFiPwd)
                    putBoolean("WifiStatus", false)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(WiFiSettingsEditFragment().apply{ arguments = bundle }))
            }

            wifi_settings_guest_wifi_password_show_image ->
            {
                wifi_settings_guest_wifi_password_text.transformationMethod = if (showGuestWiFiPed) PasswordTransformationMethod() else null
                wifi_settings_guest_wifi_password_show_image.setImageDrawable(resources.getDrawable(if (showGuestWiFiPed) R.drawable.icon_hide else R.drawable.icon_show))
                showGuestWiFiPed = !showGuestWiFiPed
            }

            wifi_settings_guest_wifi_share_image -> QRCodeDialog(activity!!, getString(R.string.qrcode_dialog_guest_wifi_msg), guestWiFiQRCodeBitmap).show()

            wifi_settings_guest_wifi_edit_image ->
            {
                val bundle = Bundle().apply{
                    putBoolean("GuestWiFiMode", true)
                    putString("Name", guestWiFiName)
                    putString("Password", guestWiFiPwd)
                    putBoolean("WifiStatus", guestWiFiStatus)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(WiFiSettingsEditFragment().apply{ arguments = bundle }))
            }

            wifi_settings_guest_wifi_switch_image ->
            {
                guestWiFiStatus = !guestWiFiStatus
                val bundle = Bundle().apply{
                    putString("Title", "")
                    putString("Description", getString(R.string.loading_transition_please_wait))
                    putString("Sec_Description", getString(R.string.loading_transition_update_wifi_settings))
                    putInt("LoadingSecond", AppConfig.guestWiFiSettingTime)
                    putSerializable("Anim", AppConfig.Companion.LoadingAnimation.ANIM_REBOOT)
                    putSerializable("DesPage", AppConfig.Companion.LoadingGoToPage.FRAG_SEARCH)
                    putBoolean("ShowCountDownTimer", false)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionFragment().apply{ arguments = bundle }))
            }
        }
    }

    private fun setClickListener()
    {
        wifi_settings_wifi_password_show_image.setOnClickListener(clickListener)
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

    private fun getGatewayInfoTask()
    {
        doAsync{
            WiFiName = "Zyxel06075"
            WiFiPwd = "383TAR8ND7"
            WiFiSecurity = "WPA2PSK"
            guestWiFiName = "Zyxel06075_guest"
            guestWiFiPwd = "383TAR8N"
            guestWiFiSecurity = "WPA2PSK"
            guestWiFiStatus = false

            uiThread{
                if(isVisible)
                {
                    wifi_settings_wifi_name_text.text = WiFiName
                    wifi_settings_wifi_password_text.text = WiFiPwd
                    wifi_settings_guest_wifi_name_text.text = guestWiFiName
                    wifi_settings_guest_wifi_password_text.text = guestWiFiPwd
                    wifi_settings_guest_wifi_switch_image.setImageResource(if(guestWiFiStatus) R.drawable.switch_on else R.drawable.switch_off)
                    generateQRCode()
                }
            }
        }
    }
}