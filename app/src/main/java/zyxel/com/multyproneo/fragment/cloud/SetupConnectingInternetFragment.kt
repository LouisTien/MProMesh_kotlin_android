package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup_connecting_internet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.uiThread
import org.json.JSONException
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.GatewayApi
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.model.cloud.CloudAgentInfo
import zyxel.com.multyproneo.util.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class SetupConnectingInternetFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var db: DatabaseCloudUtil
    private lateinit var siteInfoList: List<DatabaseSiteInfoEntity>
    private lateinit var cloudAgentInfo: CloudAgentInfo
    private lateinit var gatewayInfo: GatewayInfo
    private var hasPreviousSettings = false
    private var hasUID = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_connecting_internet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseCloudUtil.getInstance(context!!)!!

        setup_connecting_internet_next_button.onClick{
            GlobalBus.publish(MainEvent.ShowLoading())
            startGetUIDTask()
        }

        startInternetCheckTask()

        runOnUiThread{
            setup_connecting_internet_content_animation_view.setAnimation("ConnectToTheInternet_1_oldJson.json")
            setup_connecting_internet_content_animation_view.playAnimation()
        }

        //Glide.with(activity!!).load(R.drawable.slide1).into(setup_connecting_internet_content_image)
    }

    override fun onResume()
    {
        super.onResume()
        gatewayInfo = GlobalData.getCurrentGatewayInfo()
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

    private fun startInternetCheckTask()
    {
        LogUtil.d(TAG,"startInternetCheckTask()")

        doAsync{
            var result = false
            try
            {
                val urlc = URL(AppConfig.VERFY_INTERNET_DOMAIN_URL).openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "Test")
                urlc.setRequestProperty("Connection", "close")
                urlc.connectTimeout = 1500
                urlc.connect()
                if(urlc.responseCode == 200)
                {
                    result = true
                }
            }
            catch(e: IOException)
            {
                LogUtil.d(TAG, "Error checking internet connection = $e")
                result = false
            }

            try
            {
                Thread.sleep(1000)
            }
            catch(e: InterruptedException)
            {
                e.printStackTrace()
            }

            when(result)
            {
                true ->
                {
                    runOnUiThread{
                        setup_connecting_internet_title_text.text = getString(R.string.setup_connecting_internet_success_title)
                        setup_connecting_internet_description_text.visibility = View.GONE
                        setup_connecting_internet_content_animation_view.setAnimation("ConnectToTheInternet_2_oldJson.json")
                        setup_connecting_internet_content_animation_view.playAnimation()
                    }

                    Thread.sleep(2500)

                    startGetUIDTask()
                }

                false ->
                {
                    val bundle = Bundle().apply{
                        putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_NO_INTERNET)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
                }
            }
        }
    }

    private fun startGetUIDTask()
    {
        LogUtil.d(TAG,"startGetUIDTask()")
        GatewayApi.GetCloudAgentInfo()
                .setRequestPageName(TAG)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            cloudAgentInfo = Gson().fromJson(responseStr, CloudAgentInfo::class.javaObjectType)
                            LogUtil.d(TAG,"getUID:$cloudAgentInfo")

                            if(cloudAgentInfo.Object.TUTK_UID.isNotEmpty()
                                && cloudAgentInfo.Object.TUTK_UID != "N/A"
                                && cloudAgentInfo.Object.TUTK_UID != ""
                                && cloudAgentInfo.Object.TUTK_UID != " ")
                            {
                                hasUID = true
                                GlobalData.currentUID = cloudAgentInfo.Object.TUTK_UID
                            }

                            startGetPreviousSettingsTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun startGetPreviousSettingsTask()
    {
        LogUtil.d(TAG,"startGetPreviousSettingsTask()")

        doAsync{
            siteInfoList = db.getSiteInfoDao().queryByBackup(true)
            hasPreviousSettings = siteInfoList.isNotEmpty()

            uiThread{
                if(hasUID)
                {
                    if(hasPreviousSettings)
                        GlobalBus.publish(MainEvent.SwitchToFrag(SetupApplyPreviousSettingsFragment()))
                    else
                        GlobalBus.publish(MainEvent.SwitchToFrag(ConnectToCloudFragment()))
                }
                else
                {
                    DatabaseUtil.getInstance(activity!!)?.updateInformationToDB(gatewayInfo)
                    GlobalBus.publish(MainEvent.EnterHomePage())
                }

                GlobalBus.publish(MainEvent.HideLoading())
            }
        }
    }
}