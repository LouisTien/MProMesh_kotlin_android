package zyxel.com.multyproneo.fragment.cloud

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_wifi_settings_edit.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.textColor
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.P2PGatewayApi
import zyxel.com.multyproneo.api.cloud.P2PWiFiSettingApi
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.LoadingTransitionProgressFragment
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudWiFiSettingsEditFragment : Fragment()
{
    private val TAG = "CloudWiFiSettingsEditFragment"
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var msgDialogResponse: Disposable
    private lateinit var db: DatabaseCloudUtil
    private var name = ""
    private var pwd = ""
    private var security = ""
    private var showPassword = false
    private var name5g = ""
    private var pwd5g = ""
    private var security5g = ""
    private var showPassword5g = false
    private var nameGuest = ""
    private var pwdGuest = ""
    private var securityGuest = ""
    private var showPasswordGuest = false
    private var wifiNameIllegalInput = false
    private var wifiPwdIllegalInput = false
    private var wifiNameEditFocus = false
    private var wifiPwdEditFocus = false
    private var wifiNameIllegalInput5g = false
    private var wifiPwdIllegalInput5g = false
    private var wifiNameEditFocus5g = false
    private var wifiPwdEditFocus5g = false
    private var wifiNameIllegalInputGuest = false
    private var wifiPwdIllegalInputGuest = false
    private var wifiNameEditFocusGuest = false
    private var wifiPwdEditFocusGuest = false
    private var showOneSSID = true
    private var available5g = false
    private var keyboardListenersAttached = false
    private var preserveSettingsEnable = false
    private var currentSiteInfo: DatabaseSiteInfoEntity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_wifi_settings_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        db = DatabaseCloudUtil.getInstance(context!!)!!

        with(arguments)
        {
            this?.getBoolean("ShowOneSSID")?.let{ showOneSSID = it }
            this?.getBoolean("Available5g")?.let{ available5g = it }
            this?.getString("Name")?.let{ name = it }
            this?.getString("Password")?.let{ pwd = it }
            this?.getString("Security")?.let{ security = it }
            this?.getString("Name5g")?.let{ name5g = it }
            this?.getString("Password5g")?.let{ pwd5g = it }
            this?.getString("Security5g")?.let{ security5g = it }
            this?.getString("NameGuest")?.let{ nameGuest = it }
            this?.getString("PasswordGuest")?.let{ pwdGuest = it }
            this?.getString("SecurityGuest")?.let{ securityGuest = it }
        }

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            setWiFiSettingTask()
            showLoadingTransitionPage()
        }

        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setSaveTextStatus(false)

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

        if(!msgDialogResponse.isDisposed) msgDialogResponse.dispose()

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
            if(wifiNameEditFocus5g || wifiPwdEditFocus5g)
            {
                val heightDiff = view?.rootView?.height!! - (rect.bottom - rect.top)
                if(heightDiff > 500)
                    wifi_edit_wifi_24g_relative.visibility = View.GONE
                else
                    wifi_edit_wifi_24g_relative.visibility = View.VISIBLE
            }
        }

        if(wifiNameEditFocusGuest || wifiPwdEditFocusGuest)
        {
            val heightDiff = view?.rootView?.height!! - (rect.bottom - rect.top)
            if(heightDiff > 500)
            {
                wifi_edit_wifi_24g_relative.visibility = View.GONE
                if(!showOneSSID)
                    wifi_edit_wifi_5g_relative.visibility = View.GONE
            }
            else
            {
                wifi_edit_wifi_24g_relative.visibility = View.VISIBLE
                if(!showOneSSID)
                    wifi_edit_wifi_5g_relative.visibility = View.VISIBLE
            }
        }
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            wifi_edit_cancel_text ->
            {
                hideAllSoftKeyboard()
                GlobalBus.publish(MainEvent.EnterCloudWiFiSettingsPage())
            }

            wifi_edit_save_text ->
            {
                hideAllSoftKeyboard()

                name = wifi_edit_wifi_24g_name_edit.text.toString()
                pwd = wifi_edit_wifi_24g_password_edit.text.toString()
                name5g = wifi_edit_wifi_5g_name_edit.text.toString()
                pwd5g = wifi_edit_wifi_5g_password_edit.text.toString()
                nameGuest = wifi_edit_guest_wifi_name_edit.text.toString()
                pwdGuest = wifi_edit_guest_wifi_password_edit.text.toString()

                MessageDialog(
                        activity!!,
                        "",
                        getString(R.string.wifi_settings_connect_change_tip),
                        arrayOf(getString(R.string.message_dialog_ok_got_it)),
                        AppConfig.DialogAction.ACT_NONE
                ).show()
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

            wifi_edit_guest_wifi_password_show_image ->
            {
                wifi_edit_guest_wifi_password_edit.transformationMethod = if(showPasswordGuest) PasswordTransformationMethod() else null
                wifi_edit_guest_wifi_password_show_image.setImageDrawable(resources.getDrawable(if(showPasswordGuest) R.drawable.icon_hide else R.drawable.icon_show))
                showPasswordGuest = !showPasswordGuest
            }
        }
    }

    private fun hideAllSoftKeyboard()
    {
        inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_24g_name_edit.applicationWindowToken, 0)
        inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_24g_password_edit.applicationWindowToken, 0)
        inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_5g_name_edit.applicationWindowToken, 0)
        inputMethodManager.hideSoftInputFromWindow(wifi_edit_wifi_5g_password_edit.applicationWindowToken, 0)
        inputMethodManager.hideSoftInputFromWindow(wifi_edit_guest_wifi_name_edit.applicationWindowToken, 0)
        inputMethodManager.hideSoftInputFromWindow(wifi_edit_guest_wifi_password_edit.applicationWindowToken, 0)
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
        wifi_edit_guest_wifi_password_show_image.setOnClickListener(clickListener)
    }

    private fun initUI()
    {
        attachKeyboardListeners()
        initWiFiNameEdit24g()
        initWiFiPwdEdit24g()
        initWiFiNameEdit5g()
        initWiFiPwdEdit5g()
        initWiFiNameEditGuest()
        initWiFiPwdEditGuest()

        if(showOneSSID)
        {
            wifi_edit_24g_title_text.text = getString(R.string.common_home)
            wifi_edit_wifi_5g_relative.visibility = View.GONE
            wifi_edit_wifi_5g_password_area_relative.visibility = View.GONE
        }
        else
        {
            if(!available5g)
            {
                wifi_edit_wifi_5g_relative.animate().alpha(0.4f)
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
                        /*|| SpecialCharacterHandler.containsSpecialCharacter(str.toString())*/
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
                        /*|| SpecialCharacterHandler.containsSpecialCharacter(str.toString())*/
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

    private fun initWiFiNameEditGuest()
    {
        wifi_edit_guest_wifi_name_edit.setText(nameGuest)

        wifi_edit_guest_wifi_name_edit.setOnFocusChangeListener{
            _, hasFocus ->
            wifiNameEditFocusGuest = hasFocus
            checkUIByKeyboardAppear()
            if(!wifiNameIllegalInputGuest)
                wifi_edit_guest_wifi_name_edit_line_image.setImageResource(if(hasFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
        }

        wifi_edit_guest_wifi_name_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                wifiNameIllegalInputGuest = SpecialCharacterHandler.containsEmoji(str.toString())
                        /*|| SpecialCharacterHandler.containsSpecialCharacter(str.toString())*/
                        || SpecialCharacterHandler.containsExcludeASCII(str.toString())
                checkInputEditUI()
            }
        }
    }

    private fun initWiFiPwdEditGuest()
    {
        wifi_edit_guest_wifi_password_edit.setText(pwdGuest)

        wifi_edit_guest_wifi_password_edit.setOnFocusChangeListener{
            _, hasFocus ->
            checkUIByKeyboardAppear()
            wifiPwdEditFocusGuest = hasFocus
            if(!wifiPwdIllegalInputGuest)
                wifi_edit_guest_wifi_password_edit_line_image.setImageResource(if(hasFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
        }

        wifi_edit_guest_wifi_password_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                wifiPwdIllegalInputGuest = SpecialCharacterHandler.containsEmoji(str.toString())
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

        when(wifiNameIllegalInputGuest)
        {
            true ->
            {
                with(wifi_guest_edit_name_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }
                wifi_edit_guest_wifi_name_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                wifi_guest_edit_name_error_text.visibility = View.INVISIBLE
                wifi_edit_guest_wifi_name_edit_line_image.setImageResource(if(wifiNameEditFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
            }
        }

        when(wifiPwdIllegalInputGuest)
        {
            true ->
            {
                with(wifi_guest_edit_password_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }
                wifi_edit_guest_wifi_password_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                wifi_guest_edit_password_error_text.visibility = View.INVISIBLE
                wifi_edit_guest_wifi_password_edit_line_image.setImageResource(if(wifiPwdEditFocus) R.color.color_ffc800 else R.color.color_b4b4b4)
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
            && saveAvailable5g
            && (wifi_edit_guest_wifi_name_edit.text.length >= AppConfig.wifiNameRequiredLength)
            && (wifi_edit_guest_wifi_password_edit.text.length >= AppConfig.wifiPwdRequiredLength)
            && !wifiNameIllegalInputGuest
            && !wifiPwdIllegalInputGuest
            && (wifi_edit_wifi_24g_name_edit.text.toString() != name
                    || wifi_edit_wifi_24g_password_edit.text.toString() != pwd
                    || wifi_edit_wifi_5g_name_edit.text.toString() != name5g
                    || wifi_edit_wifi_5g_password_edit.text.toString() != pwd5g
                    || wifi_edit_guest_wifi_name_edit.text.toString() != nameGuest
                    || wifi_edit_guest_wifi_password_edit.text.toString() != pwdGuest)
            )
            setSaveTextStatus(true)
        else
            setSaveTextStatus(false)
    }

    private fun showLoadingTransitionPage()
    {
        val bundle = Bundle().apply{
            putString("Title", getString(R.string.loading_transition_update_wifi_settings))
            putInt("LoadingSecond", AppConfig.WiFiSettingTime)
            putSerializable("DesPage", AppConfig.LoadingGoToPage.FRAG_SEARCH)
            putBoolean("IsCloud", true)
        }
        GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionProgressFragment().apply{ arguments = bundle }))
    }

    private fun setSaveTextStatus(status: Boolean)
    {
        with(wifi_edit_save_text)
        {
            isEnabled = status
            textColor = resources.getColor(if(status) R.color.color_575757 else R.color.color_b4b4b4)
        }
    }

    private fun setWiFiSettingTask()
    {
        var ssidName = name
        var setPwd = pwd
        var ssidName5g = if(showOneSSID) name else name5g
        var setPwd5g = if(showOneSSID) pwd else pwd5g
        var ssidNameGuest = nameGuest
        var setPwdGuest = pwdGuest
        ssidName = SpecialCharacterHandler.handleSpecialCharacterForJSON(ssidName)
        setPwd = SpecialCharacterHandler.handleSpecialCharacterForJSON(setPwd)
        ssidName5g = SpecialCharacterHandler.handleSpecialCharacterForJSON(ssidName5g)
        setPwd5g = SpecialCharacterHandler.handleSpecialCharacterForJSON(setPwd5g)
        ssidNameGuest = SpecialCharacterHandler.handleSpecialCharacterForJSON(ssidNameGuest)
        setPwdGuest = SpecialCharacterHandler.handleSpecialCharacterForJSON(setPwdGuest)
        val params = ",\"MultiObjects\":true,\"TR181_Objects\":[{\"object_path\":\"Device.WiFi.SSID.1.\",\"SSID\":\"$ssidName\"},{\"object_path\":\"Device.WiFi.AccessPoint.1.Security.\",\"KeyPassphrase\":\"$setPwd\",\"X_ZYXEL_AutoGenPSK\":false},{\"object_path\":\"Device.WiFi.SSID.5.\",\"SSID\":\"$ssidName5g\"},{\"object_path\":\"Device.WiFi.AccessPoint.5.Security.\",\"KeyPassphrase\":\"$setPwd5g\",\"X_ZYXEL_AutoGenPSK\":false},{\"object_path\":\"Device.WiFi.SSID.2.\",\"SSID\":\"$ssidNameGuest\"},{\"object_path\":\"Device.WiFi.AccessPoint.2.Security.\",\"KeyPassphrase\":\"$setPwdGuest\",\"X_ZYXEL_AutoGenPSK\":false},{\"object_path\":\"Device.WiFi.SSID.6.\",\"SSID\":\"$ssidNameGuest\"},{\"object_path\":\"Device.WiFi.AccessPoint.6.Security.\",\"KeyPassphrase\":\"$setPwdGuest\",\"X_ZYXEL_AutoGenPSK\":false}]}"

        P2PGatewayApi.SetMultiObjects()
                .setRequestPageName(TAG)
                .setRequestPayload(params)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        saveToDB()
                    }
                }).execute()
    }

    private fun saveToDB()
    {
        doAsync{
            currentSiteInfo = db.getSiteInfoDao().queryByMac(GlobalData.getCurrentGatewayInfo().MAC)
            preserveSettingsEnable = currentSiteInfo?.backup?:false
            if(preserveSettingsEnable && (currentSiteInfo != null))
            {
                currentSiteInfo!!.wifiSSID = name
                currentSiteInfo!!.wifiPWD = pwd
                db.getSiteInfoDao().update(currentSiteInfo!!)
            }
        }
    }
}