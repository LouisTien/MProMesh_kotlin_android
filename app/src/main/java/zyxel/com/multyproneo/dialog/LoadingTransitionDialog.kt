package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.dialog_loading_transition.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.AddMeshFailFragment
import zyxel.com.multyproneo.fragment.AddMeshSuccessFragment
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/7/30.
 */
class LoadingTransitionDialog
(
        context: Context,
        private val title: String = "",
        private val description: String = "",
        private val secDescription: String = "",
        private val loadingSecond: Int = 0,
        private val anim: AppConfig.LoadingAnimation = AppConfig.LoadingAnimation.ANIM_REBOOT,
        private val desPage: AppConfig.LoadingGoToPage = AppConfig.LoadingGoToPage.FRAG_SEARCH,
        private val showCountDownTimer: Boolean = false
) : Dialog(context, R.style.full_screen_dialog)
{
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading_transition)
        setCancelable(true)
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        initUI()
    }

    override fun show()
    {
        super.show()
    }

    override fun dismiss()
    {
        super.dismiss()
    }

    private fun initUI()
    {
        countDownTimer = object : CountDownTimer((loadingSecond * 1000).toLong(), 1000)
        {
            override fun onTick(millisUntilFinished: Long)
            {
                val min = (millisUntilFinished / 1000) / 60
                val sec = (millisUntilFinished / 1000) % 60
                val minStr = if(min < 10) "0" + min.toString() else min.toString()
                val secStr = if(sec < 10) "0" + sec.toString() else sec.toString()
                loading_countdown_time_text.text = "$minStr:$secStr"
            }

            override fun onFinish() = finishAction()
        }

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

        if(secDescription != "")
        {
            loading_msg_working_text.text = secDescription
            loading_msg_working_text.visibility = View.VISIBLE
        }
        else
            loading_msg_working_text.visibility = View.INVISIBLE

        loading_countdown_time_text.visibility = if(showCountDownTimer) View.VISIBLE else View.GONE

        when(anim)
        {
            AppConfig.LoadingAnimation.ANIM_REBOOT -> loading_animation_view.setAnimation("rebooting.json")
            AppConfig.LoadingAnimation.ANIM_SEARCH -> loading_animation_view.setAnimation("searching.json")
            AppConfig.LoadingAnimation.ANIM_NOFOUND -> loading_animation_view.setAnimation("nofound.json")
        }

        loading_animation_view.playAnimation()

        countDownTimer.start()
    }

    private fun finishAction()
    {
        dismiss()
        when(desPage)
        {
            AppConfig.LoadingGoToPage.FRAG_SEARCH -> GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
            AppConfig.LoadingGoToPage.FRAG_HOME -> GlobalBus.publish(MainEvent.EnterHomePage())
            AppConfig.LoadingGoToPage.FRAG_MESH_SUCCESS -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshSuccessFragment()))
            AppConfig.LoadingGoToPage.FRAG_MESH_FAIL -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshFailFragment()))
        }
    }
}