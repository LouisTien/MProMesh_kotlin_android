package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.dialog_randow_password.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

class RandomPasswordDialog(
    context: Context,
    private var password24g: String,
    private var password5g: String,
    private var passwordGuest: String,
    private var is24gPasswordChange: Boolean,
    private var is5gPasswordChange: Boolean,
    private var isGuestPasswordChagne: Boolean,
    private var TAG: String,
    var action: AppConfig.DialogAction
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_randow_password)
        setCancelable(false)
        setClickListener()
        initUI()
    }

    private fun initUI() {
        random_password_24g_relative.visibility =
            if (is24gPasswordChange) View.VISIBLE else View.GONE
        random_password_5g_relative.visibility =
            if (is5gPasswordChange) View.VISIBLE else View.GONE
        random_password_guest_relative.visibility =
            if (isGuestPasswordChagne) View.VISIBLE else View.GONE

        if (is24gPasswordChange && is5gPasswordChange) {
            random_password_24g_description_text.text =
                context.getString(R.string.random_password_24g_description)
        } else {
            random_password_24g_description_text.text =
                context.getString(R.string.random_password_description)
        }

        random_password_24g_pwd_text.text = password24g
        random_password_5g_pwd_text.text = password5g
        random_password_guest_pwd_text.text = passwordGuest

    }

    override fun show() {
        super.show()
    }

    private fun setClickListener() {
        random_password_24g_pwd_relative.setOnClickListener(clickListener)
        random_password_5g_pwd_relative.setOnClickListener(clickListener)
        random_password_guest_pwd_relative.setOnClickListener(clickListener)
        random_password_close_text.setOnClickListener(clickListener)
    }

    private val clickListener = View.OnClickListener { view ->

        when (view) {
            random_password_24g_pwd_relative -> {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("password", password24g)
                clipboard.setPrimaryClip(clip)
                GlobalBus.publish(
                    MainEvent.ShowToast(
                        context.getString(R.string.password_copied),
                        TAG
                    )
                )
            }

            random_password_5g_pwd_relative -> {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("password", password5g)
                clipboard.setPrimaryClip(clip)
                GlobalBus.publish(
                    MainEvent.ShowToast(
                        context.getString(R.string.password_copied),
                        TAG
                    )
                )
            }

            random_password_guest_pwd_relative -> {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("password", passwordGuest)
                clipboard.setPrimaryClip(clip)
                GlobalBus.publish(
                    MainEvent.ShowToast(
                        context.getString(R.string.password_copied),
                        TAG
                    )
                )
            }

            random_password_close_text -> {
                GlobalBus.publish(DialogEvent.OnCloselBtn(action))
                dismiss()
            }
        }

    }
}