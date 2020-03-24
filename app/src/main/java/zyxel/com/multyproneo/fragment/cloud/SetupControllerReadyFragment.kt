package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup_controller_ready.*
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.*
import zyxel.com.multyproneo.database.room.DatabaseClientListEntity
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.dialog.SetupControllerReadyHelpDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfo
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.LogUtil
import zyxel.com.multyproneo.util.SharedPreferencesUtil

class SetupControllerReadyFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var helpDlg: SetupControllerReadyHelpDialog
    private lateinit var db: DatabaseCloudUtil

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_controller_ready, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
        db = DatabaseCloudUtil.getInstance(context!!)!!

        //if(TUTKP2PBaseApi.initIOTCRDT() >= 0)
            //TUTKP2PBaseApi.startSession("EZPAA13CVHRC9HPGY1WJ")

        //GlobalBus.publish(MainEvent.ShowHintLoading("test"))
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

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            setup_controller_ready_help_image ->
            {
                helpDlg = SetupControllerReadyHelpDialog(activity!!)
                helpDlg.show()
                //dbTest()
                //p2pTest()
            }

            //setup_controller_ready_next_image -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectControllerFragment()))
            setup_controller_ready_next_image ->
            {
                val bundle = Bundle().apply{
                    putBoolean("isInSetupFlow", false)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudLoginFragment().apply{ arguments = bundle }))
            }
        }
    }

    private fun setClickListener()
    {
        setup_controller_ready_help_image.setOnClickListener(clickListener)
        setup_controller_ready_next_image.setOnClickListener(clickListener)
    }

    private fun dbTest()
    {
        //var db = DatabaseCloudUtil.getInstance(context!!)!!

        var test1 = DatabaseSiteInfoEntity(
                "aaa",
                "AAA",
                "A",
                "xxx/xx1",
                "WA",
                "WAP",
                true
        )

        var test2 = DatabaseSiteInfoEntity(
                "bbb",
                "BBB",
                "B",
                "xxx/xx2",
                "WB",
                "WBP",
                false
        )

        var test3 = DatabaseSiteInfoEntity(
                "ccc",
                "CCC",
                "C",
                "xxx/xx3",
                "WC",
                "WCP",
                true
        )

        var test4 = DatabaseSiteInfoEntity(
                "ddd",
                "DDD",
                "D",
                "xxx/xx3",
                "WC",
                "WCP",
                true
        )

        var test5 = DatabaseSiteInfoEntity(
                "eee",
                "EEE",
                "E",
                "xxx/xx3",
                "WC",
                "WCP",
                true
        )

        var test6 = DatabaseSiteInfoEntity(
                "bbb",
                "FFF",
                "F",
                "xxx/xx333333",
                "WC",
                "WCP",
                true
        )


        var client1 = DatabaseClientListEntity(
                "aaa",
                "eee",
                "NE"
        )

        var client2 = DatabaseClientListEntity(
                "aaa",
                "fff",
                "NF"
        )

        var client3 = DatabaseClientListEntity(
                "ccc",
                "ggg",
                "NG"
        )

        var client4 = DatabaseClientListEntity(
                "ccc",
                "hhh",
                "NH"
        )

        var client5 = DatabaseClientListEntity(
                "ccc",
                "iii",
                "NI"
        )

        var client6 = DatabaseClientListEntity(
                "ccc",
                "jjj",
                "NJ"
        )

        var client7 = DatabaseClientListEntity(
                "ccc",
                "kkk",
                "NK"
        )

        doAsync{
            db.getSiteInfoDao().insert(test1)
            db.getSiteInfoDao().insert(test2)
            db.getSiteInfoDao().insert(test3)
            db.getSiteInfoDao().insert(test4)
            db.getSiteInfoDao().insert(test5)
            db.getSiteInfoDao().insert(test6)
            db.getClientListDao().insert(client1)
            db.getClientListDao().insert(client2)
            db.getClientListDao().insert(client3)
            db.getClientListDao().insert(client4)
            db.getClientListDao().insert(client5)
            db.getClientListDao().insert(client6)
            db.getClientListDao().insert(client7)

            var array: List<DatabaseSiteInfoEntity> = ArrayList()
            array = db.getSiteInfoDao().getAll()
            LogUtil.d(TAG,"[LOUIS]isEmpty:${array.isEmpty()}")
            for(item in array)
            {
                LogUtil.d(TAG,"[LOUIS]mac:${item.mac}")
                LogUtil.d(TAG,"[LOUIS]siteName:${item.siteName}")
                LogUtil.d(TAG,"[LOUIS]sitePicPath:${item.sitePicPath}")
                LogUtil.d(TAG,"[LOUIS]wifiSSID:${item.wifiSSID}")
                LogUtil.d(TAG,"[LOUIS]wifiPWD:${item.wifiPWD}")
                LogUtil.d(TAG,"[LOUIS]backup:${item.backup}")
                LogUtil.d(TAG,"[LOUIS]reserveOne:${item.reserveOne}")
                LogUtil.d(TAG,"[LOUIS]reserveTwo:${item.reserveTwo}")
                LogUtil.d(TAG,"[LOUIS]reserveThree:${item.reserveThree}")
                LogUtil.d(TAG,"[LOUIS]-------------------------------------")
            }

            var array2: List<DatabaseClientListEntity> = ArrayList()
            array2 = db.getClientListDao().queryByMac("ccc")
            LogUtil.d(TAG,"[LOUIS]2isEmpty:${array2.isEmpty()}")
            for(item in array2)
            {
                LogUtil.d(TAG,"[LOUIS][2]mac:${item.mac}")
                LogUtil.d(TAG,"[LOUIS][2]deviceMac:${item.deviceMac}")
                LogUtil.d(TAG,"[LOUIS][2]deviceName:${item.deviceName}")
                LogUtil.d(TAG,"[LOUIS][2]reserveOne:${item.reserveOne}")
                LogUtil.d(TAG,"[LOUIS][2]reserveTwo:${item.reserveTwo}")
                LogUtil.d(TAG,"[LOUIS][2]reserveThree:${item.reserveThree}")
                LogUtil.d(TAG,"[LOUIS]-------------------------------------")
            }

            var info = DatabaseSiteInfoEntity()
            info = db.getSiteInfoDao().queryByMac("zzz")
            if(info == null)
                LogUtil.e(TAG,"mac : zzz return NULL!!!!!")
            else
            {
                LogUtil.d(TAG,"[LOUIS1]mac:${info.mac}")
                LogUtil.d(TAG,"[LOUIS1]siteName:${info.siteName}")
                LogUtil.d(TAG,"[LOUIS1]sitePicPath:${info.sitePicPath}")
                LogUtil.d(TAG,"[LOUIS1]wifiSSID:${info.wifiSSID}")
                LogUtil.d(TAG,"[LOUIS1]wifiPWD:${info.wifiPWD}")
                LogUtil.d(TAG,"[LOUIS1]backup:${info.backup}")
                LogUtil.d(TAG,"[LOUIS1]-------------------------------------")
            }

            var clientInfoList = db.getClientListDao().queryByMac("ZZZ")
            if(clientInfoList == null)
            {
                LogUtil.e(TAG,"clientInfoList is NULL!!")
            }

            if(clientInfoList.isEmpty())
            {
                LogUtil.e(TAG,"clientInfoList is EMPTY!!")
            }

            for(item in clientInfoList)
            {
                LogUtil.e(TAG,"clientInfoList mac:${item.mac}")
                LogUtil.e(TAG,"clientInfoList deviceMac:${item.deviceMac}")
                LogUtil.e(TAG,"clientInfoList deviceName:${item.deviceName}")
            }

            LogUtil.e(TAG,"clientInfoList size:${clientInfoList.size}")
        }
    }

    fun p2pTest()
    {
        if(TUTKP2PBaseApi.initIOTCRDT() >= 0)
            TUTKP2PBaseApi.startSession("E7KA952WU5RMUH6GY1CJ")

        P2PGatewayApi.GetSystemInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"responseStr:$responseStr")
                    }
                }).execute()
    }

    fun p2pTest2()
    {
        P2PDevicesApi.GetDevicesInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"responseStr:$responseStr")

                        //var devicesInfo = Gson().fromJson(responseStr, DevicesInfo::class.javaObjectType)
                        //LogUtil.d(TAG,"devicesInfo:$devicesInfo")
                    }
                }).execute()
    }

    fun p2pTest3()
    {
        P2PGatewayApi.GetSystemInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"responseStr:$responseStr")

                        //var devicesInfo = Gson().fromJson(responseStr, DevicesInfo::class.javaObjectType)
                        //LogUtil.d(TAG,"devicesInfo:$devicesInfo")
                    }
                }).execute()
    }
}