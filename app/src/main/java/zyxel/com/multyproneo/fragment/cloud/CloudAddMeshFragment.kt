package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_cloud_add_mesh.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent

class CloudAddMeshFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_add_mesh, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
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
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            cloud_mesh_back_image -> GlobalBus.publish(MainEvent.EnterCloudHomePage())

            cloud_mesh_wireless_area_frame -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshExtenderFragment()))

            cloud_mesh_wire_area_frame -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshCableInfoFragment()))
        }
    }

    private fun setClickListener()
    {
        cloud_mesh_back_image.setOnClickListener(clickListener)
        cloud_mesh_wireless_area_frame.setOnClickListener(clickListener)
        cloud_mesh_wire_area_frame.setOnClickListener(clickListener)
    }
}