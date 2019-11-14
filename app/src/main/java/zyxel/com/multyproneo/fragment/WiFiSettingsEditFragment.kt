package zyxel.com.multyproneo.fragment

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.fragment_wifi_settings_edit.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.textColor
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.WiFiSettingApi
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/6/12.
 */
class WiFiSettingsEditFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var inputMethodManager: InputMethodManager
    private var name = ""
    private var pwd = ""
    private var security = ""
    private var showPassword = false
    private var name5g = ""
    private var pwd5g = ""
    private var security5g = ""
    private var showPassword5g = false
    private var wifiNameIllegalInput = false
    private var wifiPwdIllegalInput = false
    private var wifiNameEditFocus = false
    private var wifiPwdEditFocus = false
    private var wifiNameIllegalInput5g = false
    private var wifiPwdIllegalInput5g = false
    private var wifiNameEditFocus5g = false
    private var wifiPwdEditFocus5g = false
    private var isGuestWiFiMode = false
    private var showOneSSID = true
    private var available5g = false
    private var keyboardListenersAttached = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_wifi_settings_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("GuestWiFiMode")?.let{ isGuestWiFiMode = it }
            this?.getBoolean("ShowOneSSID")?.let{ showOneSSID = it }
            this?.getBoolean("Available5g")?.let{ available5g = it }
            this?.getString("Name")?.let{ name = it }
            this?.getString("Password")?.let{ pwd = it }
            this?.getString("Security")?.let{ security = it }
            this?.getString("Name5g")?.let{ name5g = it }
            this?.getString("Password5g")?.let{ pwd5g = it }
            this?.getString("Security5g")?.let{ security5g = it }
        }

        if(isGuestWiFiMode)
        {
            showOneSSID = true
            available5g = false
        }

        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
        initUI()
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
            checkUIByKeyboardAppear()
        }
    }

    private fun checkUIByKeyboardAppear()
    {
        val rect = Rect()
        view?.getWindowVisibleDisplayFrame(rect)

        if(!showOneSSID && available5g)
        {
            val heightDiff = view?.rootView?.height!! - (rect.bottom - rect.top)
            if(wifiNameEditFocus5g || wifiPwdEditFocus5g)
            {
                val heightDiff = view?.rootView?.height!! - (rect.bottom - rect.top)
                if(heightDiff > 500)
                {
                    wifi_edit_wifi_24g_name_area_relative.visibility = View.GONE
                    wifi_edit_wifi_24g_password_area_relative.visibility = View.GONE
                }
                else
                {
                    wifi_edit_wifi_24g_name_area_relative.visibility = View.VISIBLE
                    wifi_edit_wifi_24g_password_area_relative.visibility = View.VISIBLE
                }
            }
        }
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            wifi_edit_cancel_text ->
            {
                inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_24g_name_edit.applicationWindowToken, 0)
                inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_24g_password_edit.applicationWindowToken, 0)
                GlobalBus.publish(MainEvent.EnterWiFiSettingsPage())
            }

            wifi_edit_save_text ->
            {
                inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_24g_name_edit.applicationWindowToken, 0)
                inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_24g_password_edit.applicationWindowToken, 0)

                name = wifi_edit_wifi_24g_name_edit.text.toString()
                pwd = wifi_edit_wifi_24g_password_edit.text.toString()
                name5g = wifi_edit_wifi_5g_name_edit.text.toString()
                pwd5g = wifi_edit_wifi_5g_password_edit.text.toString()

                if(isGuestWiFiMode)
                    setGuestWiFi24GSSIDTask()
                else
                    setWiFi24GSSIDTask()

                val bundle = Bundle().apply{
                    putString("Title", "")
                    putString("Description", resources.getString(R.string.loading_transition_please_wait))
                    putString("Sec_Description", resources.getString(R.string.loading_transition_update_wifi_settings))
                    putInt("LoadingSecond", AppConfig.WiFiSettingTime)
                    putSerializable("Anim", AppConfig.LoadingAnimation.ANIM_REBOOT)
                    putSerializable("DesPage", AppConfig.LoadingGoToPage.FRAG_SEARCH)
                    putBoolean("ShowCountDownTimer", false)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionFragment().apply{ arguments = bundle }))
            }

            wifi_edit_wifi_24g_password_show_image ->
            {
                wifi_edit_wifi_24g_password_edit.transformationMethod = if(showPassword) PasswordTransformationMethod() else null
                wifi_edit_wifi_24g_password_show_image.setImageDrawable(resources.getDrawable(if(showPassword) R.drawable.icon_hide else R.drawable.icon_show))
                showPassword = !showPassword
            }

            wifi_edit_wifi_5g_password_show_image ->
            {
                wifi_edit_wifi_5g_password_edit.transformationMethod = if(showPassword5g) PasswordTransformationMethod() else null
                wifi_edit_wifi_5g_password_show_image.setImageDrawable(resources.getDrawable(if(showPassword5g) R.drawable.icon_hide else R.drawable.icon_show))
                showPassword5g = !showPassword5g
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
        wifi_edit_cancel_text.setOnClickListener(clickListener)
        wifi_edit_save_text.setOnClickListener(clickListener)
        wifi_edit_wifi_24g_password_show_image.setOnClickListener(clickListener)
        wifi_edit_wifi_5g_password_show_image.setOnClickListener(clickListener)
    }

    private fun initUI()
    {
        attachKeyboardListeners()
        initWiFiNameEdit24g()
        initWiFiPwdEdit24g()
        initWiFiNameEdit5g()
        initWiFiPwdEdit5g()

        if(showOneSSID)
        {
            wifi_edit_wifi_24g_name_text.text = getString(R.string.wifi_settings_wifi_name)
            wifi_edit_wifi_24g_password_text.text = getString(R.string.wifi_settings_wifi_password)
            wifi_edit_wifi_5g_name_area_relative.visibility = View.GONE
            wifi_edit_wifi_5g_password_area_relative.visibility = View.GONE
        }
        else
        {
            if(!available5g)
            {
                wifi_edit_wifi_5g_name_area_relative.animate().alpha(0.4f)
                wifi_edit_wifi_5g_password_area_relative.animate().alpha(0.4f)
                wifi_edit_wifi_5g_name_edit.isEnabled = false
                wifi_edit_wifi_5g_password_edit.isEnabled = false
                wifi_edit_wifi_5g_password_show_image.isEnabled = false
            }
        }
    }

    private fun initWiFiNameEdit24g()
    {
        wifi_edit_wifi_24g_name_edit.setText(name)

        wifi_edit_wifi_24g_name_edit.setOnFocusChangeListener{
            _, hasFocus ->
            wifiNameEditFocus = hasFocus
            checkUIByKeyboardAppear()
            if(!wifiNameIllegalInput)
                wifi_edit_wifi_24g_name_edit_line_image.setImageResource(if(hasFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
        }

        wifi_edit_wifi_24g_name_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                wifiNameIllegalInput = SpecialCharacterHandler.containsEmoji(str.toString())
                                    || SpecialCharacterHandler.containsSpecialCharacter(str.toString())
                                    || SpecialCharacterHandler.containsExcludeASCII(str.toString())
                checkInputEditUI()
            }
        }
    }

    private fun initWiFiPwdEdit24g()
    {
        wifi_edit_wifi_24g_password_edit.setText(pwd)

        wifi_edit_wifi_24g_password_edit.setOnFocusChangeListener{
            _, hasFocus ->
            checkUIByKeyboardAppear()
            wifiPwdEditFocus = hasFocus
            if(!wifiPwdIllegalInput)
                wifi_edit_wifi_24g_password_edit_line_image.setImageResource(if(hasFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
        }

        wifi_edit_wifi_24g_password_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                wifiPwdIllegalInput = SpecialCharacterHandler.containsEmoji(str.toString())
                                   || SpecialCharacterHandler.containsExcludeASCII(str.toString())
                checkInputEditUI()
            }
        }
    }

    private fun initWiFiNameEdit5g()
    {
        wifi_edit_wifi_5g_name_edit.setText(name5g)

        wifi_edit_wifi_5g_name_edit.setOnFocusChangeListener{
            _, hasFocus ->
            wifiNameEditFocus5g = hasFocus
            checkUIByKeyboardAppear()
            if(!wifiNameIllegalInput5g)
                wifi_edit_wifi_5g_name_edit_line_image.setImageResource(if(hasFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
        }

        wifi_edit_wifi_5g_name_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                wifiNameIllegalInput5g = SpecialCharacterHandler.containsEmoji(str.toString())
                                        || SpecialCharacterHandler.containsSpecialCharacter(str.toString())
                                        || SpecialCharacterHandler.containsExcludeASCII(str.toString())
                checkInputEditUI()
            }
        }
    }

    private fun initWiFiPwdEdit5g()
    {
        wifi_edit_wifi_5g_password_edit.setText(pwd5g)

        wifi_edit_wifi_5g_password_edit.setOnFocusChangeListener{
            _, hasFocus ->
            wifiPwdEditFocus5g = hasFocus
            checkUIByKeyboardAppear()
            if(!wifiPwdIllegalInput5g)
                wifi_edit_wifi_5g_password_edit_line_image.setImageResource(if(hasFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
        }

        wifi_edit_wifi_5g_password_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                wifiPwdIllegalInput5g = SpecialCharacterHandler.containsEmoji(str.toString())
                                     || SpecialCharacterHandler.containsExcludeASCII(str.toString())
                checkInputEditUI()
            }
        }
    }

    private fun checkInputEditUI()
    {
        when(wifiNameIllegalInput)
        {
            true ->
            {
                with(wifi_edit_24g_name_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }
                wifi_edit_wifi_24g_name_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                wifi_edit_24g_name_error_text.visibility = View.INVISIBLE
                wifi_edit_wifi_24g_name_edit_line_image.setImageResource(if(wifiNameEditFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
            }
        }

        when(wifiPwdIllegalInput)
        {
            true ->
            {
                with(wifi_edit_24g_password_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }
                wifi_edit_wifi_24g_password_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                wifi_edit_24g_password_error_text.visibility = View.INVISIBLE
                wifi_edit_wifi_24g_password_edit_line_image.setImageResource(if(wifiPwdEditFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
            }
        }

        when(wifiNameIllegalInput5g)
        {
            true ->
            {
                with(wifi_edit_5g_name_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }
                wifi_edit_wifi_5g_name_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                wifi_edit_5g_name_error_text.visibility = View.INVISIBLE
                wifi_edit_wifi_5g_name_edit_line_image.setImageResource(if(wifiNameEditFocus5g) R.color.color_ffc800 else R.color.color_b4b4b4)
            }
        }

        when(wifiPwdIllegalInput5g)
        {
            true ->
            {
                with(wifi_edit_5g_password_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }
                wifi_edit_wifi_5g_password_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                wifi_edit_5g_password_error_text.visibility = View.INVISIBLE
                wifi_edit_wifi_5g_password_edit_line_image.setImageResource(if(wifiPwdEditFocus5g) R.color.color_ffc800 else R.color.color_b4b4b4)
            }
        }

        var saveAvailable5g = true
        if(available5g)
        {
            if( (wifi_edit_wifi_5g_name_edit.text.length < AppConfig.wifiNameRequiredLength)
                    || (wifi_edit_wifi_5g_password_edit.text.length < AppConfig.wifiPwdRequiredLength)
                    || wifiNameIllegalInput5g
                    || wifiPwdIllegalInput5g )
                saveAvailable5g = false
        }

        if( (wifi_edit_wifi_24g_name_edit.text.length >= AppConfig.wifiNameRequiredLength)
                && (wifi_edit_wifi_24g_password_edit.text.length >= AppConfig.wifiPwdRequiredLength)
                && !wifiNameIllegalInput
                && !wifiPwdIllegalInput
                && saveAvailable5g )
            setSaveTextStatus(true)
        else
            setSaveTextStatus(false)
    }

    private fun setSaveTextStatus(status: Boolean)
    {
        with(wifi_edit_save_text)
        {
            isEnabled = status
            textColor = resources.getColor(if(status) R.color.color_575757 else R.color.color_b4b4b4)
        }
    }

    private fun setWiFi24GSSIDTask()
    {
        val params = JSONObject()
        params.put("SSID", name)
        LogUtil.d(TAG,"setWiFi24GSSIDTask param:${params.toString()}")

        WiFiSettingApi.SetWiFi24GInfo()
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
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setWiFi24GPwdTask()
                    }
                }).execute()
    }

    private fun setWiFi24GPwdTask()
    {
        val params = JSONObject()
        params.put("ModeEnabled", security)
        params.put("KeyPassphrase", pwd)
        params.put("X_ZYXEL_AutoGenPSK", false)
        LogUtil.d(TAG,"setWiFi24GPwdTask param:${params.toString()}")

        WiFiSettingApi.SetWiFi24GPwd()
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
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        if(showOneSSID)
                            setLogoutTask()
                        else
                            setWiFi5GSSIDTask()
                    }
                }).execute()
    }

    private fun setWiFi5GSSIDTask()
    {
        val params = JSONObject()
        params.put("SSID", name5g)
        LogUtil.d(TAG,"setWiFi5GSSIDTask param:${params.toString()}")

        WiFiSettingApi.SetWiFi5GInfo()
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
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setWiFi5GPwdTask()
                    }
                }).execute()
    }

    private fun setWiFi5GPwdTask()
    {
        val params = JSONObject()
        params.put("ModeEnabled", security5g)
        params.put("KeyPassphrase", pwd5g)
        params.put("X_ZYXEL_AutoGenPSK", false)
        LogUtil.d(TAG,"setWiFi5GPwdTask param:${params.toString()}")

        WiFiSettingApi.SetWiFi5GPwd()
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
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setLogoutTask()
                    }
                }).execute()
    }

    private fun setGuestWiFi24GSSIDTask()
    {
        val params = JSONObject()
        params.put("SSID", name)
        LogUtil.d(TAG,"setGuestWiFi24GSSIDTask param:${params.toString()}")

        WiFiSettingApi.SetGuestWiFi24GInfo()
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
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setGuestWiFi24GPwdTask()
                    }
                }).execute()
    }

    private fun setGuestWiFi24GPwdTask()
    {
        val params = JSONObject()
        params.put("ModeEnabled", security)
        params.put("KeyPassphrase", pwd)
        params.put("X_ZYXEL_AutoGenPSK", false)
        LogUtil.d(TAG,"setGuestWiFi24GPwdTask param:${params.toString()}")

        WiFiSettingApi.SetGuestWiFi24GPwd()
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
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setGuestWiFi5GSSIDTask()
                    }
                }).execute()
    }

    private fun setGuestWiFi5GSSIDTask()
    {
        val params = JSONObject()
        params.put("SSID", name)
        LogUtil.d(TAG,"setGuestWiFi5GSSIDTask param:${params.toString()}")

        WiFiSettingApi.SetGuestWiFi5GInfo()
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
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setGuestWiFi5GPwdTask()
                    }
                }).execute()
    }

    private fun setGuestWiFi5GPwdTask()
    {
        val params = JSONObject()
        params.put("ModeEnabled", security)
        params.put("KeyPassphrase", pwd)
        params.put("X_ZYXEL_AutoGenPSK", false)
        LogUtil.d(TAG,"setGuestWiFi5GPwdTask param:${params.toString()}")

        WiFiSettingApi.SetGuestWiFi5GPwd()
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
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setLogoutTask()
                    }
                }).execute()
    }

    private fun setLogoutTask()
    {
        val params = JSONObject()
        AccountApi.Logout()
                .setRequestPageName(TAG)
                .setParams(params)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {

                    }
                }).execute()
    }
}