package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_settings_cloud_account_detail.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudAccountWiFiRouterItemAdapter
import zyxel.com.multyproneo.event.CloudAccountEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.cloud.AllDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudSettingsCloudAccountDetailFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var siteSelectedDisposable: Disposable
    private lateinit var siteDeleteDisposable: Disposable
    private var deleteMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_settings_cloud_account_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        siteSelectedDisposable = GlobalBus.listen(CloudAccountEvent.OnSiteSelect::class.java).subscribe{
            LogUtil.d(TAG,"OnSiteSelect UID:${it.uid}")
        }

        siteDeleteDisposable = GlobalBus.listen(CloudAccountEvent.OnSiteDelete::class.java).subscribe{
            LogUtil.d(TAG,"OnSiteSelect info:${it.info}")
            LogUtil.d(TAG,"OnSiteSelect is self :${it.self}")
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
    }

    private val clickListener = View.OnClickListener { view ->
        when(view)
        {
            settings_cloud_account_detail_back_image -> GlobalBus.publish(MainEvent.EnterCloudSettingsPage())

            settings_cloud_account_detail_router_list_action_text -> {
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
        settings_cloud_account_detail_value_text.text = GlobalData.currentEmail
        settings_cloud_account_detail_router_list.adapter = CloudAccountWiFiRouterItemAdapter(moveSelfSiteToFirst(GlobalData.cloudGatewayListInfo), deleteMode)
        settings_cloud_account_detail_router_list_action_text.text =
                if(deleteMode)
                    getString(R.string.settings_cloud_account_router_action_done)
                else
                    getString(R.string.settings_cloud_account_router_action_edit)
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
}