package zyxel.com.multyproneo.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
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
import zyxel.com.multyproneo.adapter.MeshTopologyPageAdapter
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mesh_topology, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var pageAdapter = MeshTopologyPageAdapter(this)
        mesh_topology_viewpager.adapter = pageAdapter
        TabLayoutMediator(into_tab_layout, mesh_topology_viewpager)
        { tab, position -> }.attach()
        setClickListener()
    }

    override fun onResume() {
        super.onResume()
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
            mesh_topology_back_image -> GlobalBus.publish(MainEvent.SwitchToFrag(HomeFragment()))

            mesh_topology_note_image -> GlobalBus.publish(MainEvent.SwitchToFrag(HomeFragment()))
        }
    }
}