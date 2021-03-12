package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.GlobalData
import kotlinx.android.synthetic.main.fragment_loading_transition_progress.*
import zyxel.com.multyproneo.fragment.cloud.CloudAddMeshApplySettings
import zyxel.com.multyproneo.fragment.cloud.CloudAddMeshFailFragment
import zyxel.com.multyproneo.fragment.cloud.CloudWelcomeFragment
import zyxel.com.multyproneo.util.AppConfig

class LoadingTransitionProgressFragment : Fragment() {
    private val TAG = "LoadingTransitionProgressFragment"
    private var title = ""
    private var desPage = AppConfig.LoadingGoToPage.FRAG_SEARCH
    private var loadingSecond = 0
    private var isCloud = false
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loading_transition_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        with(arguments)
        {
            this?.getString("Title")?.let{ title = it }
            this?.getInt("LoadingSecond")?.let{ loadingSecond = it }
            this?.getSerializable("DesPage")?.let{ desPage = it as AppConfig.LoadingGoToPage }
            this?.getBoolean("IsCloud")?.let{ isCloud = it }
        }

        initUI()
        countDownTimer = object : CountDownTimer((loadingSecond * 1000).toLong(), 1000)
        {
            override fun onTick(millisUntilFinished: Long)
            {

            }

            override fun onFinish() = finishAction()
        }.start()
    }

    override fun onResume() {
        super.onResume()

        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer.cancel()
    }

    private fun initUI() {
        loading_msg_title_text.text = title
    }

    private fun finishAction() {
        if(isCloud) {
            when(desPage)
            {
                AppConfig.LoadingGoToPage.FRAG_SEARCH -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudWelcomeFragment()))
                AppConfig.LoadingGoToPage.FRAG_HOME -> GlobalBus.publish(MainEvent.EnterCloudHomePage())
                AppConfig.LoadingGoToPage.FRAG_MESH_SUCCESS -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshApplySettings()))
                AppConfig.LoadingGoToPage.FRAG_MESH_FAIL -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshFailFragment()))
            }
        }
        else {
            when(desPage)
            {
                AppConfig.LoadingGoToPage.FRAG_SEARCH -> GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
                AppConfig.LoadingGoToPage.FRAG_HOME -> GlobalBus.publish(MainEvent.EnterHomePage())
                AppConfig.LoadingGoToPage.FRAG_MESH_SUCCESS -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshSuccessFragment()))
                AppConfig.LoadingGoToPage.FRAG_MESH_FAIL -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshFailFragment()))
            }
        }
    }
}