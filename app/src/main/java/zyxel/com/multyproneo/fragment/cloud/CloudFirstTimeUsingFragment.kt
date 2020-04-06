package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_setup_first_time_using.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.SharedPreferencesUtil

class CloudFirstTimeUsingFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_first_time_using, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setup_first_time_start_image.onClick{
            var firstTimeUse by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_SETUP_FIRST_TIME_KEY, true)
            firstTimeUse = false
            GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))
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