package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_setup_controller_ready.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.dialog.SetupControllerReadyHelpDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

class SetupControllerReadyFragment : Fragment()
{
    private lateinit var helpDlg: SetupControllerReadyHelpDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_controller_ready, container, false)
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
            setup_controller_ready_help_image ->
            {
                helpDlg = SetupControllerReadyHelpDialog(activity!!)
                helpDlg.show()
            }

            setup_controller_ready_next_image -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectControllerFragment()))
        }
    }

    private fun setClickListener()
    {
        setup_controller_ready_help_image.setOnClickListener(clickListener)
        setup_controller_ready_next_image.setOnClickListener(clickListener)
    }
}