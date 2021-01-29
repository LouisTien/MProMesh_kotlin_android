package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_cloud_add_mesh_wps.*
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.P2PAddMeshApi
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudAddMeshWPSFragment : Fragment()
{
    private val TAG = "CloudAddMeshWPSFragment"
    private var fromAddMeshFailPage = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_add_mesh_wps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        with(arguments)
        {
            this?.getBoolean("fromAddMeshFailPage", false)?.let{ fromAddMeshFailPage = it }
        }

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
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
            cloud_mesh_wps_back_image ->
            {
                if(fromAddMeshFailPage)
                    GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshFailFragment()))
                else
                    GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshFragment()))
            }

            cloud_mesh_wps_pair_button ->
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
        }
    }

    private fun setClickListener()
    {
        cloud_mesh_wps_back_image.setOnClickListener(clickListener)
        cloud_mesh_wps_pair_button.setOnClickListener(clickListener)
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