package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_gateway_list.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.GatewayItemAdapter
import zyxel.com.multyproneo.event.GatewayListEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.GatewayProfile
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/5/30.
 */
class GatewayListFragment : Fragment()
{
    private lateinit var deviceSelectedDisposable: Disposable
    private lateinit var gatewayProfileMutableList: MutableList<GatewayProfile>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_gateway_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        gateway_retry_image.setOnClickListener{
            GlobalBus.publish(MainEvent.SwitchToFrag(FindingDeviceFragment()))
        }
    }

    override fun onResume()
    {
        super.onResume()

        GlobalBus.publish(MainEvent.HideBottomToolbar())

        deviceSelectedDisposable = GlobalBus.listen(GatewayListEvent.OnDeviceSelected::class.java).subscribe{
            OnDeviceSelected(it.index)
        }

        gatewayProfileMutableList = GlobalData.gatewayProfileMutableList

        with(gateway_list_view)
        {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            adapter = GatewayItemAdapter(gatewayProfileMutableList)
        }

        if(gatewayProfileMutableList.size == 1)
        {
            GlobalBus.publish(MainEvent.ShowLoading())
            OnDeviceSelected(0)
        }
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

    private fun OnDeviceSelected(index: Int)
    {
        GlobalData.currentGatewayIndex = index
        GlobalBus.publish(MainEvent.HideLoading())
        GlobalBus.publish(MainEvent.SwitchToFrag(LoginFragment()))
    }
}