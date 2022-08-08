package zyxel.com.multyproneo.fragment

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_account.*
import org.json.JSONObject
import zyxel.com.multyproneo.BuildConfig
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.service.SendMailReceiver
import zyxel.com.multyproneo.util.*
import java.io.File

/**
 * Created by LouisTien on 2019/6/13.
 */
class AccountFragment : Fragment()
{
    private val TAG = "AccountFragment"
    private lateinit var msgDialogResponse: Disposable
    private lateinit var appLogZipFile: File

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        account_app_version_text.text = BuildConfig.VERSION_NAME

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_LOGOUT -> setLogoutTask()
                else -> {}
            }
        }

        account_send_feedback_relative.visibility = if(BuildConfig.DEBUG) View.VISIBLE else View.GONE
        account_send_feedback_line_image.visibility = if(BuildConfig.DEBUG) View.VISIBLE else View.GONE

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!msgDialogResponse.isDisposed) msgDialogResponse.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            account_help_relative -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://service-provider.zyxel.com/app-help/MProMesh/index.html")))
            account_privacy_policy_relative -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zyxel.com/privacy_policy.shtml")))

            account_logout_button ->
            {
                MessageDialog(
                        activity!!,
                        getString(R.string.account_logout),
                        getString(R.string.message_dialog_check_lougot),
                        arrayOf(getString(R.string.message_dialog_yes), getString(R.string.message_dialog_no)),
                        AppConfig.DialogAction.ACT_LOGOUT
                ).show()
            }

            account_send_feedback_relative -> zipLogAndSend()
        }
    }

    private fun setClickListener()
    {
        account_help_relative.setOnClickListener(clickListener)
        account_privacy_policy_relative.setOnClickListener(clickListener)
        account_logout_button.setOnClickListener(clickListener)
        account_send_feedback_relative.setOnClickListener(clickListener)
    }

    private fun setLogoutTask()
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        val params = JSONObject()
        AccountApi.Logout()
                .setRequestPageName(TAG)
                .setParams(params)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        DatabaseUtil.getInstance(activity!!)?.deleteInformationToDB(GlobalData.getCurrentGatewayInfo())
                        GlobalBus.publish(MainEvent.HideLoading())
                        GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
                    }
                }).execute()
    }

    private fun zipLogAndSend()
    {
        appLogZipFile = SaveLogUtil.zipFiles()
        sendMail()
    }

    private fun sendMail()
    {
        val emailIntent = Intent(Intent.ACTION_SEND)
        with(emailIntent)
        {
            type = "text/plain"
            //putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.FEEDBACK_MAIL))
            //putExtra(Intent.EXTRA_CC, arrayOf(AppConfig.FEEDBACK_MAIL_CC))
            putExtra(Intent.EXTRA_SUBJECT, "MProMesh app v${BuildConfig.VERSION_NAME} feedback - ${GlobalData.getCurrentGatewayInfo().ModelName} ${GlobalData.getCurrentGatewayInfo().SoftwareVersion}")
            putExtra(Intent.EXTRA_TEXT, "Please describe the issue.")
        }

        val appLogZipFileUri = FileProvider.getUriForFile(context!!, "${BuildConfig.APPLICATION_ID}.fileprovider", appLogZipFile)

        try
        {
            with(emailIntent)
            {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                putExtra(Intent.EXTRA_STREAM, appLogZipFileUri)
            }

            val receiverIntent = Intent(context, SendMailReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                context!!.startActivity(Intent.createChooser(emailIntent, "Send mail", pendingIntent.intentSender))
            else
                context!!.startActivity(Intent.createChooser(emailIntent, "Send mail"))
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            context!!.startActivity(Intent.createChooser(emailIntent, "Send mail"))
        }
    }
}