package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_setup_controller_ready.*
import org.jetbrains.anko.doAsync
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.SetupControllerReadyHelpAdapter
import zyxel.com.multyproneo.api.cloud.*
import zyxel.com.multyproneo.dialog.SetupControllerReadyHelpDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent

class SetupControllerReadyFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var helpDlg: SetupControllerReadyHelpDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_controller_ready, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val helpAdapter = SetupControllerReadyHelpAdapter(activity!!)
        setup_controller_ready_help_pager.adapter = helpAdapter
        setup_controller_ready_help_pager.offscreenPageLimit = 3
        setup_controller_ready_help_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int)
            {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
            {
            }

            override fun onPageSelected(position: Int)
            {
            }

        })
        setup_controller_ready_help_pager_indicator.attachTo(setup_controller_ready_help_pager)

        setClickListener()

        GlobalBus.publish(MainEvent.ShowLoading())
        doAsync{
            TUTKP2PBaseApi.stopSession()
            GlobalBus.publish(MainEvent.HideLoading())
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