package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_setup_controller_ready.*
import org.jetbrains.anko.doAsync
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.database.room.DatabaseClientListEntity
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.dialog.SetupControllerReadyHelpDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.LogUtil

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
                //helpDlg = SetupControllerReadyHelpDialog(activity!!)
                //helpDlg.show()
                dbTest()
            }

            setup_controller_ready_next_image -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectControllerFragment()))
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
                1,
                "aaa",
                "A",
                "xxx/xx1",
                "WA",
                "WAP",
                true
        )

        var test2 = DatabaseSiteInfoEntity(
                2,
                "bbb",
                "B",
                "xxx/xx2",
                "WB",
                "WBP",
                false
        )

        var test3 = DatabaseSiteInfoEntity(
                3,
                "ccc",
                "C",
                "xxx/xx3",
                "WC",
                "WCP",
                true
        )

        var test4 = DatabaseSiteInfoEntity(
                4,
                "ccc",
                "D",
                "xxx/xx3",
                "WC",
                "WCP",
                true
        )

        var test5 = DatabaseSiteInfoEntity(
                5,
                "ccc",
                "E",
                "xxx/xx3",
                "WC",
                "WCP",
                true
        )

        var test6 = DatabaseSiteInfoEntity(
                6,
                "ccc",
                "F",
                "xxx/xx3",
                "WC",
                "WCP",
                true
        )


        var client1 = DatabaseClientListEntity(
                1,
                "aaa",
                "eee",
                "NE"
        )

        var client2 = DatabaseClientListEntity(
                2,
                "aaa",
                "fff",
                "NF"
        )

        var client3 = DatabaseClientListEntity(
                3,
                "ccc",
                "ggg",
                "NG"
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

            var array: List<DatabaseSiteInfoEntity> = ArrayList()
            array = db.getSiteInfoDao().getAll()
            for(item in array)
            {
                LogUtil.d(TAG,"[LOUIS]ID:${item.id}")
                LogUtil.d(TAG,"[LOUIS]mac:${item.mac}")
                LogUtil.d(TAG,"[LOUIS]siteName:${item.siteName}")
                LogUtil.d(TAG,"[LOUIS]sitePicPath:${item.sitePicPath}")
                LogUtil.d(TAG,"[LOUIS]wifiSSID:${item.wifiSSID}")
                LogUtil.d(TAG,"[LOUIS]wifiPWD:${item.wifiPWD}")
                LogUtil.d(TAG,"[LOUIS]backup:${item.backup}")
                LogUtil.d(TAG,"[LOUIS]-------------------------------------")
            }

            var array2: List<DatabaseClientListEntity> = ArrayList()
            array2 = db.getClientListDao().queryByMac("aaa")
            for(item in array2)
            {
                LogUtil.d(TAG,"[LOUIS][2]ID:${item.id}")
                LogUtil.d(TAG,"[LOUIS][2]mac:${item.mac}")
                LogUtil.d(TAG,"[LOUIS][2]deviceMac:${item.deviceMac}")
                LogUtil.d(TAG,"[LOUIS][2]deviceName:${item.deviceName}")
                LogUtil.d(TAG,"[LOUIS]-------------------------------------")
            }
        }
    }
}