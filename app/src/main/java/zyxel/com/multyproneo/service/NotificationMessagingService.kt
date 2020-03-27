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

    override fun onMessageReceived(remoteMessage: RemoteMessage)
    {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        LogUtil.d(TAG, "From: " + remoteMessage.from!!)

        // Check if message contains a data payload.
        if(remoteMessage.data.isNotEmpty())
        {
            LogUtil.d(TAG, "Message data payload: " + remoteMessage.data)
            LogUtil.d(TAG, "Message data title: " + remoteMessage.notification!!.title)
            LogUtil.d(TAG, "Message data body: " + remoteMessage.notification!!.body)

            /*if(*//* Check if data needs to be processed by long running job *//* true)
            {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob()
            }
            else
            {
                // Handle message within 10 seconds
                handleNow()
            }*/

        }

        // Check if message contains a notification payload.
        if(remoteMessage.notification != null)
        {
            LogUtil.d(TAG, "Message Notification title: " + remoteMessage.notification!!.title)
            LogUtil.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body)
            sendNotification(remoteMessage.notification!!.title?:"", remoteMessage.notification!!.body?:"")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?)
    {
        LogUtil.d(TAG, "Refreshed token: " + token!!)

        var notificationToken by SharedPreferencesUtil(this, AppConfig.SHAREDPREF_NOTIFICATION_TOKEN, "N/A")
        notificationToken = token

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token)
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
