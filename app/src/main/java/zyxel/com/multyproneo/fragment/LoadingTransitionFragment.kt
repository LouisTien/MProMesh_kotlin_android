package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_loading_transition.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.LoadingTransitionEvent
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/6/5.
 */
class LoadingTransitionFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private var title = ""
    private var description = ""
    private var secDescription = ""
    private var loadingSecond = 0
    private var showCountDownTimer = false
    private var anim = AppConfig.LoadingAnimation.ANIM_REBOOT
    private var desPage = AppConfig.LoadingGoToPage.FRAG_SEARCH
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var WPSStatusUpdateDisposable: Disposable

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
            this?.getString("Sec_Description")?.let{ secDescription = it }
            this?.getInt("LoadingSecond")?.let{ loadingSecond = it }
            this?.getSerializable("Anim")?.let{ anim = it as AppConfig.LoadingAnimation }
            this?.getSerializable("DesPage")?.let{ desPage = it as AppConfig.LoadingGoToPage }
            this?.getBoolean("ShowCountDownTimer")?.let{ showCountDownTimer = it }
        }

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

        WPSStatusUpdateDisposable = GlobalBus.listen(LoadingTransitionEvent.WPSStatusUpdate::class.java).subscribe{
            countDownTimer.cancel()
            when(it.status)
            {
                true -> desPage = AppConfig.LoadingGoToPage.FRAG_MESH_SUCCESS
                false -> desPage = AppConfig.LoadingGoToPage.FRAG_MESH_FAIL
            }
            finishAction()
        }

        initUI()
        loading_animation_view.playAnimation()
        //Handler().postDelayed({ finishAction() }, (loadingSecond * 1000).toLong())
        countDownTimer.start()
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
        countDownTimer.cancel()
        if(!WPSStatusUpdateDisposable.isDisposed) WPSStatusUpdateDisposable.dispose()
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

        if(secDescription != "")
        {
            loading_msg_working_text.text = secDescription
            loading_msg_working_text.visibility = View.VISIBLE
        }
        else
            loading_msg_working_text.visibility = View.INVISIBLE

        loading_countdown_time_text.visibility = if(showCountDownTimer) View.VISIBLE else View.GONE

        //loading_animation_view.enableMergePathsForKitKatAndAbove(true)
        when(anim)
        {
            AppConfig.LoadingAnimation.ANIM_REBOOT -> loading_animation_view.setAnimation("rebooting.json")
            AppConfig.LoadingAnimation.ANIM_SEARCH -> loading_animation_view.setAnimation("searching.json")
            AppConfig.LoadingAnimation.ANIM_NOFOUND -> loading_animation_view.setAnimation("nofound.json")
        }
    }

    private fun finishAction()
    {
        GlobalBus.publish(MainEvent.StopGetWPSStatusTask())

        when(desPage)
        {
            AppConfig.LoadingGoToPage.FRAG_SEARCH -> GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
            AppConfig.LoadingGoToPage.FRAG_HOME -> GlobalBus.publish(MainEvent.EnterHomePage())
            AppConfig.LoadingGoToPage.FRAG_MESH_SUCCESS -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshSuccessFragment()))
            AppConfig.LoadingGoToPage.FRAG_MESH_FAIL -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshFailFragment()))
        }
    }
}