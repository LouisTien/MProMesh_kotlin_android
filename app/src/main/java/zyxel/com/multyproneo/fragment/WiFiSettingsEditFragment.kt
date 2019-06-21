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
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/6/12.
 */
class WiFiSettingsEditFragment : Fragment()
{
    private lateinit var inputMethodManager: InputMethodManager
    private var name = ""
    private var pwd = ""
    private var isGuestWiFiMode = false
    private var wifiStatus = false
    private var showPassword = false
    private var keyboardListenersAttached = false
    private var wifiNameIllegalInput = false
    private var wifiPwdIllegalInput = false
    private var wifiNameEditFocus = false
    private var wifiPwdEditFocus = false
    private val wifiNameRequiredLength = 1
    private val wifiPwdRequiredLength = 8

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
            this?.getString("Name")?.let{ name = it }
            this?.getString("Password")?.let{ pwd = it }
            this?.getBoolean("WifiStatus")?.let{ wifiStatus = it }
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
            val rect = Rect()
            view?.getWindowVisibleDisplayFrame(rect)

            if(isGuestWiFiMode)
            {
                val heightDiff = view?.rootView?.height!! - (rect.bottom - rect.top)
                if(heightDiff > 500)
                    wifi_edit_switch_area_relative.visibility = View.GONE
                else
                    wifi_edit_switch_area_relative.visibility = View.VISIBLE
            }
        }
    }

    private val clickListener = View.OnClickListener { view ->
        when(view)
        {
            wifi_edit_cancel_text ->
            {
                inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_name_edit.applicationWindowToken, 0)
                inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_password_edit.applicationWindowToken, 0)
                GlobalBus.publish(MainEvent.EnterWiFiSettingsPage())
            }

            wifi_edit_save_text ->
            {
                inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_name_edit.applicationWindowToken, 0)
                inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_password_edit.applicationWindowToken, 0)

                val bundle = Bundle().apply{
                    putString("Title", "")
                    putString("Description", resources.getString(R.string.loading_transition_please_wait))
                    putString("Sec_Description", resources.getString(R.string.loading_transition_update_wifi_settings))
                    putInt("LoadingSecond", AppConfig.guestWiFiSettingTime)
                    putSerializable("Anim", AppConfig.Companion.LoadingAnimation.ANIM_REBOOT)
                    putSerializable("DesPage", AppConfig.Companion.LoadingGoToPage.FRAG_SEARCH)
                    putBoolean("showRetry", false)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionFragment().apply{ arguments = bundle }))
            }

            wifi_edit_wifi_image ->
            {
                wifiStatus = !wifiStatus
                checkWiFiSwitchStatus()
            }

            wifi_edit_wifi_password_show_image ->
            {
                wifi_edit_wifi_password_edit.transformationMethod = if(showPassword) PasswordTransformationMethod() else null
                wifi_edit_wifi_password_show_image.setImageDrawable(resources.getDrawable(if(showPassword) R.drawable.icon_hide else R.drawable.icon_show))
                showPassword = !showPassword
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
        wifi_edit_wifi_image.setOnClickListener(clickListener)
        wifi_edit_wifi_password_show_image.setOnClickListener(clickListener)
    }

    private fun initUI()
    {
        attachKeyboardListeners()
        initWiFiNameEdit()
        initWiFiPwdEdit()

        if(isGuestWiFiMode)
            checkWiFiSwitchStatus()
        else
            wifi_edit_switch_area_relative.visibility = View.GONE
    }

    private fun checkWiFiSwitchStatus() = wifi_edit_wifi_image.setImageResource(if(wifiStatus) R.drawable.switch_on else R.drawable.switch_off)

    private fun initWiFiNameEdit()
    {
        wifi_edit_wifi_name_edit.setText(name)

        wifi_edit_wifi_name_edit.setOnFocusChangeListener{
            _, hasFocus ->
            wifiNameEditFocus = hasFocus
            if(!wifiNameIllegalInput)
                wifi_edit_wifi_name_edit_line_image.setImageResource(if(hasFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
        }

        wifi_edit_wifi_name_edit.textChangedListener{
            onTextChanged{
                _: CharSequence?, _: Int, _: Int, _: Int ->
                wifiNameIllegalInput = SpecialCharacterHandler.containsEmoji(wifi_edit_wifi_name_edit.text.toString())
                checkInputEditUI()
            }
        }
    }

    private fun initWiFiPwdEdit()
    {
        wifi_edit_wifi_password_edit.setText(pwd)

        wifi_edit_wifi_password_edit.setOnFocusChangeListener{
            _, hasFocus ->
            wifiPwdEditFocus = hasFocus
            if(!wifiPwdIllegalInput)
                wifi_edit_wifi_password_edit_line_image.setImageResource(if(hasFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
        }

        wifi_edit_wifi_password_edit.textChangedListener{
            onTextChanged{
                _: CharSequence?, _: Int, _: Int, _: Int ->
                wifiPwdIllegalInput = SpecialCharacterHandler.containsEmoji(wifi_edit_wifi_password_edit.text.toString())
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
                with(wifi_edit_name_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }
                wifi_edit_wifi_name_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                wifi_edit_name_error_text.visibility = View.INVISIBLE
                wifi_edit_wifi_name_edit_line_image.setImageResource(if(wifiNameEditFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
            }
        }

        when(wifiPwdIllegalInput)
        {
            true ->
            {
                with(wifi_edit_password_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }
                wifi_edit_wifi_password_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                wifi_edit_password_error_text.visibility = View.INVISIBLE
                wifi_edit_wifi_password_edit_line_image.setImageResource(if(wifiPwdEditFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
            }
        }

        if( (wifi_edit_wifi_name_edit.text.length >= wifiNameRequiredLength)
                && (wifi_edit_wifi_password_edit.text.length >= wifiPwdRequiredLength)
                && !wifiNameIllegalInput
                && !wifiPwdIllegalInput )
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
}