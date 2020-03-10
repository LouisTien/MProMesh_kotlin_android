package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.AMDMApi
import zyxel.com.multyproneo.api.cloud.TUTKCommander
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.cloud.TUTKAddDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import zyxel.com.multyproneo.util.SharedPreferencesUtil
import java.util.HashMap

class SetupFinalizingYourHomeNetwork : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var addDeviceInfo: TUTKAddDeviceInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_finalizing_your_home_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        addDevice()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private fun addDevice()
    {
        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        val params = JSONObject()
        params.put("udid", GlobalData.currentUID)
        params.put("fwVer", GlobalData.getCurrentGatewayInfo().SoftwareVersion)
        params.put("displayName", GlobalData.getCurrentGatewayInfo().ModelName)
        params.put("credential", "")
        LogUtil.d(TAG,"addDevice param:$params")

        AMDMApi.AddDevice()
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setParams(params)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            addDeviceInfo = Gson().fromJson(responseStr, TUTKAddDeviceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"addDeviceInfo:$addDeviceInfo")
                            getAllDevice()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getAllDevice()
    {
        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        AMDMApi.GetAllDevice()
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            GlobalData.cloudGatewayListInfo = Gson().fromJson(responseStr, TUTKAllDeviceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"allDeviceInfo:${GlobalData.cloudGatewayListInfo}")
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }
}