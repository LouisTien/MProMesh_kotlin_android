package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_settings_cloud_account_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.uiThread
import org.json.JSONException
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudAccountWiFiRouterItemAdapter
import zyxel.com.multyproneo.api.cloud.AMDMApi
import zyxel.com.multyproneo.api.cloud.NotificationApi
import zyxel.com.multyproneo.api.cloud.TUTKCommander
import zyxel.com.multyproneo.database.room.DatabaseClientListEntity
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.dialog.RemoveSiteDialog
import zyxel.com.multyproneo.event.CloudAccountEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.cloud.AllDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.util.*
import java.util.HashMap

class CloudSettingsCloudAccountDetailFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var db: DatabaseCloudUtil
    private lateinit var siteSelectedDisposable: Disposable
    private lateinit var siteDeleteDisposable: Disposable
    private lateinit var confirmSiteDeleteDisposable: Disposable
    private lateinit var delInfo: AllDeviceInfo
    private lateinit var clientList: List<DatabaseClientListEntity>
    private var siteInfo: DatabaseSiteInfoEntity? = null
    private var deleteMode = false
    private var keepBackup = true
    private var isDelSelf = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_settings_cloud_account_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseCloudUtil.getInstance(activity!!)!!

        siteSelectedDisposable = GlobalBus.listen(CloudAccountEvent.OnSiteSelect::class.java).subscribe{
            LogUtil.d(TAG,"OnSiteSelect UID:${it.uid}")
        }

        siteDeleteDisposable = GlobalBus.listen(CloudAccountEvent.OnSiteDelete::class.java).subscribe{
            LogUtil.d(TAG,"OnSiteSelect info:${it.info}")
            LogUtil.d(TAG,"OnSiteSelect is self :${it.self}")
            isDelSelf = it.self
            RemoveSiteDialog(activity!!, it.info, it.self, AppConfig.RemoveSitePage.RM_SITE_PAGE_CLOUD_ACCOUNT).show()
        }

        confirmSiteDeleteDisposable = GlobalBus.listen(CloudAccountEvent.ConfirmSiteDelete::class.java).subscribe{
            LogUtil.d(TAG,"ConfirmSiteDelete info:${it.info}")
            LogUtil.d(TAG,"ConfirmSiteDelete backup :${it.backup}")
            delInfo = it.info
            keepBackup = it.backup
            doAsync{
                GlobalBus.publish(MainEvent.ShowHintLoading(getString(R.string.remove_site_dialog_remove_description, delInfo.displayName)))
                Thread.sleep(2000)
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
        if(!siteSelectedDisposable.isDisposed) siteSelectedDisposable.dispose()
        if(!siteDeleteDisposable.isDisposed) siteDeleteDisposable.dispose()
        if(!confirmSiteDeleteDisposable.isDisposed) confirmSiteDeleteDisposable.dispose()
    }

    private val clickListener = View.OnClickListener { view ->
        when(view)
        {
            settings_cloud_account_detail_back_image -> GlobalBus.publish(MainEvent.EnterCloudSettingsPage())

            settings_cloud_account_detail_router_list_action_text ->
            {
                deleteMode = !deleteMode
                updateUI()
            }
        }
    }

    private fun setClickListener()
    {
        settings_cloud_account_detail_back_image.setOnClickListener(clickListener)
        settings_cloud_account_detail_router_list_action_text.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        GlobalBus.publish(MainEvent.HideLoading())

        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        runOnUiThread{
            settings_cloud_account_detail_value_text.text = GlobalData.currentEmail
            settings_cloud_account_detail_router_list.adapter = CloudAccountWiFiRouterItemAdapter(moveSelfSiteToFirst(GlobalData.cloudGatewayListInfo), deleteMode)
            settings_cloud_account_detail_router_list_action_text.text =
                    if(deleteMode)
                        getString(R.string.settings_cloud_account_router_action_done)
                    else
                        getString(R.string.settings_cloud_account_router_action_edit)
        }
    }

    private fun moveSelfSiteToFirst(info: TUTKAllDeviceInfo): TUTKAllDeviceInfo
    {
        var selfIndex = 0
        var siteListInfo = TUTKAllDeviceInfo()
        var selfInfo = AllDeviceInfo()
        for(i in info.data.indices)
        {
            if(GlobalData.currentUID == info.data[i].udid)
            {
                selfIndex = i
                selfInfo = info.data[i]
            }

            siteListInfo.data.add(info.data[i])
        }

        siteListInfo.data.removeAt(selfIndex)
        siteListInfo.data.add(0, selfInfo)

        return siteListInfo
    }

    private fun deleteDevice()
    {
        LogUtil.d(TAG,"deleteDevice()")

        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        AMDMApi.DeleteDevice(delInfo.udid)
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

    fun removeMappingNoti()
    {
        LogUtil.d(TAG,"removeMappingNoti()")

        val phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "rm_mapping"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["uid"] = GlobalData.currentUID
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
                siteInfo = db.getSiteInfoDao().queryByUid(delInfo.udid)
                if(siteInfo != null)
                {
                    db.getSiteInfoDao().delete(siteInfo!!)
                    clientList = db.getClientListDao().queryByMac(siteInfo!!.mac)
                    for(item in clientList)
                        db.getClientListDao().delete(item)
                }
            }

            uiThread{
                if(isDelSelf)
                {
                    GlobalBus.publish(MainEvent.HideLoading())
                    GlobalBus.publish(MainEvent.SwitchToFrag(CloudWelcomeFragment()))
                }
                else
                    getAllDevice()
            }
        }
    }
}