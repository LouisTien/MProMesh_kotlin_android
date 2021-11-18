package zyxel.com.multyproneo.fragment.cloud

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
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_cloud_settings.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONException
import zyxel.com.multyproneo.BuildConfig
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.P2PGatewayApi
import zyxel.com.multyproneo.api.cloud.P2PWiFiSettingApi
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.WiFiSettingInfo
import zyxel.com.multyproneo.service.SendMailReceiver
import zyxel.com.multyproneo.util.*
import java.io.File

class CloudSettingsFragment : Fragment()
{
    private val TAG = "CloudSettingsFragment"
    private lateinit var msgDialogResponse: Disposable
    private lateinit var db: DatabaseCloudUtil
    private lateinit var WiFiSettingInfoSet: WiFiSettingInfo
    private lateinit var appLogZipFile: File
    private var currentDBInfo: DatabaseSiteInfoEntity? = null
    private var preserveSettingsEnable = false
    private var WiFiName = ""
    private var WiFiPwd = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            checkPreviousSetting()
        }

        db = DatabaseCloudUtil.getInstance(activity!!)!!
        setClickListener()
        getInfoFromDB()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowCloudBottomToolbar())
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
            settings_notification_relative -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsNotificationDetailFragment()))

            settings_preserve_settings_switch_image -> {

                if(preserveSettingsEnable)
                {
                    MessageDialog(
                            activity!!,
                            getString(R.string.settings_preserve_settings_alert_title),
                            getString(R.string.settings_preserve_settings_alert_msg),
                            arrayOf(getString(R.string.message_dialog_turn_off), getString(R.string.remove_site_dialog_cancel)),
                            AppConfig.DialogAction.ACT_NONE
                    ).show()
                }
                else
                    checkPreviousSetting()
            }

            settings_cloud_account_relative -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsCloudAccountDetailFragment()))

            settings_issue_report_relative ->
            {
                if(GlobalData.logFileDeliver)
                {
                    GlobalBus.publish(MainEvent.ShowLoading())
                    getFWLogFile()
                }
                else
                    zipLogAndSend()
            }

            settings_privacy_policy_relative -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zyxel.com/privacy_policy.shtml")))

            settings_troubleshooting_relative -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsTroubleshootingFragment()))
        }
    }

    private fun setClickListener()
    {
        settings_notification_relative.setOnClickListener(clickListener)
        settings_preserve_settings_switch_image.setOnClickListener(clickListener)
        settings_troubleshooting_relative.setOnClickListener(clickListener)
        settings_cloud_account_relative.setOnClickListener(clickListener)
        settings_issue_report_relative.setOnClickListener(clickListener)
        settings_privacy_policy_relative.setOnClickListener(clickListener)
    }

    private fun checkPreviousSetting()
    {
        preserveSettingsEnable = !preserveSettingsEnable

        doAsync{
            if(currentDBInfo != null)
            {
                currentDBInfo!!.backup = preserveSettingsEnable
                db.getSiteInfoDao().insert(currentDBInfo!!)
                uiThread{ updateUI() }
            }
            else
            {
                GlobalBus.publish(MainEvent.ShowLoading())
                getWiFiSettingInfoTask()
            }
        }
    }

    private fun getInfoFromDB()
    {
        LogUtil.d(TAG,"MAC:${GlobalData.getCurrentGatewayInfo().MAC}")
        doAsync{
            currentDBInfo = db.getSiteInfoDao().queryByMac(GlobalData.getCurrentGatewayInfo().MAC)
            preserveSettingsEnable = currentDBInfo?.backup?:false
            LogUtil.d(TAG,"preserveSettingsEnable:$preserveSettingsEnable")
            uiThread{ updateUI() }
        }
    }

    private fun updateUI()
    {
        settings_preserve_settings_switch_image.setImageResource(if(preserveSettingsEnable) R.drawable.switch_on else R.drawable.switch_off_2)
        settings_app_version_value_text.text = BuildConfig.VERSION_NAME
    }

    private fun addToDB()
    {
        doAsync{
            currentDBInfo = DatabaseSiteInfoEntity(
                    GlobalData.getCurrentGatewayInfo().MAC,
                    GlobalData.currentUID,
                    GlobalData.currentDisplayName,
                    "N/A",
                    WiFiName,
                    WiFiPwd,
                    preserveSettingsEnable
            )
            db.getSiteInfoDao().insert(currentDBInfo!!)
            uiThread{ updateUI() }
        }
    }

    private fun getWiFiSettingInfoTask()
    {
        LogUtil.d(TAG,"getWiFiSettingInfoTask()")
        P2PWiFiSettingApi.GetWiFiSettingInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            WiFiSettingInfoSet = Gson().fromJson(responseStr, WiFiSettingInfo::class.javaObjectType)
                            LogUtil.d(TAG,"wiFiSettingInfo:$WiFiSettingInfoSet")

                            WiFiName = WiFiSettingInfoSet.Object.SSID[0].SSID
                            WiFiPwd = WiFiSettingInfoSet.Object.AccessPoint[0].Security.KeyPassphrase
                            /*WiFiSecurity = WiFiSettingInfoSet.Object.AccessPoint[0].Security.ModeEnabled
                            WiFiName5g = WiFiSettingInfoSet.Object.SSID[4].SSID
                            WiFiPwd5g = WiFiSettingInfoSet.Object.AccessPoint[4].Security.KeyPassphrase
                            WiFiSecurity5g = WiFiSettingInfoSet.Object.AccessPoint[4].Security.ModeEnabled
                            guestWiFiName = WiFiSettingInfoSet.Object.SSID[1].SSID
                            guestWiFiPwd = WiFiSettingInfoSet.Object.AccessPoint[1].Security.KeyPassphrase
                            guestWiFiSecurity = WiFiSettingInfoSet.Object.AccessPoint[1].Security.ModeEnabled
                            guestWiFiStatus = WiFiSettingInfoSet.Object.SSID[1].Enable*/
                            addToDB()
                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                        catch(e: JSONException)
                        {
                            GlobalBus.publish(MainEvent.HideLoading())
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    private fun sendMail()
    {
        val emailIntent = Intent(Intent.ACTION_SEND)
        with(emailIntent)
        {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.FEEDBACK_MAIL))
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

    /*private fun sendMailIncludeFWLog()
    {
        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        with(emailIntent)
        {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.FEEDBACK_MAIL))
            //putExtra(Intent.EXTRA_CC, arrayOf(AppConfig.FEEDBACK_MAIL_CC))
            putExtra(Intent.EXTRA_SUBJECT, "MProMesh app v${BuildConfig.VERSION_NAME} feedback - ${GlobalData.getCurrentGatewayInfo().ModelName} ${GlobalData.getCurrentGatewayInfo().SoftwareVersion}")
            putExtra(Intent.EXTRA_TEXT, "Please describe the issue.")
        }

        val uris = ArrayList<Uri>()
        val appLogZipFileUri = FileProvider.getUriForFile(context!!, "${BuildConfig.APPLICATION_ID}.fileprovider", appLogZipFile)
        val appFWLogFileUri = FileProvider.getUriForFile(context!!, "${BuildConfig.APPLICATION_ID}.fileprovider", SaveLogUtil.fileFWLog)
        uris.add(appLogZipFileUri)
        uris.add(appFWLogFileUri)

        try
        {
            with(emailIntent)
            {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
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
    }*/

    private fun getFWLogFile()
    {
        P2PGatewayApi.GetFWLogFile()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        GlobalBus.publish(MainEvent.HideLoading())
                        zipLogAndSend()
                    }
                }).execute()
    }

    private fun zipLogAndSend()
    {
        appLogZipFile = SaveLogUtil.zipFiles()
        sendMail()
    }
}