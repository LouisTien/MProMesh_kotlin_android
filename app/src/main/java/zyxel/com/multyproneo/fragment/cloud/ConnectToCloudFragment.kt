package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_connect_to_cloud.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.GatewayApi
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.cloud.CloudAgentInfo
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class ConnectToCloudFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var cloudAgentInfo: CloudAgentInfo
    private var isInSetupFlow = true
    private var needLoginWhenFinal = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_connect_to_cloud, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("isInSetupFlow", true)?.let{ isInSetupFlow = it }
            this?.getBoolean("needLoginWhenFinal", false)?.let{ needLoginWhenFinal = it }
        }

        connect_to_cloud_continue_image.onClick{
            val bundle = Bundle().apply{
                putBoolean("isInSetupFlow", isInSetupFlow)
                putBoolean("needLoginWhenFinal", needLoginWhenFinal)
            }
            GlobalBus.publish(MainEvent.SwitchToFrag(CloudLoginFragment().apply{ arguments = bundle }))
        }

        getIOTCLoginStatus()

        /*runOnUiThread{
            connect_to_cloud_content_animation_view.setAnimation("ConnectToTheCloud_oldJson.json")
            connect_to_cloud_content_animation_view.playAnimation()
        }*/
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

    private fun getIOTCLoginStatus()
    {
        LogUtil.d(TAG,"getIOTCLoginStatus()")

        GlobalBus.publish(MainEvent.ShowLoading())

        GatewayApi.GetCloudAgentInfo()
                .setRequestPageName(TAG)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        GlobalBus.publish(MainEvent.HideLoading())

                        try
                        {
                            cloudAgentInfo = Gson().fromJson(responseStr, CloudAgentInfo::class.javaObjectType)
                            LogUtil.d(TAG,"getIOTCLoginStatus:$cloudAgentInfo")

                            if(cloudAgentInfo.Object.Status.contains("success", ignoreCase = true))
                            {
                                runOnUiThread{
                                    MessageDialog(
                                            activity!!,
                                            getString(R.string.settings_login_cloud_title),
                                            getString(R.string.settings_login_cloud_msg),
                                            arrayOf(getString(R.string.setup_connect_controller_format_error_dialog_confirm)),
                                            AppConfig.DialogAction.ACT_NONE
                                    ).show()
                                }
                            }
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }
}