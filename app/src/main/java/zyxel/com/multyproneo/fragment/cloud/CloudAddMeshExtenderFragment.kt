package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add_mesh_extender.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent

class CloudAddMeshExtenderFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_add_mesh_extender, container, false)
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
            mesh_extender_back_image -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshFragment()))

            mesh_extender_tip_text -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshTipsFragment().apply{ arguments = Bundle().apply{ putString("FromFrag", "CloudAddMeshExtender") } }))

            mesh_extender_next_button -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshWPSFragment()))
        }
    }

    private fun setClickListener()
    {
        mesh_extender_back_image.setOnClickListener(clickListener)
        mesh_extender_tip_text.setOnClickListener(clickListener)
        mesh_extender_next_button.setOnClickListener(clickListener)
    }
}