package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_cloud_add_mesh_apply_setting.*
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.P2PAddMeshApi
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudAddMeshApplySettings : Fragment()
{
    private val TAG = "CloudAddMeshApplySettings"
    private lateinit var countDownTimerFinish: CountDownTimer
    private lateinit var countDownTimerGetL2DeviceNum: CountDownTimer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_add_mesh_apply_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        countDownTimerFinish = object : CountDownTimer((AppConfig.addMeshTime * 1000).toLong(), 1000)
        {
            override fun onTick(millisUntilFinished: Long)
            {
                val min = (millisUntilFinished / 1000) / 60
                val sec = (millisUntilFinished / 1000) % 60
                val minStr = if(min < 10) "0" + min.toString() else min.toString()
                val secStr = if(sec < 10) "0" + sec.toString() else sec.toString()
                cloud_loading_countdown_time_text.text = "$minStr:$secStr"
            }

            override fun onFinish() = finishAction()
        }

        countDownTimerGetL2DeviceNum = object : CountDownTimer((AppConfig.WPSSCheckL2DeviceUpdateTime * 1000).toLong(), 1000)
        {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() = getL2DeviceNumberTask()
        }

        cloud_loading_animation_view.setAnimation("ApplyingWiFiSettings_MeshDevice_oldJson.json")
        cloud_loading_animation_view.playAnimation()

        countDownTimerFinish.start()
        countDownTimerGetL2DeviceNum.start()
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
        stopAllTimer()
    }

    private fun finishAction()
    {
        stopAllTimer()
        GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshSuccessFragment()))
    }

    private fun getL2DeviceNumberTask()
    {
        P2PAddMeshApi.GetL2DeviceNumber()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val number = data.getJSONObject("Object").getInt("X_ZYXEL_HostNumberOfL2Devices")
                            LogUtil.d(TAG,"L2Device Number:$number")
                            LogUtil.d(TAG,"Original L2Device Number:${GlobalData.L2DeviceNumber}")

                            if(number > GlobalData.L2DeviceNumber)
                                finishAction()
                            else
                                countDownTimerGetL2DeviceNum.start()
                        }
                        catch(e: Exception)
                        {
                            e.printStackTrace()
                            stopAllTimer()
                        }
                    }
                }).execute()
    }

    private fun stopAllTimer()
    {
        countDownTimerFinish.cancel()
        countDownTimerGetL2DeviceNum.cancel()
    }
}