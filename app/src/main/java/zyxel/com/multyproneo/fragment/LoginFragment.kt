package zyxel.com.multyproneo.fragment

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.fragment_login.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.GatewayProfile
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
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
        gatewayIndex = GlobalData.currentGatewayIndex
        gatewayInfo = GlobalData.getCurrentGatewayInfo()
        login_title_text.text = getString(R.string.login_title) + " " + gatewayInfo.modelName
        attachKeyboardListeners()
        initLoginUsernameEdit()
        initLoginPasswordEdit()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    protected fun attachKeyboardListeners()
    {
        if(keyboardListenersAttached) return

        view?.viewTreeObserver?.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener
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
        })

        keyboardListenersAttached = true
    }

    private fun initLoginUsernameEdit()
    {

    }

    private fun initLoginPasswordEdit()
    {

    }
}