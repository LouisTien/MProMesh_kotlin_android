package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_connect_to_cloud.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.GatewayApi
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.LoginInfo
import zyxel.com.multyproneo.model.cloud.CloudAgentInfo
import zyxel.com.multyproneo.tool.CryptTool
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class ConnectToCloudFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var cloudAgentInfo: CloudAgentInfo
    private lateinit var loginInfo: LoginInfo
    private lateinit var countDownTimerIOTCStatus: CountDownTimer
    private lateinit var msgDialogResponse: Disposable
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

        countDownTimerIOTCStatus = object : CountDownTimer((AppConfig.waitForGetIOTCLoginStatusTime * 1000).toLong(), 1000)
        {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() = getIOTCLoginStatus()
        }

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{ gotoCloudLogin() }

        connect_to_cloud_continue_image.onClick{
            if(cloudAgentInfo.Object.Status.contains("success", ignoreCase = true))
            {
                MessageDialog(
                        activity!!,
                        getString(R.string.settings_login_cloud_title),
                        getString(R.string.settings_login_cloud_msg),
                        arrayOf(getString(R.string.setup_connect_controller_format_error_dialog_confirm)),
                        AppConfig.DialogAction.ACT_NONE
                ).show()
            }
            else
                gotoCloudLogin()
        }

        GlobalBus.publish(MainEvent.ShowLoading())

        if(AppConfig.SNLogin)
        {
            if(needLoginWhenFinal)
            {
                SNlogin()
                countDownTimerIOTCStatus.start()
            }
            else
                getIOTCLoginStatus()
        }
        else //SetupFinalizingYourHomeNetwork do login
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
        countDownTimerIOTCStatus.cancel()
        if(!msgDialogResponse.isDisposed) msgDialogResponse.dispose()
    }

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        LogUtil.d(TAG, "updateUI()")

        runOnUiThread{
            if(cloudAgentInfo.Object.Status.contains("success", ignoreCase = true))
                connect_to_cloud_continue_image.setImageResource(R.drawable.btn_login_on)
            else
                connect_to_cloud_continue_image.setImageResource(R.drawable.btn_continue)

            connect_to_cloud_continue_image.visibility = View.VISIBLE
        }
    }

    private fun gotoCloudLogin()
    {
        val bundle = Bundle().apply{
            putBoolean("isInSetupFlow", isInSetupFlow)
            putBoolean("needLoginWhenFinal", needLoginWhenFinal)
        }
        GlobalBus.publish(MainEvent.SwitchToFrag(CloudLoginFragment().apply{ arguments = bundle }))
    }

    private fun getIOTCLoginStatus()
    {
        LogUtil.d(TAG,"getIOTCLoginStatus()")

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
                            updateUI()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    private fun SNlogin()
    {
        LogUtil.d(TAG,"SNlogin()")

        val iv = CryptTool.getRandomString(16)
        val encryptedSN = CryptTool.EncryptAES(
                iv.toByteArray(charset("UTF-8")),
                CryptTool.KeyAESDefault.toByteArray(charset("UTF-8")),
                GlobalData.getCurrentGatewayInfo().SerialNumber.toByteArray(charset("UTF-8")))

        val params = JSONObject()
        params.put("serialnumber", encryptedSN)
        params.put("iv", iv)
        LogUtil.d(TAG,"login param:$params")
        AccountApi.SNLogin()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            loginInfo = Gson().fromJson(responseStr, LoginInfo::class.javaObjectType)
                            LogUtil.d(TAG,"loginInfo:$loginInfo")
                            GlobalData.sessionKey = loginInfo.sessionkey
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }
}