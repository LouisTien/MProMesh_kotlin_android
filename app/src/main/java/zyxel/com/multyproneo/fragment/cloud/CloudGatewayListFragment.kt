package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_cloud_gateway_list.*
import org.jetbrains.anko.doAsync
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudGatewayItemAdapter
import zyxel.com.multyproneo.api.cloud.TUTKP2PBaseApi
import zyxel.com.multyproneo.event.GatewayListEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudGatewayListFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var deviceSelectedDisposable: Disposable
    private lateinit var gatewayListInfo: TUTKAllDeviceInfo
    private var autoLogin = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_gateway_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("AutoLogin")?.let{ autoLogin = it }
        }

        cloud_gateway_add_image.setOnClickListener{
            GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))
        }
    }

    override fun onResume()
    {
        super.onResume()

        GlobalBus.publish(MainEvent.HideBottomToolbar())

        deviceSelectedDisposable = GlobalBus.listen(GatewayListEvent.OnDeviceSelected::class.java).subscribe{
            LogUtil.d(TAG,"select index:${it.index}")
            LogUtil.d(TAG,"select name:${gatewayListInfo.data[it.index].displayName}")
            LogUtil.d(TAG,"select udid:${gatewayListInfo.data[it.index].udid}")

            connectP2P(gatewayListInfo.data[it.index].udid)
        }

        gatewayListInfo = GlobalData.cloudGatewayListInfo

        with(cloud_gateway_list_view)
        {
            setHasFixedSize(true)
            //layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            layoutManager = GridLayoutManager(activity, 2)
            adapter = CloudGatewayItemAdapter(gatewayListInfo)
        }

        if(gatewayListInfo.data.isEmpty())
            cloud_gateway_choose_text.text = getString(R.string.cloud_gateway_list_no_choose)
        else
            cloud_gateway_choose_text.text = getString(R.string.cloud_gateway_list_choose)

        if(autoLogin && gatewayListInfo.data.size == 1)
            connectP2P(gatewayListInfo.data[0].udid)
    }

    override fun onPause()
    {
        super.onPause()
        if(!deviceSelectedDisposable.isDisposed) deviceSelectedDisposable.dispose()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private fun connectP2P(uid: String)
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        doAsync{
            if(TUTKP2PBaseApi.initIOTCRDT() >= 0)
            {
                if(TUTKP2PBaseApi.startSession(uid) >= 0)
                {
                    GlobalData.currentUID = uid
                    GlobalBus.publish(MainEvent.SwitchToFrag(CloudHomeFragment()))
                }
                else
                    gotoTroubleShooting()
            }
            else
                gotoTroubleShooting()
        }
    }

    private fun gotoTroubleShooting()
    {
        GlobalBus.publish(MainEvent.HideLoading())

        val bundle = Bundle().apply{
            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_P2P_INIT_FAIL_IN_GATEWAY_LIST)
        }

        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
    }
}