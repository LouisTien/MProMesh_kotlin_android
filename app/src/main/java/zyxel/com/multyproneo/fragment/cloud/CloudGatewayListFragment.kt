package zyxel.com.multyproneo.fragment.cloud

import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_cloud_gateway_list.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudGatewayItemAdapter
import zyxel.com.multyproneo.api.cloud.*
import zyxel.com.multyproneo.database.room.DatabaseClientListEntity
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.dialog.RemoveSiteDialog
import zyxel.com.multyproneo.event.GatewayListEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.cloud.AllDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.util.*
import java.util.HashMap

class CloudGatewayListFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var db: DatabaseCloudUtil
    private lateinit var deviceSelectedDisposable: Disposable
    private lateinit var deviceDeleteDisposable: Disposable
    private lateinit var confirmSiteDeleteDisposable: Disposable
    private lateinit var gatewayListInfo: TUTKAllDeviceInfo
    private lateinit var deviceInfo: AllDeviceInfo
    private lateinit var clientList: List<DatabaseClientListEntity>
    private var siteInfo: DatabaseSiteInfoEntity? = null
    private var autoLogin = true
    private var deleteMode = false
    private var keepBackup = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_gateway_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseCloudUtil.getInstance(activity!!)!!

        with(arguments)
        {
            this?.getBoolean("AutoLogin", true)?.let{ autoLogin = it }
        }

        deviceSelectedDisposable = GlobalBus.listen(GatewayListEvent.OnDeviceSelected::class.java).subscribe{
            deviceInfo = gatewayListInfo.data[it.index]
            LogUtil.d(TAG,"select name:${deviceInfo.displayName}")
            LogUtil.d(TAG,"select udid:${deviceInfo.udid}")
            LogUtil.d(TAG,"select credential:${deviceInfo.credential}")
            connectP2P()
        }

        deviceDeleteDisposable = GlobalBus.listen(GatewayListEvent.OnDeviceDelete::class.java).subscribe{
            deviceInfo = gatewayListInfo.data[it.index]
            LogUtil.d(TAG,"delete name:${deviceInfo.displayName}")
            LogUtil.d(TAG,"delete udid:${deviceInfo.udid}")
            LogUtil.d(TAG,"delete credential:${deviceInfo.credential}")
            autoLogin = false
            RemoveSiteDialog(activity!!, deviceInfo, false, AppConfig.RemoveSitePage.RM_SITE_PAGE_CLOUD_GATEWAY_LIST).show()
        }

        confirmSiteDeleteDisposable = GlobalBus.listen(GatewayListEvent.ConfirmDeviceDelete::class.java).subscribe{
            LogUtil.d(TAG,"ConfirmSiteDelete info:${it.info}")
            LogUtil.d(TAG,"ConfirmSiteDelete backup :${it.backup}")
            deviceInfo = it.info
            keepBackup = it.backup
            doAsync{
                GlobalBus.publish(MainEvent.ShowLoading())
                uiThread{ deleteDevice() }
            }
        }

        setClickListener()
        updateUI()
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
        if(!deviceSelectedDisposable.isDisposed) deviceSelectedDisposable.dispose()
        if(!deviceDeleteDisposable.isDisposed) deviceDeleteDisposable.dispose()
        if(!confirmSiteDeleteDisposable.isDisposed) confirmSiteDeleteDisposable.dispose()
    }

    private val clickListener = View.OnClickListener { view ->
        when(view)
        {

            cloud_gateway_add_image -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectingControllerFragment()))

            cloud_gateway_edit_text ->
            {
                if(gatewayListInfo.data.isNotEmpty())
                {
                    autoLogin = false
                    deleteMode = !deleteMode
                    updateUI()
                }
            }
        }
    }

    private fun setClickListener()
    {
        cloud_gateway_add_image.setOnClickListener(clickListener)
        cloud_gateway_edit_text.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        GlobalBus.publish(MainEvent.HideLoading())

        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        gatewayListInfo = GlobalData.cloudGatewayListInfo

        runOnUiThread{
            with(cloud_gateway_list_view)
            {
                setHasFixedSize(true)
                //layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                layoutManager = GridLayoutManager(activity, 2)
                adapter = CloudGatewayItemAdapter(gatewayListInfo, deleteMode)
            }

            if(gatewayListInfo.data.isEmpty())
            {
                cloud_gateway_edit_text.textColor = activity!!.resources.getColor(R.color.color_a3a3a3)
                cloud_gateway_choose_text.text = getString(R.string.cloud_gateway_list_no_choose)
            }
            else
            {
                cloud_gateway_edit_text.textColor = activity!!.resources.getColor(R.color.color_575757)
                cloud_gateway_choose_text.text = getString(R.string.cloud_gateway_list_choose)
            }

            if(deleteMode)
            {
                cloud_gateway_edit_text.text = getString(R.string.settings_cloud_account_router_action_done)
                cloud_gateway_edit_text.typeface = Typeface.DEFAULT_BOLD
            }
            else
            {
                cloud_gateway_edit_text.text = getString(R.string.settings_cloud_account_router_action_edit)
                cloud_gateway_edit_text.typeface = Typeface.DEFAULT
            }

            if(autoLogin && gatewayListInfo.data.size == 1)
            {
                deviceInfo = gatewayListInfo.data[0]
                connectP2P()
            }
        }
    }

    private fun connectP2P()
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        doAsync{
            TUTKP2PBaseApi.stopSession()
            if(TUTKP2PBaseApi.initIOTCRDT() >= 0)
            {
                if(TUTKP2PBaseApi.startSession(deviceInfo.udid) >= 0)
                {
                    GlobalData.currentDisplayName = deviceInfo.displayName
                    GlobalData.currentUID = deviceInfo.udid
                    verifyCloudAgentTask()
                }
                else
                    gotoTroubleShooting()
            }
            else
                gotoTroubleShooting()
        }
    }

    private fun verifyCloudAgentTask()
    {
        LogUtil.d(TAG,"verifyCloudAgentTask()")

        val params = ",\"credential\":\"${deviceInfo.credential}\""

        P2PGatewayApi.VerifyCloudAgent()
                .setRequestPageName(TAG)
                .setRequestPayload(params)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val result = data.get("oper_status").toString()
                            if(result.equals("Success", ignoreCase = true))
                            {
                                GlobalData.currentCredential = deviceInfo.credential
                                GlobalBus.publish(MainEvent.HideLoading())
                                GlobalBus.publish(MainEvent.SwitchToFrag(CloudHomeFragment()))
                            }
                            else
                                gotoTroubleShooting()
                        }
                        catch(e: Exception)
                        {
                            e.printStackTrace()
                            gotoTroubleShooting()
                        }
                    }
                }).execute()
    }

    private fun deleteDevice()
    {
        LogUtil.d(TAG,"deleteDevice()")

        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        AMDMApi.DeleteDevice(deviceInfo.udid)
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"deleteDevice:$responseStr")
                        removeMappingNoti()
                    }
                }).execute()
    }

    fun removeMappingNoti()
    {
        LogUtil.d(TAG,"removeMappingNoti()")

        val phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "rm_mapping"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["uid"] = deviceInfo.udid
        body["udid"] = phoneUdid

        NotificationApi.Common(activity!!)
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setFormBody(body)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"NotificationApi removeMapping:$responseStr")
                        deleteFromDB()
                    }
                }).execute()
    }

    private fun deleteFromDB()
    {
        doAsync{

            if(!keepBackup)
            {
                siteInfo = db.getSiteInfoDao().queryByUid(deviceInfo.udid)
                if(siteInfo != null)
                {
                    db.getSiteInfoDao().delete(siteInfo!!)
                    clientList = db.getClientListDao().queryByMac(siteInfo!!.mac)
                    for(item in clientList)
                        db.getClientListDao().delete(item)
                }
            }

            uiThread{ getAllDevice() }
        }
    }

    private fun getAllDevice()
    {
        LogUtil.d(TAG,"getAllDevice()")

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
                            deleteMode = false
                            updateUI()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun gotoTroubleShooting()
    {
        TUTKP2PBaseApi.forceStopSession()

        GlobalBus.publish(MainEvent.HideLoading())

        val bundle = Bundle().apply{
            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_P2P_INIT_FAIL_IN_GATEWAY_LIST)
        }

        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
    }
}