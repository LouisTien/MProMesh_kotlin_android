package zyxel.com.multyproneo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import zyxel.com.multyproneo.MainActivity
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.LogUtil
import zyxel.com.multyproneo.util.SharedPreferencesUtil


class NotificationMessagingService : FirebaseMessagingService()
{
    private val TAG = javaClass.simpleName
    private val NOTIFICATION_CHANNEL_ID = "10001"
    private var msg: String = ""
    private var alert: String = ""
    private var dev_name: String = ""
    private var mac: String = ""
    private var uid: String = ""

    /*override fun handleIntent(intent: Intent?)
    {
        //super.handleIntent(intent); // disable this to prevent show notification from system

        *//*
        {received_at=1585375972, google.delivered_priority=high, google.sent_time=1585375972468, google.ttl=2419200, google.original_priority=high, gcm.notification.e=1, gcm.notification.title={"type":{"0":"out","1":"in"}}, msg=LouisHouse, uid=E7KA952WU5RMUH6GY1CJ, from=578617382573, alert= LouisTest is Connected, sound=sound.aif, google.message_id=0:1585375972499266%10ac36f910ac36f9, gcm.notification.body=0, customized_payload={"content_available":true,"notification":{"title":"{\"type\":{\"0\":\"out\",\"1\":\"in\"}}","body":"0"}}, event_time=1585375972, event_type=60, google.c.a.e=1, dev_name=LouisTest, google.c.sender.id=578617382573, collapse_key=zyxel.com.multyproneo}
         *//*

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
    }*/

    override fun onMessageReceived(remoteMessage: RemoteMessage)
    {
        if(remoteMessage.data.isNotEmpty())
        {
            LogUtil.d(TAG, "Message data : ${remoteMessage.data}")

            val remoteMessageMap = remoteMessage.data
            val keySet = remoteMessageMap.keys
            val iterator = keySet.iterator()
            while(iterator.hasNext())
            {
                val key = iterator.next() as String
                //LogUtil.d(TAG, "key : $key")
                //LogUtil.d(TAG, "value : ${remoteMessageMap[key]}")
                with(key)
                {
                    when
                    {
                        equals("mac", ignoreCase = true) -> mac = remoteMessageMap[key]?:""
                        equals("msg", ignoreCase = true) -> msg = remoteMessageMap[key]?:""
                        equals("uid", ignoreCase = true) -> uid = remoteMessageMap[key]?:""
                        equals("alert", ignoreCase = true) -> alert = remoteMessageMap[key]?:""
                        equals("dev_name", ignoreCase = true) -> dev_name = remoteMessageMap[key]?:""
                    }
                }
            }

            LogUtil.d(TAG, "mac : $mac")
            LogUtil.d(TAG, "msg : $msg")
            LogUtil.d(TAG, "uid : $uid")
            LogUtil.d(TAG, "alert : $alert")
            LogUtil.d(TAG, "dev_name : $dev_name")

            sendNotification(alert, msg)
        }

        /*if(remoteMessage.notification != null)
        {
            LogUtil.d(TAG, "Message Notification Title : ${remoteMessage.notification!!.title}")
            LogUtil.d(TAG, "Message Notification Body : ${remoteMessage.notification!!.body}")
            sendNotification(remoteMessage.notification!!.title?:"", remoteMessage.notification!!.body?:"")
        }*/
    }

    override fun onNewToken(token: String)
    {
        var notificationToken by SharedPreferencesUtil(this, AppConfig.SHAREDPREF_NOTIFICATION_TOKEN, "")
        notificationToken = token
        LogUtil.d(TAG, "Notification Refreshed token : $notificationToken")
    }

    private fun sendNotification(title: String, messageBody: String)
    {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val bundle = Bundle()
        bundle.putString("noti_uid", uid)
        bundle.putString("noti_mac", mac)
        intent.putExtras(bundle)

        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_stat_notify_mpro)
                .setContentTitle(title)
                .setContentText(" $messageBody")
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
