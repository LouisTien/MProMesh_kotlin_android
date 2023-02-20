package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
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
    private lateinit var getInfoCompleteDisposable: Disposable
    private lateinit var stopRegularTaskDisposable: Disposable
    private lateinit var showTipsDisposable: Disposable
    private var selectedNodeMAC: String = ""
    private var rootNodeMAC: String = ""
    private var apiTimer = Timer()

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
            this?.getString(GlobalData.SelectedNodeMAC)?.let { selectedNodeMAC = it }
            this?.getString(GlobalData.RootNodeMAC)?.let { rootNodeMAC = it }
        }

        meshDevicePlacementStatusDisposable =
            GlobalBus.listen(DevicesEvent.MeshDevicePlacementStatus::class.java).subscribe {
                MeshDeviceStatusDialog(requireActivity(), it.isHomePage, false).show()
            }

        getInfoCompleteDisposable =
            GlobalBus.listen(ApiEvent.ApiExecuteComplete::class.java).subscribe {
                when (it.event) {
                    ApiHandler.API_RES_EVENT.API_RES_EVENT_DEVICES_API_REGULAR -> updateUI()
                    else -> {}
                }
            }

        stopRegularTaskDisposable =
            GlobalBus.listen(ApiEvent.StopRegularTask::class.java).subscribe {
                GlobalBus.publish(MainEvent.HideLoading())
                stopGetDeviceInfo()
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

    override fun onResume() {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
        startGetDeviceInfo()
    }

    override fun onPause() {
        super.onPause()
        stopGetDeviceInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!meshDevicePlacementStatusDisposable.isDisposed) meshDevicePlacementStatusDisposable.dispose()
        if (!getInfoCompleteDisposable.isDisposed) getInfoCompleteDisposable.dispose()
        if (!stopRegularTaskDisposable.isDisposed) stopRegularTaskDisposable.dispose()
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
                        putString(GlobalData.SelectedNodeMAC, selectedNodeMAC)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(MeshTopologyFragment().apply {
                        arguments = bundle
                    }))
                } else {
                    val bundle = Bundle().apply {
                        putBoolean("isGateway", true)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(MeshTopologyFragment().apply {
                        arguments = bundle
                    }))
                }
            }
        }
    }

    private fun setClickListener() {
        device_list_back_image.setOnClickListener(clickListener)
    }

    private fun updateUI() {

        runOnUiThread {
            device_list_devices_list.adapter =
                CloudHomeGuestEndDeviceItemAdapter(
                    requireActivity(),
                    getSelectNodeEndDeviceList(),
                    false,
                    false,
                    true,
                    selectedNodeMAC
                )
        }
    }

    private fun getSelectNodeEndDeviceList(): MutableList<DevicesInfoObject> {
        val deviceList = mutableListOf<DevicesInfoObject>()

        if (selectedNodeMAC == GlobalData.getCurrentGatewayInfo().MAC) {
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
                                deviceList.add(item)
                            }
                            else -> {}
                        }
                    }
                }
            }
        } else {
            for (item in GlobalData.homeEndDeviceList) {
                if (item.X_ZYXEL_Neighbor == SpecialCharacterHandler.checkEmptyTextValue(
                        selectedNodeMAC
                    ) &&
                    item.Active
                ) {
                    deviceList.add(item)
                }
            }
            for (item in GlobalData.guestEndDeviceList) {
                if (item.X_ZYXEL_Neighbor == SpecialCharacterHandler.checkEmptyTextValue(
                        selectedNodeMAC
                    ) &&
                    item.Active
                ) {
                    deviceList.add(item)
                }
            }
        }
        return deviceList
    }

    private fun startGetDeviceInfo() {
        val apiList = arrayListOf<ApiHandler.API_REF>()
        apiList.add(ApiHandler.API_REF.API_GET_CHANGE_ICON_NAME)
        apiList.add(ApiHandler.API_REF.API_GET_DEVICE_INFO)

        if (FeatureConfig.FeatureInfo.APPUICustomList.Parental_Control) {
            apiList.add(ApiHandler.API_REF.API_GET_PARENTAL_CONTROL_INFO)
            apiList.add(ApiHandler.API_REF.API_GET_GATEWAY_SYSTEM_DATE)
            apiList.add(ApiHandler.API_REF.API_CHECK_IN_USE_SELECT_DEVICE)
        }

        apiTimer = Timer()
        apiTimer.schedule(0, (AppConfig.endDeviceListUpdateTime * 1000).toLong())
        {
            ApiHandler().execute(
                ApiHandler.API_RES_EVENT.API_RES_EVENT_DEVICES_API_REGULAR,
                apiList
            )
        }
    }

    private fun stopGetDeviceInfo() {
        apiTimer.cancel()
    }
}