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
import kotlinx.android.synthetic.main.fragment_setup_change_admin_password.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.AppConfig

class SetupChangeAdminPasswordFragment : Fragment()
{
    private lateinit var inputMethodManager: InputMethodManager
    private var newPWDIllegalInput = false
    private var confirmNewPWDIllegalInput = false
    private var keyboardListenersAttached = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_change_admin_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setClickListener()
        initNewPasswordEdit()
        initConfirmNewPasswordEdit()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
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
                setup_change_admin_pwd_title_text.visibility = View.GONE
                setup_change_admin_pwd_description_text.visibility = View.GONE
            }
            else
            {
                setup_change_admin_pwd_title_text.visibility = View.VISIBLE
                setup_change_admin_pwd_description_text.visibility = View.VISIBLE
            }
        }
    }

    private val clickListener = View.OnClickListener { view ->
        when(view)
        {
            setup_change_admin_pwd_password_skip_image -> {}

            setup_change_admin_pwd_password_change_image ->
            {
                when(setup_change_admin_pwd_new_password_edit.text == setup_change_admin_pwd_confirm_new_password_edit.text)
                {
                    true -> {}

                    false ->
                    {
                        setup_change_admin_pwd_password_error_text.text = getString(R.string.setup_change_admin_pwd_not_match_error_msg)
                        setup_change_admin_pwd_password_error_text.visibility = View.VISIBLE
                    }
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
        setup_change_admin_pwd_password_skip_image.setOnClickListener(clickListener)
        setup_change_admin_pwd_password_change_image.setOnClickListener(clickListener)
    }

    private fun checkInputEditUI()
    {
        when(newPWDIllegalInput)
        {
            true ->
            {
                setup_change_admin_pwd_new_password_error_text.text = getString(R.string.login_no_support_character)
                setup_change_admin_pwd_new_password_error_text.visibility = View.VISIBLE
            }

            false -> setup_change_admin_pwd_new_password_error_text.visibility = View.INVISIBLE
        }

        when(confirmNewPWDIllegalInput)
        {
            true ->
            {
                setup_change_admin_pwd_password_error_text.text = getString(R.string.login_no_support_character)
                setup_change_admin_pwd_password_error_text.visibility = View.VISIBLE
            }

            false -> setup_change_admin_pwd_password_error_text.visibility = View.INVISIBLE
        }

        when
        {
            setup_change_admin_pwd_new_password_edit.text.length >= AppConfig.loginPwdRequiredLength
                    && setup_change_admin_pwd_confirm_new_password_edit.text.length >= AppConfig.loginPwdRequiredLength
                    && !newPWDIllegalInput
                    && !confirmNewPWDIllegalInput
            ->
            {
                setup_change_admin_pwd_password_change_image.isEnabled = true
                setup_change_admin_pwd_password_change_image.setImageResource(R.drawable.btn_change_on)
            }

            else ->
            {
                setup_change_admin_pwd_password_change_image.isEnabled = false
                setup_change_admin_pwd_password_change_image.setImageResource(R.drawable.btn_change_off)
            }
        }
    }

    private fun initNewPasswordEdit()
    {
        setup_change_admin_pwd_new_password_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                newPWDIllegalInput = SpecialCharacterHandler.containsEmoji(str.toString())
                checkInputEditUI()
            }
        }
    }

    private fun initConfirmNewPasswordEdit()
    {
        setup_change_admin_pwd_confirm_new_password_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                confirmNewPWDIllegalInput = SpecialCharacterHandler.containsEmoji(str.toString())
                checkInputEditUI()
            }
        }
    }
}