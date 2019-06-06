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
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.GatewayProfile
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/5/31.
 */
class LoginFragment : Fragment()
{
    private lateinit var gatewayInfo: GatewayProfile
    private lateinit var inputMethodManager: InputMethodManager
    private var gatewayIndex = 0
    private val userNameRequiredLength = 1
    private val passwordRequiredLength = 1
    private var keyboardListenersAttached = false
    private var showPassword = false
    private var userNameIllegalInput = false
    private var passwordIllegalInput = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setClickListener()
        initLoginUsernameEdit()
        initLoginPasswordEdit()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
        gatewayIndex = GlobalData.currentGatewayIndex
        gatewayInfo = GlobalData.getCurrentGatewayInfo()
        login_title_text.text = getString(R.string.login_title) + " " + gatewayInfo.modelName
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
                login_title_text.visibility = View.GONE
                login_description_text.visibility = View.GONE
            }
            else
            {
                login_title_text.visibility = View.VISIBLE
                login_description_text.visibility = View.VISIBLE
            }
        }
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            login_back_image ->
            {
                inputMethodManager.hideSoftInputFromWindow(login_username_edit.applicationWindowToken, 0)
                inputMethodManager.hideSoftInputFromWindow(login_password_edit.applicationWindowToken, 0)
                GlobalBus.publish(MainEvent.SwitchToFrag(FindingDeviceFragment()))
            }

            login_password_show_image ->
            {
                login_password_edit.transformationMethod = if(showPassword) PasswordTransformationMethod() else null
                login_password_show_image.setImageDrawable(getResources().getDrawable(if(showPassword) R.drawable.icon_hide else R.drawable.icon_show))
                showPassword = !showPassword
            }

            login_enter_button ->
            {
                inputMethodManager.hideSoftInputFromWindow(login_username_edit.applicationWindowToken, 0)
                inputMethodManager.hideSoftInputFromWindow(login_password_edit.applicationWindowToken, 0)
                GlobalBus.publish(MainEvent.SwitchToFrag(HomeFragment()))
            }
        }
    }

    protected fun attachKeyboardListeners()
    {
        if(keyboardListenersAttached) return
        view?.viewTreeObserver?.addOnGlobalLayoutListener(keyboardLayoutListener)
        keyboardListenersAttached = true
    }

    private fun setClickListener()
    {
        login_back_image.setOnClickListener(clickListener)
        login_password_show_image.setOnClickListener(clickListener)
        login_enter_button.setOnClickListener(clickListener)
    }

    private fun checkInputEditUI()
    {
        when(userNameIllegalInput)
        {
            true ->
            {
                login_username_error_text.text = getString(R.string.login_no_support_character)
                login_username_error_text.visibility = View.VISIBLE
            }

            false -> login_username_error_text.visibility = View.INVISIBLE
        }

        when(passwordIllegalInput)
        {
            true ->
            {
                login_password_error_text.text = getString(R.string.login_no_support_character)
                login_password_error_text.visibility = View.VISIBLE
            }

            false -> login_password_error_text.visibility = View.INVISIBLE
        }

        when
        {
            login_username_edit.text.length >= userNameRequiredLength
            && login_password_edit.text.length >= passwordRequiredLength
            && !userNameIllegalInput
            && !passwordIllegalInput
            -> login_enter_button.isEnabled = true

            else -> login_enter_button.isEnabled = false
        }
    }

    private fun initLoginUsernameEdit()
    {
        login_username_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, start: Int, before: Int, count: Int ->
                try
                {
                    userNameIllegalInput = SpecialCharacterHandler.containsEmoji(str.toString())
                    checkInputEditUI()
                }
                catch (ex: NumberFormatException){}
            }
        }
    }

    private fun initLoginPasswordEdit()
    {
        login_password_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, start: Int, before: Int, count: Int ->
                passwordIllegalInput = SpecialCharacterHandler.containsEmoji(str.toString())
                checkInputEditUI()
            }
        }
    }
}