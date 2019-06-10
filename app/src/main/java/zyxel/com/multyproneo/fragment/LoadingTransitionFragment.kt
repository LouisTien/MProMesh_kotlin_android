package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_loading_transition.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/6/5.
 */
class LoadingTransitionFragment : Fragment()
{
    private var title = ""
    private var description = ""
    private var sec_description = ""
    private var loadingSecond = 0
    private var showRetry = false
    private var anim = AppConfig.Companion.LoadingAnimation.ANIM_REBOOT
    private var desPage = AppConfig.Companion.LoadingGoToPage.FRAG_SEARCH

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_loading_transition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getString("Title")?.let{ title = it }
            this?.getString("Description")?.let{ description = it }
            this?.getString("Sec_Description")?.let{ sec_description = it }
            this?.getInt("LoadingSecond")?.let{ loadingSecond = it }
            this?.getSerializable("Anim")?.let{ anim = it as AppConfig.Companion.LoadingAnimation }
            this?.getSerializable("DesPage")?.let{ desPage = it as AppConfig.Companion.LoadingGoToPage }
            this?.getBoolean("ShowRetry")?.let{ showRetry = it }
        }

        initUI()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
        loading_animation_view.playAnimation()
        Handler().postDelayed({ finishAction() }, (loadingSecond * 1000).toLong())
    }

    private fun initUI()
    {
        if(title != "")
        {
            loading_msg_title_text.text = title
            loading_msg_title_text.visibility = View.VISIBLE
        }
        else
            loading_msg_title_text.visibility = View.INVISIBLE

        if(description != "")
        {
            loading_msg_status_text.text = description
            loading_msg_status_text.visibility = View.VISIBLE
        }
        else
            loading_msg_status_text.visibility = View.INVISIBLE

        if(sec_description != "")
        {
            loading_msg_working_text.text = sec_description
            loading_msg_working_text.visibility = View.VISIBLE
        }
        else
            loading_msg_working_text.visibility = View.INVISIBLE

        loading_retry_image.visibility = if(showRetry) View.VISIBLE else View.INVISIBLE

        when(anim)
        {
            AppConfig.Companion.LoadingAnimation.ANIM_REBOOT -> loading_animation_view.setAnimation("rebooting.json")
            AppConfig.Companion.LoadingAnimation.ANIM_SEARCH -> loading_animation_view.setAnimation("searching.json")
            AppConfig.Companion.LoadingAnimation.ANIM_NOFOUND -> loading_animation_view.setAnimation("nofound.json")
            else -> loading_animation_view.setAnimation("rebooting.json")
        }
    }

    private fun finishAction()
    {
        when(desPage)
        {
            AppConfig.Companion.LoadingGoToPage.FRAG_SEARCH -> GlobalBus.publish(MainEvent.SwitchToFrag(FindingDeviceFragment()))
            AppConfig.Companion.LoadingGoToPage.FRAG_HOME -> GlobalBus.publish(MainEvent.SwitchToFrag(HomeFragment()))
        }
    }
}