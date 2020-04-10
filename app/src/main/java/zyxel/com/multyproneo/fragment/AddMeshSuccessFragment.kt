package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_add_mesh_success.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent

/**
 * Created by LouisTien on 2019/7/1.
 */
class AddMeshSuccessFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_add_mesh_success, container, false)
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
            mesh_success_close_image -> GlobalBus.publish(MainEvent.EnterHomePage())

            mesh_success_done_button -> GlobalBus.publish(MainEvent.EnterHomePage())
        }
    }

    private fun setClickListener()
    {
        mesh_success_close_image.setOnClickListener(clickListener)
        mesh_success_done_button.setOnClickListener(clickListener)
    }
}