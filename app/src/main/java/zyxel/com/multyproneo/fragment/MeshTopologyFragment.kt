package zyxel.com.multyproneo.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.adapter_mesh_topology_device_center.view.*
import kotlinx.android.synthetic.main.fragment_mesh_topology.*
import org.jetbrains.anko.imageResource
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.TreeNode
import zyxel.com.multyproneo.adapter.MeshTopologyPageAdapter
import zyxel.com.multyproneo.dialog.MeshTopologyDeviceStatusDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class MeshTopologyFragment : Fragment() {

    private val TAG = "MeshTopologyFragment"
    private lateinit var inflator: LayoutInflater
    private var chunkLayer2DeviceList = listOf<List<TreeNode<DevicesInfoObject>>>()
    private var isGateway = false
    private var tabPosition = 0
    private var selectedNodeMAC = ""
    private var rootNodeDeviceInfo = DevicesInfoObject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mesh_topology, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getSerializable("RootNodeDeviceInfo")?.let {
                rootNodeDeviceInfo = it as DevicesInfoObject
            }
            this?.getBoolean("isGateway")?.let {
                isGateway = it
            }
            this?.getString(GlobalData.SelectedNodeMAC)?.let {
                selectedNodeMAC = it
            }
        }

        inflator =
            requireActivity().applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        GlobalData.ZYXELEndDeviceListTreeNode = processEndDeviceList()

        chunkLayer2DeviceList = if (isGateway) {
            val layer2EndDeviceList = GlobalData.ZYXELEndDeviceListTreeNode.filter { it.depth == 1 }
            layer2EndDeviceList.chunked(3)
        } else {
            val layer2EndDeviceList = GlobalData.ZYXELEndDeviceListTreeNode.filter {
                it.parent?.data?.PhysAddress == rootNodeDeviceInfo.PhysAddress
            }
            layer2EndDeviceList.chunked(3)
        }

        val fragmentList = mutableListOf<Fragment>()
        for (item in chunkLayer2DeviceList) {
            if (isGateway)
                fragmentList.add(
                    MeshTopologyPageFragment(
                        item as MutableList<TreeNode<DevicesInfoObject>>,
                        GlobalData.getCurrentGatewayInfo().MAC
                    )
                )
            else
                fragmentList.add(
                    MeshTopologyPageFragment(
                        item as MutableList<TreeNode<DevicesInfoObject>>,
                        rootNodeDeviceInfo.PhysAddress
                    )
                )
        }

        val pageAdapter = MeshTopologyPageAdapter(fragmentList, this)
        mesh_topology_viewpager.adapter = pageAdapter
        TabLayoutMediator(
            into_tab_layout,
            mesh_topology_viewpager
        ) { tab, position ->
        }.attach()

        into_tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                LogUtil.d("dddd", "onTabSelected tab.position: ${tab.position}")
                tabPosition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })



        settingL1TopologyView(isGateway)
        setClickListener()
    }

    private fun processEndDeviceList(): MutableList<TreeNode<DevicesInfoObject>> {
        GlobalData.layer2EndDeviceList.clear()

        //layer 1
        val rootNode = TreeNode(
            DevicesInfoObject(
                HostName = GlobalData.getCurrentGatewayInfo().getName(),
                PhysAddress = GlobalData.getCurrentGatewayInfo().MAC
            )
        )

        val deviceNode = mutableListOf<TreeNode<DevicesInfoObject>>()
        deviceNode.add(rootNode)

        //layer 2
        for (item in GlobalData.ZYXELEndDeviceList) {
            with(item.X_ZYXEL_Neighbor)
            {
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
                        GlobalData.layer2EndDeviceList.add(
                            TreeNode<DevicesInfoObject>(
                                item,
                                rootNode
                            )
                        )
                    }
                    else -> {}
                }
            }
        }

        deviceNode.addAll(GlobalData.layer2EndDeviceList)
        var depth = 1;
        while ((deviceNode.size - 1) < GlobalData.ZYXELEndDeviceList.size) {
            val tempDeviceList = mutableListOf<TreeNode<DevicesInfoObject>>()
            for (item in GlobalData.ZYXELEndDeviceList) {
                for (node in deviceNode) {
                    if (node.depth == depth) {
                        if (CommonTool.checkIsTheSameDeviceMac(
                                item.X_ZYXEL_Neighbor,
                                node.data.PhysAddress
                            )
                        ) {
                            tempDeviceList.add(TreeNode<DevicesInfoObject>(item, node))
                        }
                    }
                }
            }
            depth++
            deviceNode.addAll(tempDeviceList)
        }
        return deviceNode
    }

    private fun settingL1TopologyView(isGateway: Boolean) {

        mesh_topology_root_device_include.mesh_topology_line_image.visibility = View.GONE
        if (isGateway) {
            mesh_topology_root_device_include.mesh_topology_device_image.imageResource =
                R.drawable.ex5601_t0
            mesh_topology_root_device_include.mesh_topology_device_role.text = GlobalData.Controller
            mesh_topology_root_device_include.mesh_topology_device_status_image.imageResource =
                if (GlobalData.gatewayWanInfo.Object.Status == "Enable") R.drawable.icon_connected else R.drawable.icon_no_connection
            mesh_topology_root_device_include.mesh_topology_device_hostname.text =
                SpecialCharacterHandler.checkEmptyTextValue(GlobalData.getCurrentGatewayInfo().UserDefineName)

            var connectedDeviceCount = 0

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

                            }
                            else -> {}
                        }
                    }
                }
            }

            mesh_topology_root_device_include.mesh_topology_device_image.setOnClickListener(View.OnClickListener {

                LogUtil.d(
                    TAG,
                    "setOnClickListener GlobalData.getCurrentGatewayInfo().MAC:${GlobalData.getCurrentGatewayInfo().MAC}"
                )

                if (connectedDeviceCount > 0) {
                    val bundle = Bundle().apply {
                        putString(GlobalData.SelectedNodeMAC, GlobalData.getCurrentGatewayInfo().MAC)
                        putString(GlobalData.RootNodeMAC, GlobalData.getCurrentGatewayInfo().MAC)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(DevicesListFragment().apply {
                        arguments = bundle
                    }))
                }
            })
            mesh_topology_root_device_include.mesh_topology_connected_count.text =
                connectedDeviceCount.toString()


        } else {
            mesh_topology_root_device_include.mesh_topology_device_image.imageResource =
                R.drawable.wx3100_t0
            mesh_topology_root_device_include.mesh_topology_device_role.text = GlobalData.Satellite
            mesh_topology_root_device_include.mesh_topology_device_status_image.visibility =
                View.GONE
            mesh_topology_root_device_include.mesh_topology_device_hostname.text =
                SpecialCharacterHandler.checkEmptyTextValue(rootNodeDeviceInfo.getName())
            var connectedDeviceCount = 0
            val deviceList = mutableListOf<DevicesInfoObject>()
            for (item in GlobalData.homeEndDeviceList) {
                if (item.X_ZYXEL_Neighbor == SpecialCharacterHandler.checkEmptyTextValue(
                        rootNodeDeviceInfo.PhysAddress
                    ) &&
                    item.Active
                ) {
                    deviceList.add(item)
                    connectedDeviceCount++
                }
            }
            for (item in GlobalData.guestEndDeviceList) {
                if (item.X_ZYXEL_Neighbor == SpecialCharacterHandler.checkEmptyTextValue(
                        rootNodeDeviceInfo.PhysAddress
                    ) &&
                    item.Active
                ) {
                    deviceList.add(item)
                    connectedDeviceCount++
                }
            }

            mesh_topology_root_device_include.mesh_topology_connected_count.text =
                connectedDeviceCount.toString()

            mesh_topology_root_device_include.mesh_topology_device_image.setOnClickListener(View.OnClickListener {
                if (connectedDeviceCount > 0) {
                    val bundle = Bundle().apply {
                        putString(GlobalData.SelectedNodeMAC, rootNodeDeviceInfo.PhysAddress)
                        putString(GlobalData.RootNodeMAC, rootNodeDeviceInfo.PhysAddress)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(DevicesListFragment().apply {
                        arguments = bundle
                    }))
                }
            })
        }
        mesh_topology_root_area.visibility = View.VISIBLE
    }

    fun updateUI() {
        for ((count, item) in chunkLayer2DeviceList.withIndex()) {
            for (item2 in item) {
                if (item2.data.PhysAddress == selectedNodeMAC) {
                    into_tab_layout.getTabAt(count)?.select()
                    mesh_topology_viewpager.currentItem = count
                    break
                }
            }
        }
        mesh_topology_viewpager.visibility = View.VISIBLE
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

    private fun setClickListener() {
        mesh_topology_back_image.setOnClickListener(clickListener)
        mesh_topology_note_image.setOnClickListener(clickListener)
    }

    private val clickListener = View.OnClickListener { view ->
        when (view) {
            mesh_topology_back_image -> {
                if (isGateway) {
                    GlobalBus.publish(MainEvent.SwitchToFrag(HomeFragment()))
                } else {
                    val temp =
                        GlobalData.ZYXELEndDeviceListTreeNode.filter { it.data.PhysAddress == rootNodeDeviceInfo.PhysAddress }
                    if (temp.isNotEmpty()) {
                        val bundle = Bundle().apply {
                            putSerializable("RootNodeDeviceInfo", temp[0].parent?.data)
                            temp[0].parent?.isRootNode?.let { putBoolean("isGateway", it) }
                            putString(GlobalData.SelectedNodeMAC, temp[0].data.PhysAddress)
                        }

                        GlobalBus.publish(MainEvent.SwitchToFrag(MeshTopologyFragment().apply {
                            arguments = bundle
                        }))
                    } else {
                        GlobalBus.publish(MainEvent.EnterNetworkTopologyPage())
                    }
                }
            }
            mesh_topology_note_image -> {
                MeshTopologyDeviceStatusDialog(requireActivity()).show()
            }
        }
    }
}