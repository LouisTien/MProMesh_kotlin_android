package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_cloud_add_mesh_fail.*
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.P2PAddMeshApi
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudAddMeshFailFragment : Fragment()
{
    private val TAG = javaClass.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_add_mesh_fail, container, false)
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
            cloud_mesh_fail_quit_text -> GlobalBus.publish(MainEvent.EnterCloudHomePage())

            cloud_mesh_fail_retry_image ->
            {
                getL2DeviceNumberTask()

                val bundle = Bundle().apply{
                    putString("Title", getString(R.string.cloud_loading_transition_extender))
                    putString("Description", getString(R.string.loading_transition_please_wait))
                    putString("Sec_Description", "")
                    putInt("LoadingSecond", AppConfig.addMeshTime)
                    putSerializable("Anim", AppConfig.LoadingAnimation.ANIM_REBOOT)
                    putSerializable("DesPage", AppConfig.LoadingGoToPage.FRAG_MESH_FAIL)
                    putBoolean("ShowCountDownTimer", true)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudLoadingTransitionFragment().apply{ arguments = bundle }))
            }

            cloud_mesh_fail_help_text -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshHelpFragment()))
        }
    }

    private fun setClickListener()
    {
        cloud_mesh_fail_quit_text.setOnClickListener(clickListener)
        cloud_mesh_fail_retry_image.setOnClickListener(clickListener)
        cloud_mesh_fail_help_text.setOnClickListener(clickListener)
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
                            GlobalData.L2DeviceNumber = data.getJSONObject("Object").getInt("X_ZYXEL_HostNumberOfL2Devices")
                            LogUtil.d(TAG,"L2Device Number:${GlobalData.L2DeviceNumber}")
                            startPairingTask()
                        }
                        catch(e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    private fun startPairingTask()
    {
        val params = ",\"X_ZyXEL_TriggerWPSPushButton\":true"

        P2PAddMeshApi.StartPairing()
                .setRequestPageName(TAG)
                .setRequestPayload(params)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            GlobalBus.publish(MainEvent.StartCloudGetWPSStatusTask())
                        }
                        catch(e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }
}