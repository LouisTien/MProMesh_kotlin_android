package zyxel.com.multyproneo.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.adapter_mesh_topology_device_center.view.*
import kotlinx.android.synthetic.main.fragment_mesh_topology_viewpager.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.runOnUiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.TreeNode
import zyxel.com.multyproneo.dialog.MeshTopologyRssiStatusDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class MeshTopologyPageFragment(
    private val layer2EndDeviceList: MutableList<TreeNode<DevicesInfoObject>>,
    private val rootNodeMAC: String
) :
    Fragment() {

    private val TAG = "MeshTopologyPageFragment"
    private lateinit var inflator: LayoutInflater
    private lateinit var rootView: View
    private val GuidelineID = 100005
    private val positionLeft = "Left"
    private val positionCenter = "Center"
    private val positionRight = "right"
    private lateinit var viewL2Left: View
    private lateinit var viewL2Center: View
    private lateinit var viewL2Right: View
    private lateinit var viewL3Left: View
    private lateinit var viewL3Center: View
    private lateinit var viewL3Right: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mesh_topology_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view
        inflator =
            requireActivity().applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        initUI(mesh_topology_viewpager_area)
    }

    private fun initUI(view: ConstraintLayout) {
        viewL2Left = inflator.inflate(R.layout.adapter_mesh_topology_device_left, null)
        viewL2Center = inflator.inflate(R.layout.adapter_mesh_topology_device_center, null)
        viewL2Right = inflator.inflate(R.layout.adapter_mesh_topology_device_right, null)
        viewL3Left = inflator.inflate(R.layout.adapter_mesh_topology_device_center, null)
        viewL3Center = inflator.inflate(R.layout.adapter_mesh_topology_device_center, null)
        viewL3Right = inflator.inflate(R.layout.adapter_mesh_topology_device_center, null)

        viewL2Left.id = View.generateViewId()
        viewL2Center.id = View.generateViewId()
        viewL2Right.id = View.generateViewId()
        viewL3Left.id = View.generateViewId()
        viewL3Center.id = View.generateViewId()
        viewL3Right.id = View.generateViewId()

        view.id = View.generateViewId()
        view.addView(viewL2Left)
        view.addView(viewL2Center)
        view.addView(viewL2Right)
        view.addView(viewL3Left)
        view.addView(viewL3Center)
        view.addView(viewL3Right)

        val constraintSet = ConstraintSet().apply {
            clone(view)
            create(GuidelineID, ConstraintSet.VERTICAL_GUIDELINE)
            setVisibility(GuidelineID, ConstraintSet.VISIBLE)
            setGuidelinePercent(GuidelineID, 0.5f)
            centerHorizontally(viewL2Center.id, ConstraintSet.PARENT_ID)

            connect(viewL2Center.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
            connect(viewL2Center.id, ConstraintSet.END, view.id, ConstraintSet.END)
            connect(viewL2Center.id, ConstraintSet.START, view.id, ConstraintSet.START)

            connect(viewL2Left.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
            connect(viewL2Left.id, ConstraintSet.END, GuidelineID, ConstraintSet.START)

            connect(viewL2Right.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
            connect(viewL2Right.id, ConstraintSet.START, GuidelineID, ConstraintSet.END)

            connect(viewL3Left.id, ConstraintSet.TOP, viewL2Left.id, ConstraintSet.BOTTOM)
            connect(viewL3Left.id, ConstraintSet.START, viewL2Left.id, ConstraintSet.START)
            connect(viewL3Left.id, ConstraintSet.END, viewL2Left.id, ConstraintSet.END)

            connect(viewL3Center.id, ConstraintSet.TOP, viewL2Center.id, ConstraintSet.BOTTOM)
            connect(viewL3Center.id, ConstraintSet.START, viewL2Center.id, ConstraintSet.START)
            connect(viewL3Center.id, ConstraintSet.END, viewL2Center.id, ConstraintSet.END)

            connect(viewL3Right.id, ConstraintSet.TOP, viewL2Right.id, ConstraintSet.BOTTOM)
            connect(viewL3Right.id, ConstraintSet.START, viewL2Right.id, ConstraintSet.START)
            connect(viewL3Right.id, ConstraintSet.END, viewL2Right.id, ConstraintSet.END)
        }
        constraintSet.applyTo(view)
    }


    override fun onResume() {
        super.onResume()
        updateUI()
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun updateUI() {
        runOnUiThread {
            settingL2TopologyView()
        }
    }

    private fun settingL2TopologyView() {
        viewL2Center.mesh_topology_device_status_image.visibility = View.GONE

        when (layer2EndDeviceList.size) {
            0 -> {
                viewL2Left.visibility = View.GONE
                viewL2Center.visibility = View.GONE
                viewL2Right.visibility = View.GONE
                viewL3Left.visibility = View.GONE
                viewL3Center.visibility = View.GONE
                viewL3Right.visibility = View.GONE
            }
            1 -> {
                viewL2Left.visibility = View.GONE
                viewL2Center.visibility = View.VISIBLE
                viewL2Right.visibility = View.GONE
                viewL3Left.visibility = View.GONE
                viewL3Center.visibility = View.VISIBLE
                viewL3Right.visibility = View.GONE

                for ((count, item) in layer2EndDeviceList.withIndex()) {
                    when (count) {
                        0 -> {
                            setL2NodeDeviceInfo(
                                viewL2Center,
                                item.data,
                                positionCenter
                            )

                            settingL3TopologyView(
                                viewL3Center,
                                item.getChildren(),
                                positionCenter
                            )
                        }
                        else -> {}
                    }
                }
            }
            2 -> {
                viewL2Left.visibility = View.VISIBLE
                viewL2Center.visibility = View.GONE
                viewL2Right.visibility = View.VISIBLE
                viewL3Left.visibility = View.VISIBLE
                viewL3Center.visibility = View.GONE
                viewL3Right.visibility = View.VISIBLE

                for ((count, item) in layer2EndDeviceList.withIndex()) {
                    when (count) {
                        0 -> {
                            setL2NodeDeviceInfo(
                                viewL2Left,
                                item.data,
                                positionLeft
                            )
                            settingL3TopologyView(
                                viewL3Left,
                                item.getChildren(),
                                positionCenter
                            )
                        }

                        1 -> {
                            setL2NodeDeviceInfo(
                                viewL2Right,
                                item.data,
                                positionRight
                            )

                            settingL3TopologyView(
                                viewL3Right,
                                item.getChildren(),
                                positionCenter
                            )
                        }
                        else -> {}
                    }
                }
            }
            3 -> {
                viewL2Left.visibility = View.VISIBLE
                viewL2Center.visibility = View.VISIBLE
                viewL2Right.visibility = View.VISIBLE
                viewL3Left.visibility = View.VISIBLE
                viewL3Center.visibility = View.VISIBLE
                viewL3Right.visibility = View.VISIBLE

                for ((count, item) in layer2EndDeviceList.withIndex()) {
                    LogUtil.d(TAG, "layer2EndDeviceList info:${item.data.getName()}")
                    LogUtil.d(TAG, "layer2EndDeviceList children info:${item.getChildren().size}")

                    when (count) {
                        0 -> {
                            setL2NodeDeviceInfo(
                                viewL2Left,
                                item.data,
                                positionLeft
                            )
                            settingL3TopologyView(
                                viewL3Left,
                                item.getChildren(),
                                positionCenter
                            )
                        }

                        1 -> {
                            setL2NodeDeviceInfo(
                                viewL2Center,
                                item.data,
                                positionCenter
                            )
                            settingL3TopologyView(
                                viewL3Center,
                                item.getChildren(),
                                positionCenter
                            )
                        }

                        2 -> {
                            setL2NodeDeviceInfo(
                                viewL2Right,
                                item.data,
                                positionRight
                            )

                            settingL3TopologyView(
                                viewL3Right,
                                item.getChildren(),
                                positionCenter
                            )
                        }

                        else -> {}
                    }
                }
            }
            else -> {}
        }

    }

    private fun settingL3TopologyView(
        viewLayer3: View,
        data: List<TreeNode<DevicesInfoObject>>,
        position: String
    ) {
        viewLayer3.mesh_topology_device_status_image.visibility = View.GONE
        viewLayer3.mesh_topology_device_role.text = GlobalData.Satellite

        when (data.size) {
            0 -> viewLayer3.visibility = View.GONE
            1 -> {
                LogUtil.d(TAG, "parent:${data[0].parent?.data}")

                viewLayer3.mesh_topology_device_hostname.text = data[0].data.getName()
                viewLayer3.mesh_topology_line_image.imageResource =
                    checkConnection(
                        SpecialCharacterHandler.checkEmptyTextValue(data[0].data.X_ZYXEL_RSSI_STAT),
                        SpecialCharacterHandler.checkEmptyTextValue(data[0].data.X_ZYXEL_ConnectionType),
                        position
                    )

                if (data[0].data.X_ZYXEL_ConnectionType.contains("WiFi", ignoreCase = true)
                    || data[0].data.X_ZYXEL_ConnectionType.contains("Wi-Fi", ignoreCase = true)
                ) {
                    viewLayer3.mesh_topology_line_image.setOnClickListener(View.OnClickListener {
                        MeshTopologyRssiStatusDialog(
                            requireActivity(),
                            SpecialCharacterHandler.checkEmptyTextValue(data[0].data.X_ZYXEL_PhyRate.toString()),
                            SpecialCharacterHandler.checkEmptyTextValue(data[0].data.X_ZYXEL_RSSI.toString())
                        ).show()
                    })
                }
                val (connectedDeviceCount, deviceList) = calculateConnectedDeviceCount(
                    SpecialCharacterHandler.checkEmptyTextValue(data[0].data.PhysAddress)
                )

                viewLayer3.mesh_topology_connected_count.text = connectedDeviceCount.toString()
                viewLayer3.mesh_topology_device_image.setOnClickListener(View.OnClickListener {
                    if (data[0].getChildren().isNotEmpty()) {
                        val bundle = Bundle().apply {
                            putSerializable("RootNodeDeviceInfo", data[0].parent?.data)
                            putBoolean("isGateway", false)
                            putString(GlobalData.SelectedNodeMAC, data[0].data.PhysAddress)
                        }

                        GlobalBus.publish(MainEvent.SwitchToFrag(MeshTopologyFragment().apply {
                            arguments = bundle
                        }))
                    } else {
                        if (connectedDeviceCount > 0) {
                            val bundle = Bundle().apply {
                                putString(GlobalData.SelectedNodeMAC, data[0].data.PhysAddress)
                                putString(GlobalData.RootNodeMAC, rootNodeMAC)
                            }

                            GlobalBus.publish(MainEvent.SwitchToFrag(DevicesListFragment().apply {
                                arguments = bundle
                            }))
                        }
                    }
                })
            }
            else -> {
                viewLayer3.mesh_topology_device_hostname.text = "${data.size} Mesh Devices"
                viewLayer3.mesh_topology_device_frame.backgroundResource =
                    R.drawable.mesh_topology_multi_device_bg
                viewLayer3.mesh_topology_connected_count_frame.visibility = View.GONE
                viewLayer3.mesh_topology_line_image.imageResource = R.drawable.straight_double
                viewLayer3.mesh_topology_device_image.setOnClickListener(View.OnClickListener {
                    val bundle = Bundle().apply {
                        putSerializable("RootNodeDeviceInfo", data[0].parent?.data)
                        putBoolean("isGateway", false)
                        putString(GlobalData.SelectedNodeMAC, data[0].data.PhysAddress)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(MeshTopologyFragment().apply {
                        arguments = bundle
                    }))
                })
            }
        }
        mesh_topology_viewpager_area.visibility = View.VISIBLE
    }

    private fun setL2NodeDeviceInfo(view: View, data: DevicesInfoObject, position: String) {
        view.mesh_topology_line_image.imageResource =
            checkConnection(
                SpecialCharacterHandler.checkEmptyTextValue(data.X_ZYXEL_RSSI_STAT),
                SpecialCharacterHandler.checkEmptyTextValue(data.X_ZYXEL_ConnectionType),
                position
            )
        if (data.X_ZYXEL_ConnectionType.contains("WiFi", ignoreCase = true)
            || data.X_ZYXEL_ConnectionType.contains("Wi-Fi", ignoreCase = true)
        ) {
            view.mesh_topology_line_image.setOnClickListener(View.OnClickListener {
                MeshTopologyRssiStatusDialog(
                    requireActivity(),
                    SpecialCharacterHandler.checkEmptyTextValue(data.X_ZYXEL_PhyRate.toString()),
                    SpecialCharacterHandler.checkEmptyTextValue(data.X_ZYXEL_RSSI.toString())
                ).show()
            })
        }
        view.mesh_topology_device_hostname.text =
            SpecialCharacterHandler.checkEmptyTextValue(data.getName())
        view.mesh_topology_device_role.text = GlobalData.Satellite


        val (connectedDeviceCount, deviceList) = calculateConnectedDeviceCount(
            SpecialCharacterHandler.checkEmptyTextValue(data.PhysAddress)
        )

        view.mesh_topology_connected_count.text = connectedDeviceCount.toString()
        view.mesh_topology_device_image.imageResource = R.drawable.wx3100_t0

        view.mesh_topology_device_image.setOnClickListener(View.OnClickListener {

            if (connectedDeviceCount > 0) {
                val bundle = Bundle().apply {
                    putString(GlobalData.SelectedNodeMAC, data.PhysAddress)
                    putString(GlobalData.RootNodeMAC, rootNodeMAC)
                }

                GlobalBus.publish(MainEvent.SwitchToFrag(DevicesListFragment().apply {
                    arguments = bundle
                }))
            }
        })
    }

    private fun calculateConnectedDeviceCount(mac: String): Pair<Int, MutableList<DevicesInfoObject>> {
        var connectedDeviceCount = 0
        val deviceList = mutableListOf<DevicesInfoObject>()

        for (item in GlobalData.homeEndDeviceList) {
            if (item.X_ZYXEL_Neighbor == mac &&
                item.Active
            ) {
                deviceList.add(item)
                connectedDeviceCount++
            }
        }
        for (item in GlobalData.guestEndDeviceList) {
            if (item.X_ZYXEL_Neighbor == mac &&
                item.Active
            ) {
                deviceList.add(item)
                connectedDeviceCount++
            }
        }
        return Pair(connectedDeviceCount, deviceList)
    }

    private fun checkConnection(
        xZyxelRssiStat: String,
        connectionType: String,
        position: String
    ): Int {

        if (connectionType.contains("WiFi", ignoreCase = true)
            || connectionType.contains("Wi-Fi", ignoreCase = true)
        ) {
            with(xZyxelRssiStat) {
                when {
                    equals("Good", ignoreCase = true) -> {
                        return when (position) {
                            positionLeft -> R.drawable.left_wireless_good
                            positionCenter -> R.drawable.straight_wireless_good
                            positionRight -> R.drawable.right_wireless_good
                            else -> R.drawable.straight_wireless_good
                        }
                    }
                    equals("Weak", ignoreCase = true) -> {
                        return when (position) {
                            positionLeft -> R.drawable.left_wireless_weak
                            positionCenter -> R.drawable.straight_wireless_weak
                            positionRight -> R.drawable.right_wireless_weak
                            else -> R.drawable.straight_wireless_weak
                        }
                    }
                    equals("TooClose", ignoreCase = true) || equals(
                        "Too Close",
                        ignoreCase = true
                    ) -> {
                        return when (position) {
                            positionLeft -> R.drawable.left_wireless_good
                            positionCenter -> R.drawable.straight_wireless_good
                            positionRight -> R.drawable.right_wireless_good
                            else -> R.drawable.straight_wireless_good
                        }
                    }
                    else -> {
                        return when (position) {
                            positionLeft -> R.drawable.left_wireless_no
                            positionCenter -> R.drawable.straight_wireless_no
                            positionRight -> R.drawable.right_wireless_no
                            else -> R.drawable.straight_wireless_no
                        }
                    }
                }
            }
        } else {
            return when (position) {
                positionLeft -> R.drawable.left_wired_good
                positionCenter -> R.drawable.straight_wired_good
                positionRight -> R.drawable.right_wired_good
                else -> R.drawable.straight_wired_good
            }
        }
    }

    private fun setDeviceIconByModelName(modelName: String): Int {
        when (modelName) {
            GlobalData.AX7501_B1 -> {
                return R.drawable.ex5601_t0
            }
            GlobalData.DX3300_T1 -> {
                return R.drawable.ex3300_t1
            }
            GlobalData.DX3301_T0 -> {
                return R.drawable.ex3301_t0
            }
            GlobalData.EMG3525_T50B -> {
                return R.drawable.emg3525_t50b
            }
            GlobalData.EMG3525_T50C -> {
                return R.drawable.emg3525_t50c
            }
            GlobalData.EMG5523_T50B -> {
                return R.drawable.emg5523_t50b
            }
            GlobalData.EMG5723_T50K -> {
                return R.drawable.emg5723_t50k
            }
            GlobalData.EX3200_T0 -> {
                return R.drawable.ex3200_t0
            }
            GlobalData.EX3300_T1 -> {
                return R.drawable.ex3300_t1
            }
            GlobalData.EX3301_T0 -> {
                return R.drawable.ex3301_t0
            }
            GlobalData.EX5510_B0 -> {
                return R.drawable.ex5510_b0
            }
            GlobalData.EX5600_T0 -> {
                return R.drawable.ex5600_t0
            }
            GlobalData.EX5601_T0 -> {
                return R.drawable.ex5601_t0
            }
            GlobalData.EX7501_B0 -> {
                return R.drawable.ex7501_b0
            }
            GlobalData.PX5111_T0 -> {
                return R.drawable.px5111_t0
            }
            GlobalData.PX5501_B1 -> {
                return R.drawable.px5501_b1
            }
            GlobalData.VMG3625_T50B -> {
                return R.drawable.vmg3625_t50b
            }
            GlobalData.VMG3625_T50C -> {
                return R.drawable.vmg3625_t50c
            }
            GlobalData.VMG3927_T50K -> {
                return R.drawable.vmg3927_t50k
            }
            GlobalData.VMG8623_T50B -> {
                return R.drawable.vmg8623_t50b
            }
            GlobalData.VMG8825_T50K -> {
                return R.drawable.vmg8825_t50k
            }
            GlobalData.WAP6807 -> {
                return R.drawable.vmg3625_t50b
            }
            GlobalData.WX3100_T0 -> {
                return R.drawable.wx3100_t0
            }
            GlobalData.WX5600_T0 -> {
                return R.drawable.wx5600_t0
            }
            else -> {
                return R.drawable.ex5601_t0
            }
        }
    }
}