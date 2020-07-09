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
import kotlinx.android.synthetic.main.fragment_send_feedback.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.BuildConfig
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.service.SendMailReceiver
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.SaveLogUtil
import java.io.File
import java.util.ArrayList

class SendFeedbackFragment : Fragment()
{
    private lateinit var appLogZipFile: File

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_send_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        send.onClick{
            appLogZipFile = SaveLogUtil.zipFiles()
            sendMail()
        }
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
    }

    private fun sendMail()
    {
        //val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        val emailIntent = Intent(Intent.ACTION_SEND)
        with(emailIntent)
        {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.FEEDBACK_MAIL))
            putExtra(Intent.EXTRA_SUBJECT, "Android:${Build.VERSION.RELEASE}(${BuildConfig.VERSION_NAME})")
            putExtra(Intent.EXTRA_TEXT, edit_send_feeback.text.toString())
        }

        //val uris = ArrayList<Uri>()
        val appLogZipFileUri = FileProvider.getUriForFile(context!!, "${BuildConfig.APPLICATION_ID}.fileprovider", appLogZipFile)
        //uris.add(appLogZipFileUri)

        try
        {
            with(emailIntent)
            {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                //putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
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