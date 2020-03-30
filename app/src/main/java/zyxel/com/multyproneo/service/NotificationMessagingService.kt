package zyxel.com.multyproneo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.WelcomeActivity
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.LogUtil
import zyxel.com.multyproneo.util.SharedPreferencesUtil


class NotificationMessagingService : FirebaseMessagingService()
{
    private val TAG = javaClass.simpleName
    private val NOTIFICATION_CHANNEL_ID = "10001"
    private var bundle: Bundle? = null
    private var title: String = ""
    private var msg: String = ""
    private var alert: String = ""
    private var dev_name: String = ""

    override fun handleIntent(intent: Intent?)
    {
        //super.handleIntent(intent); // disable this to prevent show notification from system

        /*
        {received_at=1585375972, google.delivered_priority=high, google.sent_time=1585375972468, google.ttl=2419200, google.original_priority=high, gcm.notification.e=1, gcm.notification.title={"type":{"0":"out","1":"in"}}, msg=LouisHouse, uid=E7KA952WU5RMUH6GY1CJ, from=578617382573, alert= LouisTest is Connected, sound=sound.aif, google.message_id=0:1585375972499266%10ac36f910ac36f9, gcm.notification.body=0, customized_payload={"content_available":true,"notification":{"title":"{\"type\":{\"0\":\"out\",\"1\":\"in\"}}","body":"0"}}, event_time=1585375972, event_type=60, google.c.a.e=1, dev_name=LouisTest, google.c.sender.id=578617382573, collapse_key=zyxel.com.multyproneo}
         */

        with(intent)
        {
            this?.extras?.let{ bundle = it }
        }

        with(bundle)
        {
            this?.getString("gcm.notification.title")?.let{ title = it }
            this?.getString("msg")?.let{ msg = it }
            this?.getString("alert")?.let{ alert = it }
            this?.getString("dev_name")?.let{ dev_name = it }
        }

        LogUtil.d(TAG, "Message bundle : $bundle")
        LogUtil.d(TAG, "Message title : $title")
        LogUtil.d(TAG, "Message msg : $msg")
        LogUtil.d(TAG, "Message alert : $alert")
        LogUtil.d(TAG, "Message dev_name : $dev_name")

        sendNotification(alert, msg)
    }

    private fun sendNotification(title: String, messageBody: String)
    {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        /*if(data != null)
        {
            val bundle = Bundle()
            bundle.putString("organization_id", data.getOrganizationId())
            bundle.putString("site_id", data.getSiteId())
            intent.putExtras(bundle)
        }*/
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.multyproneo_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt() /* ID of notification */, notificationBuilder.build())
    }
}
