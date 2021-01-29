package zyxel.com.multyproneo.fragment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_loading_transition.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.socketconnect.IResponseListener
import zyxel.com.multyproneo.socketconnect.SocketController
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.DatabaseUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/5/28.
 */
class FindingDeviceFragment : Fragment(), IResponseListener
{
    private val TAG = "FindingDeviceFragment"
    private lateinit var startWiFiSettingDisposable: Disposable
    private lateinit var findingDeviceInfo: GatewayInfo
    private var gatewayList = mutableListOf<GatewayInfo>()
    private val responseListener = this
    private var userDefineName = ""
    private var retryTimes = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_loading_transition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        loading_retry_image.setOnClickListener{
            retryTimes = 6
            startFindDevice()
        }
    }

    override fun onResume()
    {
        super.onResume()

        GlobalBus.publish(MainEvent.HideBottomToolbar())

        startWiFiSettingDisposable = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        if(!isNetworkAvailable())
        {
            setNotFindDeviceUI()

            MessageDialog(
                    activity!!,
                    getString(R.string.message_dialog_not_connect),
                    getString(R.string.message_dialog_not_connect_try_again),
                    arrayOf(getString(R.string.message_dialog_ok)),
                    AppConfig.DialogAction.ACT_NONE
            ).show()
        }
        else
        {
            retryTimes = 0
            startFindDevice()
        }
    }

    override fun onPause()
    {
        super.onPause()
        if(!startWiFiSettingDisposable.isDisposed) startWiFiSettingDisposable.dispose()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    override fun responseReceived(ip: String, data: String)
    {
        LogUtil.d(TAG,"responseReceived: ip = $ip, data = $data")
        /*if(data.contains("ApiName") && data.contains("SupportedApiVersion"))
        {
            val data = JSONObject(data)
            val ApiName = data.get("ApiName").toString()
            LogUtil.d(TAG, "ApiName:$ApiName")
            val ModelName = data.get("ModelName").toString()
            LogUtil.d(TAG, "ModelName:$ModelName")
            val SoftwareVersion = data.get("SoftwareVersion").toString()
            LogUtil.d(TAG, "SoftwareVersion:$SoftwareVersion")
            val DeviceMode = data.get("DeviceMode").toString()
            LogUtil.d(TAG, "DeviceMode:$DeviceMode")
            val SupportedApiVersion = data.getJSONArray("SupportedApiVersion")
            val subdata = JSONObject(SupportedApiVersion[0].toString())
            LogUtil.d(TAG, "subdata:$subdata")
            val ApiVersion = subdata.get("ApiVersion").toString()
            LogUtil.d(TAG, "ApiVersion:$ApiVersion")
            val LoginURL = subdata.get("LoginURL").toString()
            LogUtil.d(TAG, "LoginURL:$LoginURL")
        }*/

        if(data.contains("ApiName") && data.contains("SupportedApiVersion"))
        {
            try
            {
                findingDeviceInfo = Gson().fromJson(data, GatewayInfo::class.javaObjectType)
                findingDeviceInfo.IP = ip

                /*userDefineName = DatabaseUtil.getInstance(activity!!)?.getDeviceUserDefineNameFromDB(findingDeviceInfo.MAC)!!
                LogUtil.d(TAG, "userDefineName from DB:$userDefineName")

                if(userDefineName == "")
                    findingDeviceInfo.UserDefineName = findingDeviceInfo.ModelName
                else
                    findingDeviceInfo.UserDefineName = userDefineName*/

                LogUtil.d(TAG, "findingDeviceInfo:$findingDeviceInfo")

                var exist = false
                for(item in gatewayList)
                {
                    if(item.MAC == findingDeviceInfo.MAC)
                    {
                        LogUtil.d(TAG, "already exist, MAC:${findingDeviceInfo.MAC}")
                        exist = true
                        break
                    }
                }

                if(!exist)
                    gatewayList.add(findingDeviceInfo)
            }
            catch(e: JSONException)
            {
                e.printStackTrace()
            }
        }
    }

    override fun responseReceivedDone()
    {
        LogUtil.d(TAG,"responseReceivedDone")
        if(gatewayList.size > 0)
        {
            GlobalData.gatewayList = gatewayList.toMutableList()//copy list to global data
            GlobalBus.publish(MainEvent.SwitchToFrag(GatewayListFragment()))
        }
        else
        {
            if(retryTimes < 5)
                runSearchTask()
            else
            {
                if(isVisible)
                {
                    runOnUiThread{
                        loading_animation_view.setAnimation("nofound.json")
                        loading_animation_view.playAnimation()
                        setNotFindDeviceUI()
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean
    {
        val connectivityManager = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val wifiNetInfo = connectivityManager!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return wifiNetInfo.isAvailable && wifiNetInfo.isConnected
    }

    private fun setFindDeviceUI()
    {
        loading_retry_image.visibility = View.INVISIBLE
        loading_msg_title_text.visibility = View.INVISIBLE
        loading_msg_working_text.visibility = View.VISIBLE
        loading_msg_status_text.text = getString(R.string.find_device_status_description)
    }

    private fun setNotFindDeviceUI()
    {
        loading_retry_image.visibility = View.VISIBLE
        loading_msg_title_text.visibility = View.VISIBLE
        loading_msg_working_text.visibility = View.INVISIBLE
        loading_msg_status_text.text = getString(R.string.find_device_no_result_description)
    }

    private fun startFindDevice()
    {
        setFindDeviceUI()
        runSearchTask()
    }

    private fun runSearchTask()
    {
        if(!isResumed) return

        runOnUiThread{
            loading_animation_view.setAnimation("searching.json")
            loading_animation_view.playAnimation()
        }

        retryTimes++
        GlobalData.gatewayList.clear()
        SocketController(responseListener).deviceScan()
    }
}