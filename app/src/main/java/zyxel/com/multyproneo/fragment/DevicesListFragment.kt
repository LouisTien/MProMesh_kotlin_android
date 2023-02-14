package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_devices.*
import kotlinx.android.synthetic.main.fragment_devices_list.*
import org.jetbrains.anko.support.v4.runOnUiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudHomeGuestEndDeviceItemAdapter
import zyxel.com.multyproneo.api.ApiHandler
import zyxel.com.multyproneo.dialog.MeshDeviceStatusDialog
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.*
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by LinaLi on 2023/2/10.
 */
class DevicesListFragment : Fragment() {
    private val TAG = "DevicesListFragment"
    private lateinit var meshDevicePlacementStatusDisposable: Disposable
    private lateinit var showTipsDisposable: Disposable
    private var extenderMAC: String = ""
    private var rootNodeMAC: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_devices_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        with(arguments)
        {
            this?.getString("ExtenderMAC")?.let { extenderMAC = it }
            this?.getString("RootNodeMAC")?.let { rootNodeMAC = it }
        }

        getDeviceListInfo(extenderMAC)

        meshDevicePlacementStatusDisposable =
            GlobalBus.listen(DevicesEvent.MeshDevicePlacementStatus::class.java).subscribe {
                MeshDeviceStatusDialog(requireActivity(), it.isHomePage, false).show()
            }

        showTipsDisposable = GlobalBus.listen(DevicesEvent.ShowTips::class.java).subscribe {
            MessageDialog(
                requireActivity(),
                "",
                getString(R.string.devices_user_tips),
                arrayOf(getString(R.string.message_dialog_ok)),
                AppConfig.DialogAction.ACT_NONE
            ).show()
        }

        setClickListener()
    }

    private fun getDeviceListInfo(extenderMAC: String) {
        var connectedDeviceCount = 0
        LogUtil.d(TAG, "data.PhysAddress:$extenderMAC")
        val deviceList = mutableListOf<DevicesInfoObject>()

        if (extenderMAC == GlobalData.getCurrentGatewayInfo().MAC) {
            for (item in GlobalData.homeEndDeviceList) {
                if (item.Active) {
                    with(item.X_ZYXEL_Neighbor) {
                        when {
                            equals("gateway", ignoreCase = true) ||
                                    equals("unknown", ignoreCase = true) ||
                                    equals("NULL", ignoreCase = true) ||
                                    equals("N/A", ignoreCase = true) ||
                                    equals("", ignoreCase = true) ||
                                    equals(
                                        GlobalData.getCurrentGatewayInfo().MAC,
                                        ignoreCase = true
                                    ) ||
                                    isEmpty() -> {
                                connectedDeviceCount++
                                deviceList.add(item)
                            }
                            else -> {}
                        }
                    }
                }
            }
            for (item in GlobalData.guestEndDeviceList) {
                if (item.Active) {
                    with(item.X_ZYXEL_Neighbor) {
                        when {
                            equals("gateway", ignoreCase = true) ||
                                    equals("unknown", ignoreCase = true) ||
                                    equals("NULL", ignoreCase = true) ||
                                    equals("N/A", ignoreCase = true) ||
                                    equals("", ignoreCase = true) ||
                                    equals(
                                        GlobalData.getCurrentGatewayInfo().MAC,
                                        ignoreCase = true
                                    ) ||
                                    isEmpty() -> {
                                connectedDeviceCount++
                                deviceList.add(item)
                            }
                            else -> {}
                        }
                    }
                }
            }
        } else {
            for (item in GlobalData.homeEndDeviceList) {
                if (item.X_ZYXEL_Neighbor == SpecialCharacterHandler.checkEmptyTextValue(extenderMAC) &&
                    item.Active
                ) {
                    deviceList.add(item)
                    connectedDeviceCount++
                }
            }
            for (item in GlobalData.guestEndDeviceList) {
                if (item.X_ZYXEL_Neighbor == SpecialCharacterHandler.checkEmptyTextValue(extenderMAC) &&
                    item.Active
                ) {
                    deviceList.add(item)
                    connectedDeviceCount++
                }
            }
        }

        LogUtil.d(TAG, "connectedDeviceCount:${connectedDeviceCount}")
        LogUtil.d(TAG, "deviceList:${deviceList.size}")

        if (deviceList.isNotEmpty())
            device_list_devices_list.adapter =
                CloudHomeGuestEndDeviceItemAdapter(
                    requireActivity(),
                    deviceList,
                    false,
                    false,
                    true,
                    extenderMAC
                )
    }

    override fun onResume() {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!meshDevicePlacementStatusDisposable.isDisposed) meshDevicePlacementStatusDisposable.dispose()
        if (!showTipsDisposable.isDisposed) showTipsDisposable.dispose()
    }

    private val clickListener = View.OnClickListener { view ->
        when (view) {
            device_list_back_image -> {
                val temp =
                    GlobalData.ZYXELEndDeviceListTreeNode.filter { it.data.PhysAddress == rootNodeMAC }

                if (temp.isNotEmpty()) {
                    val bundle = Bundle().apply {
                        putSerializable("RootNodeDeviceInfo", temp[0].data)
                        putBoolean("isGateway", temp[0].isRootNode)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(MeshTopologyFragment().apply {
                        arguments = bundle
                    }))
                } else {
                    GlobalBus.publish(MainEvent.EnterNetworkTopologyPage())
                }
            }
        }
    }

    private fun setClickListener() {
        device_list_back_image.setOnClickListener(clickListener)
    }
}