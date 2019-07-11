package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add_mesh_fail.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/7/1.
 */
class AddMeshFailFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_add_mesh_fail, container, false)
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
            mesh_fail_close_image -> GlobalBus.publish(MainEvent.EnterHomePage())

            mesh_fail_try_again_button ->
            {
                val bundle = Bundle().apply{
                    putString("Title", "")
                    putString("Description", getString(R.string.loading_transition_please_wait))
                    putString("Sec_Description", getString(R.string.loading_transition_extender))
                    putInt("LoadingSecond", AppConfig.addExtenderTime)
                    putSerializable("Anim", AppConfig.LoadingAnimation.ANIM_REBOOT)
                    putSerializable("DesPage", AppConfig.LoadingGoToPage.FRAG_MESH_SUCCESS)
                    putBoolean("ShowCountDownTimer", true)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionFragment().apply{ arguments = bundle }))
            }

            mesh_fail_help_text -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshHelpFragment()))
        }
    }

    private fun setClickListener()
    {
        mesh_fail_close_image.setOnClickListener(clickListener)
        mesh_fail_try_again_button.setOnClickListener(clickListener)
        mesh_fail_help_text.setOnClickListener(clickListener)
    }
}