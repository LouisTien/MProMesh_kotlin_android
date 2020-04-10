package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_add_mesh_fail.*
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.AddMeshApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/7/1.
 */
class AddMeshFailFragment : Fragment()
{
    private val TAG = javaClass.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_add_mesh_fail, container, false)
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
            mesh_fail_close_image -> GlobalBus.publish(MainEvent.EnterHomePage())

            mesh_fail_try_again_button ->
            {
                startPairingTask()

                val bundle = Bundle().apply{
                    putString("Title", "")
                    putString("Description", getString(R.string.loading_transition_please_wait))
                    putString("Sec_Description", getString(R.string.loading_transition_extender))
                    putInt("LoadingSecond", AppConfig.addMeshTime)
                    putSerializable("Anim", AppConfig.LoadingAnimation.ANIM_REBOOT)
                    putSerializable("DesPage", AppConfig.LoadingGoToPage.FRAG_MESH_FAIL)
                    putBoolean("ShowCountDownTimer", true)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionFragment().apply{ arguments = bundle }))
            }

            mesh_fail_help_text -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshHelpFragment()))
        }
    }

    private fun setClickListener()
    {
        mesh_fail_close_image.setOnClickListener(clickListener)
        mesh_fail_try_again_button.setOnClickListener(clickListener)
        mesh_fail_help_text.setOnClickListener(clickListener)
    }

    private fun startPairingTask()
    {
        val params = JSONObject()
        params.put("X_ZyXEL_TriggerWPSPushButton", true)
        LogUtil.d(TAG,"startPairingTask param:$params")

        AddMeshApi.StartPairing()
                .setRequestPageName(TAG)
                .setParams(params)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.sessionKey = sessionkey
                            GlobalBus.publish(MainEvent.StartGetWPSStatusTask())
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }
}