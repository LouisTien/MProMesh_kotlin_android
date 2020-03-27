package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONException
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.AMDMApi
import zyxel.com.multyproneo.api.cloud.TUTKCommander
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.FindingDeviceFragment
import zyxel.com.multyproneo.model.cloud.TUTKTokenInfo
import zyxel.com.multyproneo.util.*
import java.util.HashMap

class CloudWelcomeFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var tokenInfo: TUTKTokenInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({ decideFlow() }, AppConfig.WELCOME_DISPLAY_TIME_IN_MILLISECONDS)
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

    private fun decideFlow()
    {
        var oldDBExist = false
        var newDBExist = false

        doAsync{
            val gatewayInfoArrayListDB = DatabaseUtil.getInstance(activity!!)?.getGatewayFromDB()
            if(gatewayInfoArrayListDB!!.isNotEmpty())
            {
                LogUtil.d(TAG,"old DB has data!!")
                oldDBExist = true
            }
            else
                LogUtil.d(TAG,"old DB is empty!!")

            val db = DatabaseCloudUtil.getInstance(activity!!)!!
            val siteInfoList = db.getSiteInfoDao().getAll()
            if(siteInfoList.isNotEmpty())
            {
                LogUtil.d(TAG,"new DB has data!!")
                newDBExist = true
            }
            else
                LogUtil.d(TAG,"new DB is empty!!")

            uiThread{
                if(oldDBExist)
                    GlobalBus.publish(MainEvent.SwitchToFrag(FindingDeviceFragment()))
                else if(newDBExist)
                    refreshToken()
                else
                    GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))
            }
        }
    }

    private fun refreshToken()
    {
        var refreshToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_REFRESH_TOKEN_KEY, "")
        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        if(refreshToken == "" || accessToken == "")
        {
            val bundle = Bundle().apply{
                putBoolean("isInSetupFlow", false)
            }
            GlobalBus.publish(MainEvent.SwitchToFrag(ConnectToCloudFragment().apply{ arguments = bundle }))
        }
        else
        {
            val header = HashMap<String, Any>()
            header["authorization"] = "Basic ${AppConfig.TUTK_DM_AUTHORIZATION}"
            header["content-type"] = "application/x-www-form-urlencoded"

            val body = HashMap<String, Any>()
            body["grant_type"] = "refresh_token"
            body["refresh_token"] = refreshToken

            AMDMApi.RefreshToken()
                    .setRequestPageName(TAG)
                    .setHeaders(header)
                    .setFormBody(body)
                    .setResponseListener(object: TUTKCommander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                tokenInfo = Gson().fromJson(responseStr, TUTKTokenInfo::class.javaObjectType)
                                LogUtil.d(TAG,"refreshTokenInfo:$tokenInfo")
                                refreshToken = tokenInfo.refresh_token
                                accessToken = tokenInfo.access_token
                                GlobalData.tokenType = tokenInfo.token_type
                                LogUtil.d(TAG, "refreshToken:$refreshToken")
                                LogUtil.d(TAG, "accessToken:$accessToken")
                                GlobalBus.publish(MainEvent.GetCloudInfo())
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
}