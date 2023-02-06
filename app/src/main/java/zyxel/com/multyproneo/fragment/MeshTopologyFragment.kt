package zyxel.com.multyproneo.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.adapter_mesh_topology_device_center.view.*
import kotlinx.android.synthetic.main.adapter_mesh_topology_device_center.view.mesh_topology_device_image
import kotlinx.android.synthetic.main.adapter_mesh_topology_device_center.view.mesh_topology_device_model
import kotlinx.android.synthetic.main.adapter_mesh_topology_device_center.view.mesh_topology_line_image
import kotlinx.android.synthetic.main.adapter_mesh_topology_device_center.view.mesh_topology_device_role
import kotlinx.android.synthetic.main.adapter_mesh_topology_device_left.view.*
import kotlinx.android.synthetic.main.fragment_mesh_topology.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.runOnUiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.TreeNode
import zyxel.com.multyproneo.adapter.cloud.CloudZYXELEndDeviceItemAdapter
import zyxel.com.multyproneo.api.ApiHandler
import zyxel.com.multyproneo.event.ApiEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import java.util.*
import kotlin.concurrent.schedule


class MeshTopologyFragment : Fragment() {

    private val TAG = "MeshTopologyFragment"
    private lateinit var adapter: CloudZYXELEndDeviceItemAdapter
    private lateinit var getInfoCompleteDisposable: Disposable
    private lateinit var inflator: LayoutInflater
    private lateinit var rootView: View
    private var apiTimer = Timer()
    private val GUIDELINE_ID = 100005
    private val position_left = "Left"
    private val position_center = "Center"
    private val position_right = "right"
    private lateinit var viewLeft: View
    private lateinit var viewCenter: View
    private lateinit var viewRight: View
    private lateinit var viewLeftL3: View
    private lateinit var viewCenterL3: View
    private lateinit var viewRightL3: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mesh_topology, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getInfoCompleteDisposable =
            GlobalBus.listen(ApiEvent.ApiExecuteComplete::class.java).subscribe {
                when (it.event) {
                    ApiHandler.API_RES_EVENT.API_RES_EVENT_HOME_API_REGULAR -> updateUI()
                    else -> {}
                }
            }
        rootView = view
        inflator =
            requireActivity().applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        setClickListener()
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

        if (!getInfoCompleteDisposable.isDisposed) getInfoCompleteDisposable.dispose()
    }


    private fun setClickListener() {
        mesh_topology_back_image.setOnClickListener(clickListener)
        mesh_topology_note_image.setOnClickListener(clickListener)
    }

    private fun updateUI() {
        GlobalData.layer2EndDeviceList.clear()
        GlobalData.layer3EndDeviceList.clear()
        GlobalData.layer4EndDeviceList.clear()

        LogUtil.d(TAG, "updateUI()")
        LogUtil.d(TAG, "endDeviceList size():${GlobalData.ZYXELEndDeviceList.size}")

        //layer 1
        val rootNode = TreeNode(DevicesInfoObject())
        val deviceNode = arrayListOf<TreeNode<DevicesInfoObject>>()
        deviceNode.add(rootNode)

        //layer 2
        for (item in GlobalData.ZYXELEndDeviceList) {
            if (item.X_ZYXEL_Neighbor.toLowerCase() == "gateway") {
                GlobalData.layer2EndDeviceList.add(TreeNode<DevicesInfoObject>(item, rootNode))
            }
        }

        deviceNode.addAll(GlobalData.layer2EndDeviceList)

        //layer 3
        for (item in GlobalData.ZYXELEndDeviceList) {
            for (node in GlobalData.layer2EndDeviceList)
                if (item.X_ZYXEL_Neighbor == node.data.PhysAddress) {
                    GlobalData.layer3EndDeviceList.add(TreeNode<DevicesInfoObject>(item, node))
                }
        }

        deviceNode.addAll(GlobalData.layer3EndDeviceList)

        //layer 4
        for (item in GlobalData.ZYXELEndDeviceList) {
            for (node in GlobalData.layer3EndDeviceList)
                if (item.X_ZYXEL_Neighbor == node.data.PhysAddress) {
                    GlobalData.layer4EndDeviceList.add(TreeNode<DevicesInfoObject>(item, node))
                }
        }

        deviceNode.addAll(GlobalData.layer4EndDeviceList)
        println("deviceNode size:${deviceNode.size}")
        println("layer2Node size:${GlobalData.layer2EndDeviceList.size}")
        println("layer3Node size:${GlobalData.layer3EndDeviceList.size}")
        println("layer4EndDeviceList size:${GlobalData.layer4EndDeviceList.size}")

        runOnUiThread {
            showRoot()
            showLayer2(mesh_topology_l2_area)
        }
    }

    private fun showRoot() {
        val viewCenter: View = inflator.inflate(R.layout.adapter_mesh_topology_device_center, null)
        viewCenter.id = View.generateViewId()
        viewCenter.mesh_topology_line_image.visibility = View.GONE
        setDeviceImage(
            viewCenter,
            GlobalData.getCurrentGatewayInfo().ModelName,
            "",
            "Center",
            GlobalData.getCurrentGatewayInfo().UserDefineName
        )

        mesh_topology_root_area.addView(viewCenter)
        val constraintSet = ConstraintSet().apply {
            clone(mesh_topology_root_area)
            centerHorizontally(viewCenter.id, ConstraintSet.PARENT_ID)
            connect(
                viewCenter.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                dip(5)
            )
        }
        constraintSet.applyTo(mesh_topology_root_area)
    }

    private fun showLayer2(view: ConstraintLayout) {

        viewLeft = inflator.inflate(R.layout.adapter_mesh_topology_device_left, null)
        viewCenter = inflator.inflate(R.layout.adapter_mesh_topology_device_center, null)
        viewRight = inflator.inflate(R.layout.adapter_mesh_topology_device_right, null)
        viewLeftL3 = inflator.inflate(R.layout.adapter_mesh_topology_device_center, null)
        viewCenterL3 = inflator.inflate(R.layout.adapter_mesh_topology_device_center, null)
        viewRightL3 = inflator.inflate(R.layout.adapter_mesh_topology_device_center, null)

        viewLeft.id = View.generateViewId()
        viewCenter.id = View.generateViewId()
        viewRight.id = View.generateViewId()
        view.id = View.generateViewId()
        viewLeftL3.id = View.generateViewId()
        viewCenterL3.id = View.generateViewId()
        viewRightL3.id = View.generateViewId()

        viewLeft.mesh_topology_device_image.setOnClickListener(View.OnClickListener {
            LogUtil.d("dddddddddd", "viewLeft~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        })
        viewCenter.mesh_topology_device_image.setOnClickListener(View.OnClickListener {
            LogUtil.d("dddddddddd", "viewCenter~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        })
        viewRight.mesh_topology_device_image.setOnClickListener(View.OnClickListener {
            LogUtil.d("dddddddddd", "viewRight~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        })
        viewLeftL3.mesh_topology_device_image.setOnClickListener(View.OnClickListener {
            LogUtil.d("dddddddddd", "viewLeftL3~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        })
        viewCenterL3.mesh_topology_device_image.setOnClickListener(View.OnClickListener {
            LogUtil.d("dddddddddd", "viewCenterL3~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        })
        viewRightL3.mesh_topology_device_image.setOnClickListener(View.OnClickListener {
            LogUtil.d("dddddddddd", "viewRightL3~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        })

        viewLeft.mesh_topology_device_image.setOnClickListener(clickListener)
        viewCenter.mesh_topology_device_image.setOnClickListener(clickListener)
        viewRight.mesh_topology_device_image.setOnClickListener(clickListener)
        viewLeftL3.mesh_topology_device_image.setOnClickListener(clickListener)
        viewCenterL3.mesh_topology_device_image.setOnClickListener(clickListener)
        viewRightL3.mesh_topology_device_image.setOnClickListener(clickListener)

        view.addView(viewLeft)
        view.addView(viewCenter)
        view.addView(viewRight)
        view.addView(viewLeftL3)
        view.addView(viewCenterL3)
        view.addView(viewRightL3)

        val constraintSet = ConstraintSet().apply {
            clone(view)
            create(GUIDELINE_ID, ConstraintSet.VERTICAL_GUIDELINE)
            setVisibility(GUIDELINE_ID, ConstraintSet.VISIBLE)
            setGuidelinePercent(GUIDELINE_ID, 0.5f)
            centerHorizontally(viewCenter.id, ConstraintSet.PARENT_ID)

            connect(viewCenter.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
            connect(viewCenter.id, ConstraintSet.END, view.id, ConstraintSet.END)
            connect(viewCenter.id, ConstraintSet.START, view.id, ConstraintSet.START)

            connect(viewLeft.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
            connect(viewLeft.id, ConstraintSet.END, GUIDELINE_ID, ConstraintSet.START)

            connect(viewRight.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
            connect(viewRight.id, ConstraintSet.START, GUIDELINE_ID, ConstraintSet.END)

            connect(viewLeftL3.id, ConstraintSet.TOP, viewLeft.id, ConstraintSet.BOTTOM)
            connect(viewLeftL3.id, ConstraintSet.START, viewLeft.id, ConstraintSet.START)
            connect(viewLeftL3.id, ConstraintSet.END, viewLeft.id, ConstraintSet.END)

            connect(viewCenterL3.id, ConstraintSet.TOP, viewCenter.id, ConstraintSet.BOTTOM)
            connect(viewCenterL3.id, ConstraintSet.START, viewCenter.id, ConstraintSet.START)
            connect(viewCenterL3.id, ConstraintSet.END, viewCenter.id, ConstraintSet.END)

            connect(viewRightL3.id, ConstraintSet.TOP, viewRight.id, ConstraintSet.BOTTOM)
            connect(viewRightL3.id, ConstraintSet.START, viewRight.id, ConstraintSet.START)
            connect(viewRightL3.id, ConstraintSet.END, viewRight.id, ConstraintSet.END)
        }
        constraintSet.applyTo(view)

        setLayer2(
            viewLeft,
            viewCenter,
            viewRight,
            viewLeftL3,
            viewCenterL3,
            viewRightL3,
            GlobalData.layer2EndDeviceList.size
        )


        //開始建構第三層
        LogUtil.d(TAG, "layer2EndDeviceList size:${GlobalData.layer2EndDeviceList.size}")

        for (item in GlobalData.layer2EndDeviceList) {
            LogUtil.d(TAG, "hostname:${item.data.HostName}")
            LogUtil.d(TAG, "item children size:${item.getChildren().size}")
            LogUtil.d(TAG, "--------------------------------")
        }
    }

    private fun setLayer2(
        viewLeft: View,
        viewCenter: View,
        viewRight: View,
        viewLeftL3: View,
        viewCenterL3: View,
        viewRightL3: View,
        count: Int
    ) {

        when (count) {
            0 -> {
                //hide layer2,layer3
                viewLeft.visibility = View.GONE
                viewCenter.visibility = View.GONE
                viewRight.visibility = View.GONE
                viewLeftL3.visibility = View.GONE
                viewCenterL3.visibility = View.GONE
                viewRightL3.visibility = View.GONE
            }
            1 -> {
                viewLeft.visibility = View.GONE
                viewCenter.visibility = View.VISIBLE
                viewRight.visibility = View.GONE
                viewLeftL3.visibility = View.GONE
                viewCenterL3.visibility = View.VISIBLE
                viewRightL3.visibility = View.GONE
                for ((count, item) in GlobalData.layer2EndDeviceList.withIndex()) {
                    when (count) {
                        0 -> {
                            setDeviceImage(
                                viewCenter,
                                GlobalData.WX3100_T0,
                                item.data.X_ZYXEL_RSSI_STAT,
                                position_center,
                                item.data.HostName
                            )

                            setL3View(
                                viewCenterL3,
                                item.getChildren().size,
                                item,
                                position_center
                            )

                        }
                        else -> {}
                    }
                }


            }
            2 -> {
                viewLeft.visibility = View.VISIBLE
                viewCenter.visibility = View.GONE
                viewRight.visibility = View.VISIBLE
                viewLeftL3.visibility = View.VISIBLE
                viewCenterL3.visibility = View.GONE
                viewRightL3.visibility = View.VISIBLE
                for ((count, item) in GlobalData.layer2EndDeviceList.withIndex()) {
                    when (count) {
                        0 -> {
                            setDeviceImage(
                                viewLeft,
                                GlobalData.WX3100_T0,
                                item.data.X_ZYXEL_RSSI_STAT,
                                position_left,
                                item.data.HostName
                            )

                            setL3View(
                                viewLeftL3,
                                item.getChildren().size,
                                item,
                                position_center
                            )
                        }

                        1 -> {
                            setDeviceImage(
                                viewRight,
                                GlobalData.WX3100_T0,
                                item.data.X_ZYXEL_RSSI_STAT,
                                position_right,
                                item.data.HostName
                            )

                            setL3View(
                                viewRightL3,
                                item.getChildren().size,
                                item,
                                position_center
                            )
                        }
                        else -> {}
                    }
                }
            }
            3 -> {
                viewLeft.visibility = View.VISIBLE
                viewCenter.visibility = View.VISIBLE
                viewRight.visibility = View.VISIBLE
                viewLeftL3.visibility = View.VISIBLE
                viewCenterL3.visibility = View.VISIBLE
                viewRightL3.visibility = View.VISIBLE
                for ((count, item) in GlobalData.layer2EndDeviceList.withIndex()) {
                    when (count) {
                        0 -> {
                            setDeviceImage(
                                viewLeft,
                                GlobalData.WX3100_T0,
                                item.data.X_ZYXEL_RSSI_STAT,
                                position_left,
                                item.data.UserDefineName
                            )
                            setL3View(
                                viewLeftL3,
                                item.getChildren().size,
                                item,
                                position_center
                            )
                        }

                        1 -> {
                            setDeviceImage(
                                viewCenter,
                                GlobalData.WX3100_T0,
                                item.data.X_ZYXEL_RSSI_STAT,
                                position_center,
                                item.data.UserDefineName
                            )
                            setL3View(
                                viewCenterL3,
                                item.getChildren().size,
                                item,
                                position_center
                            )
                        }

                        2 -> {
                            setDeviceImage(
                                viewRight,
                                GlobalData.WX3100_T0,
                                item.data.X_ZYXEL_RSSI_STAT,
                                position_right,
                                item.data.UserDefineName
                            )

                            setL3View(
                                viewRightL3,
                                item.getChildren().size,
                                item,
                                position_center
                            )
                        }

                        else -> {}
                    }
                }
            }
            else -> {}
        }
    }

    private fun setL3View(
        viewL3: View,
        size: Int,
        data: TreeNode<DevicesInfoObject>,
        position: String
    ) {
        if (size == 0) {
            viewL3.visibility = View.GONE
        } else if (size == 1) {
            when (position) {
                position_left -> viewL3.mesh_topology_line_image.imageResource =
                    R.drawable.left_wired_weak
                position_center -> viewL3.mesh_topology_line_image.imageResource =
                    R.drawable.straight_wired_good
                position_right -> viewL3.mesh_topology_line_image.imageResource =
                    R.drawable.right_wired_good
                else -> {}
            }
            viewL3.mesh_topology_device_model.text = data.getChildren()[0].data.HostName
        } else if (size > 1) {
            viewL3.mesh_topology_line_image.imageResource = R.drawable.straight_double
            viewL3.mesh_topology_device_model.text = data.getChildren()[0].data.HostName
        }
        viewL3.mesh_topology_device_role.text = "Satellite"
    }


    private fun setDeviceImage(
        view: View,
        modelName: String,
        rssi: String,
        position: String,
        name: String
    ) {
        println("modelName:$modelName")
        view.mesh_topology_line_image.imageResource = checkConnection(rssi, position)
        view.mesh_topology_device_model.text = name

        when (modelName) {
            GlobalData.AX7501_B1 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex5601_t0
//                view.mesh_topology_device_model.text = GlobalData.AX7501_B1
            }
            GlobalData.DX3300_T1 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex3300_t1
//                view.mesh_topology_device_model.text = GlobalData.DX3300_T1
            }
            GlobalData.DX3301_T0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex3301_t0
//                view.mesh_topology_device_model.text = GlobalData.DX3301_T0
            }
            GlobalData.EMG3525_T50B -> {
                view.mesh_topology_device_image.imageResource = R.drawable.emg3525_t50b
//                view.mesh_topology_device_model.text = GlobalData.EMG3525_T50B
            }
            GlobalData.EMG3525_T50C -> {
                view.mesh_topology_device_image.imageResource = R.drawable.emg3525_t50c
//                view.mesh_topology_device_model.text = GlobalData.EMG3525_T50C
            }
            GlobalData.EMG5523_T50B -> {
                view.mesh_topology_device_image.imageResource = R.drawable.emg5523_t50b
//                view.mesh_topology_device_model.text = GlobalData.EMG5523_T50B
            }
            GlobalData.EMG5723_T50K -> {
                view.mesh_topology_device_image.imageResource = R.drawable.emg5723_t50k
//                view.mesh_topology_device_model.text = GlobalData.EMG5723_T50K
            }

            GlobalData.EX3200_T0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex3200_t0
//                view.mesh_topology_device_model.text = GlobalData.EX3200_T0
            }
            GlobalData.EX3300_T1 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex3300_t1
//                view.mesh_topology_device_model.text = GlobalData.EX3300_T1
            }
            GlobalData.EX3301_T0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex3301_t0
//                view.mesh_topology_device_model.text = GlobalData.EX3301_T0
            }
            GlobalData.EX5510_B0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex5510_b0
//                view.mesh_topology_device_model.text = GlobalData.EX5510_B0
            }
            GlobalData.EX5600_T0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex5600_t0
//                view.mesh_topology_device_model.text = GlobalData.EX5600_T0
            }
            GlobalData.EX5601_T0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex5601_t0
//                view.mesh_topology_device_model.text = GlobalData.EX5601_T0
            }
            GlobalData.EX7501_B0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex7501_b0
//                view.mesh_topology_device_model.text = GlobalData.EX7501_B0
            }
            GlobalData.PX5111_T0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.px5111_t0
//                view.mesh_topology_device_model.text = GlobalData.PX5111_T0
            }
            GlobalData.PX5501_B1 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.px5501_b1
//                view.mesh_topology_device_model.text = GlobalData.PX5501_B1
            }

            GlobalData.VMG3625_T50B -> {
                view.mesh_topology_device_image.imageResource = R.drawable.vmg3625_t50b
//                view.mesh_topology_device_model.text = GlobalData.VMG3625_T50B
            }
            GlobalData.VMG3625_T50C -> {
                view.mesh_topology_device_image.imageResource = R.drawable.vmg3625_t50c
//                view.mesh_topology_device_model.text = GlobalData.VMG3625_T50C
            }
            GlobalData.VMG3927_T50K -> {
                view.mesh_topology_device_image.imageResource = R.drawable.vmg3927_t50k
//                view.mesh_topology_device_model.text = GlobalData.VMG3927_T50K
            }
            GlobalData.VMG8623_T50B -> {
                view.mesh_topology_device_image.imageResource = R.drawable.vmg8623_t50b
//                view.mesh_topology_device_model.text = GlobalData.VMG8623_T50B
            }
            GlobalData.VMG8825_T50K -> {
                view.mesh_topology_device_image.imageResource = R.drawable.vmg8825_t50k
//                view.mesh_topology_device_model.text = GlobalData.VMG8825_T50K
            }

            GlobalData.WAP6807 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.vmg3625_t50b
//                view.mesh_topology_device_model.text = GlobalData.WAP6807
            }
            GlobalData.WX3100_T0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.wx3100_t0
//                view.mesh_topology_device_model.text = GlobalData.WX3100_T0
            }
            GlobalData.WX5600_T0 -> {
                view.mesh_topology_device_image.imageResource = R.drawable.wx5600_t0
//                view.mesh_topology_device_model.text = GlobalData.WX5600_T0
            }

            else -> {
                view.mesh_topology_device_image.imageResource = R.drawable.ex5601_t0
//                view.mesh_topology_device_model.text = GlobalData.EX5601_T0
            }
        }
    }

    private fun checkConnection(xZyxelRssiStat: String, position: String): Int {
        when (xZyxelRssiStat) {
            "Good" -> {
                return when (position) {
                    position_left -> R.drawable.left_wired_good
                    position_center -> R.drawable.straight_wired_good
                    position_right -> R.drawable.right_wired_good
                    else -> R.drawable.straight_wired_good
                }
            }
            "Weak" -> {
                return when (position) {
                    position_left -> R.drawable.left_wired_weak
                    position_center -> R.drawable.straight_wired_weak
                    position_right -> R.drawable.right_wired_weak
                    else -> R.drawable.straight_wired_weak
                }
            }
            "Close" -> {
                return when (position) {
                    position_left -> R.drawable.left_wired_close
                    position_center -> R.drawable.straight_wired_close
                    position_right -> R.drawable.right_wired_close
                    else -> R.drawable.straight_wired_close
                }
            }
            "No" -> {
                return when (position) {
                    position_left -> R.drawable.left_wired_no
                    position_center -> R.drawable.straight_wired_no
                    position_right -> R.drawable.right_wired_no
                    else -> R.drawable.straight_wired_no
                }
            }
            else -> {
                return R.drawable.straight_wired_no
            }
        }
    }

    private val clickListener = View.OnClickListener { view ->

        LogUtil.d(TAG, "clickListener:$view")
        LogUtil.d(TAG, "clickListener id:${view.id}")

        when (view) {
            mesh_topology_back_image -> GlobalBus.publish(MainEvent.SwitchToFrag(HomeFragment()))

            mesh_topology_note_image -> GlobalBus.publish(MainEvent.SwitchToFrag(HomeFragment()))

            viewLeft -> {LogUtil.d(TAG, "viewLeft")}
            viewCenter -> {LogUtil.d(TAG, "viewCenter")}
            viewRight -> {LogUtil.d(TAG, "viewRight")}
            viewLeftL3 -> {LogUtil.d(TAG, "viewLeftL3")}
            viewCenterL3 -> {LogUtil.d(TAG, "viewCenterL3")}
            viewRightL3 -> {LogUtil.d(TAG, "viewRightL3")}

        }
    }
}