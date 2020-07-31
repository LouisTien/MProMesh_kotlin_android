package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_cloud_add_mesh_wifi_tips.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent

class CloudAddMeshWiFiTipsFragment : Fragment()
{
    private var FOLENable = false
    private var CLQEnable = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_add_mesh_wifi_tips, container, false)
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

    private val clickListener = View.OnClickListener { view ->
        when(view)
        {
            cloud_mesh_wifi_tip_cancel_image -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshExtenderFragment()))

            cloud_mesh_wifi_tip_FOL_title_relative ->
            {
                FOLENable = !FOLENable
                updateFOL()
            }

            cloud_mesh_wifi_tip_CLQ_title_relative ->
            {
                CLQEnable = !CLQEnable
                updateCLQ()
            }
        }
    }

    private fun setClickListener()
    {
        cloud_mesh_wifi_tip_cancel_image.setOnClickListener(clickListener)
        cloud_mesh_wifi_tip_FOL_title_relative.setOnClickListener(clickListener)
        cloud_mesh_wifi_tip_CLQ_title_relative.setOnClickListener(clickListener)
    }

    private fun updateFOL()
    {
        if(FOLENable)
        {
            FOL_area_status(true)
            CLQ_area_status(false)
        }
        else
            FOL_area_status(false)
    }

    private fun updateCLQ()
    {
        if(CLQEnable)
        {
            CLQ_area_status(true)
            FOL_area_status(false)
        }
        else
            CLQ_area_status(false)
    }

    private fun FOL_area_status(show: Boolean)
    {
        cloud_mesh_wifi_tip_FOL_image.setImageResource(if(show) R.drawable.icon_expand_close else R.drawable.icon_expand_open)
        cloud_mesh_wifi_tip_FOL_content_linear.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun CLQ_area_status(show: Boolean)
    {
        cloud_mesh_wifi_tip_CLQ_image.setImageResource(if(show) R.drawable.icon_expand_close else R.drawable.icon_expand_open)
        cloud_mesh_wifi_tip_CLQ_content_linear.visibility = if(show) View.VISIBLE else View.GONE
    }
}