package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.FindingDeviceFragment
import zyxel.com.multyproneo.util.*

class CloudWelcomeFragment : Fragment()
{
    private val TAG = javaClass.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({ decideFlow() }, AppConfig.WELCOME_DISPLAY_TIME_IN_MILLISECONDS)
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private fun decideFlow()
    {
        var oldDBExist = false
        var newDBExist = false

        doAsync{
            val gatewayInfoArrayListDB = DatabaseUtil.getInstance(activity!!)?.getGatewayFromDB()
            if(gatewayInfoArrayListDB!!.isNotEmpty())
            {
                LogUtil.d(TAG,"old DB has data!!")
                oldDBExist = true
            }
            else
                LogUtil.d(TAG,"old DB is empty!!")

            val db = DatabaseCloudUtil.getInstance(activity!!)!!
            val siteInfoList = db.getSiteInfoDao().getAll()
            if(siteInfoList.isNotEmpty())
            {
                LogUtil.d(TAG,"new DB has data!!")
                newDBExist = true
            }
            else
                LogUtil.d(TAG,"new DB is empty!!")

            uiThread{
                if(oldDBExist)
                    GlobalBus.publish(MainEvent.SwitchToFrag(FindingDeviceFragment()))
                else if(newDBExist)
                    GlobalBus.publish(MainEvent.RefreshToken(false))
                else
                    GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))
            }
        }
    }
}