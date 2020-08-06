package zyxel.com.multyproneo.fragment.cloud

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup_sn_login.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.model.LoginInfo
import zyxel.com.multyproneo.tool.CryptTool
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class SetupSNLoginFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var gatewayInfo: GatewayInfo
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var loginInfo: LoginInfo
    private var keyboardListenersAttached = false
    private var serialNumberIllegalInput = false
    private var needConnectFlowForRetry = false
    private var loginBtnEnable = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_sn_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("needConnectFlowForRetry", false)?.let{ needConnectFlowForRetry = it }
        }

        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setClickListener()
        initLoginSerialNumberEdit()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
        gatewayInfo = GlobalData.getCurrentGatewayInfo()
        attachKeyboardListeners()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()

        if(keyboardListenersAttached)
            view?.viewTreeObserver?.removeGlobalOnLayoutListener(keyboardLayoutListener)
    }

    private val keyboardLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener
    {
        override fun onGlobalLayout()
        {
            val rect = Rect()
            view?.getWindowVisibleDisplayFrame(rect)
            val heightDiff = view?.rootView?.height!! - (rect.bottom - rect.top)
            if(heightDiff > 500)
            {
                setup_sn_login_title_text.visibility = View.GONE
                setup_sn_login_description_text.visibility = View.GONE
            }
            else
            {
                setup_sn_login_title_text.visibility = View.VISIBLE
                setup_sn_login_description_text.visibility = View.VISIBLE
            }
        }
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            setup_sn_login_enter_button ->
            {
                if(loginBtnEnable)
                {
                    inputMethodManager.hideSoftInputFromWindow(setup_sn_login_edit.applicationWindowToken, 0)
                    val serialNumber = setup_sn_login_edit.text.toString()
                    LogUtil.d(TAG,"loginEdit:$serialNumber")

                    GlobalBus.publish(MainEvent.ShowLoading())

                    val iv = CryptTool.getRandomString(16)
                    val encryptedSN = CryptTool.EncryptAES(
                            iv.toByteArray(charset("UTF-8")),
                            CryptTool.KeyAESDefault.toByteArray(charset("UTF-8")),
                            serialNumber.toByteArray(charset("UTF-8")))

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
                                        GlobalData.loginInfo = Gson().fromJson(responseStr, LoginInfo::class.javaObjectType)
                                        LogUtil.d(TAG,"loginInfo:${GlobalData.loginInfo}")
                                        gatewayInfo.SerialNumber = serialNumber
                                        GlobalBus.publish(MainEvent.HideLoading())
                                        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectingInternetFragment()))
                                    }
                                    catch(e: JSONException)
                                    {
                                        e.printStackTrace()
                                        GlobalBus.publish(MainEvent.HideLoading())
                                        gotoCannotConnectControllerTroubleshootingPage()
                                    }
                                }

                                override fun onFail(code: Int, msg: String, ctxName: String, isCloudUsing: Boolean)
                                {
                                    LogUtil.e(TAG, "[onFail] code = $code")
                                    LogUtil.e(TAG, "[onFail] msg = $msg")
                                    LogUtil.e(TAG, "[onFail] ctxName = $ctxName")

                                    if(ctxName == TAG && code == 401)
                                    {
                                        runOnUiThread{
                                            setup_sn_login_error_text.text = getString(R.string.login_sn_error)
                                            setup_sn_login_error_text.visibility = View.VISIBLE
                                        }
                                    }
                                    else
                                        gotoCannotConnectControllerTroubleshootingPage()
                                }
                            }).execute()
                }
            }
        }
    }

    private fun attachKeyboardListeners()
    {
        if(keyboardListenersAttached) return
        view?.viewTreeObserver?.addOnGlobalLayoutListener(keyboardLayoutListener)
        keyboardListenersAttached = true
    }

    private fun setClickListener()
    {
        setup_sn_login_enter_button.setOnClickListener(clickListener)
    }

    private fun checkInputEditUI()
    {
        when(serialNumberIllegalInput)
        {
            true ->
            {
                setup_sn_login_error_text.text = getString(R.string.login_no_support_character)
                setup_sn_login_error_text.visibility = View.VISIBLE
            }

            false -> setup_sn_login_error_text.visibility = View.INVISIBLE
        }

        when
        {
            setup_sn_login_edit.text.length >= AppConfig.loginSerialNumberRequiredLength && !serialNumberIllegalInput
            ->
            {
                loginBtnEnable = true
                setup_sn_login_enter_text.setTextColor(resources.getColor(R.color.color_000000))
            }

            else ->
            {
                loginBtnEnable = false
                setup_sn_login_enter_text.setTextColor(resources.getColor(R.color.color_888888))
            }
        }
    }

    private fun initLoginSerialNumberEdit()
    {
        setup_sn_login_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                serialNumberIllegalInput = SpecialCharacterHandler.containsEmoji(str.toString())
                checkInputEditUI()
            }
        }
    }

    private fun gotoCannotConnectControllerTroubleshootingPage()
    {
        val bundle = Bundle().apply{
            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER)
            putBoolean("needConnectFlowForRetry", needConnectFlowForRetry)
        }

        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
    }
}