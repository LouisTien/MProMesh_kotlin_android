package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_add_mesh_tips.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/6/28.
 */
class AddMeshTipsFragment : Fragment()
{
    private val TAG = "AddMeshTipsFragment"
    private var fromFrag = "AddMeshExtender"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_add_mesh_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        with(arguments){ this?.getString("FromFrag")?.let{ fromFrag = it } }

        mesh_tip_cancel_image.setOnClickListener{
            when(fromFrag)
            {
                "AddMeshExtender" -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshExtenderFragment()))
                "AddMeshCableInfo" -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshCableInfoFragment()))
            }
        }
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
}