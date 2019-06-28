package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add_mesh.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent

/**
 * Created by LouisTien on 2019/6/27.
 */
class AddMeshFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_add_mesh, container, false)
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
            mesh_back_image -> GlobalBus.publish(MainEvent.EnterHomePage())

            mesh_wireless_enter_image -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshExtenderFragment()))

            mesh_wire_enter_image -> {}
        }
    }

    private fun setClickListener()
    {
        mesh_back_image.setOnClickListener(clickListener)
        mesh_wireless_enter_image.setOnClickListener(clickListener)
        mesh_wire_enter_image.setOnClickListener(clickListener)
    }
}