package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.dialog_previous_setting_details.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.database.room.DatabaseClientListEntity
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.LogUtil

class SetupPreviousSettingsDetailsDialog(context: Context, private var mac: String) : Dialog(context)
{
    private val TAG = javaClass.simpleName
    private lateinit var db: DatabaseCloudUtil
    private lateinit var siteInfo: DatabaseSiteInfoEntity
    private var clientList: List<DatabaseClientListEntity> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_previous_setting_details)
        setCancelable(true)

        db = DatabaseCloudUtil.getInstance(context)!!
        getDataFromDB()

        previous_settings_details_close_image.onClick{ dismiss() }
    }

    private fun getDataFromDB()
    {
        doAsync{
            siteInfo = db.getSiteInfoDao().queryByMac(mac)
            clientList = db.getClientListDao().queryByMac(mac)

            val nameList: ArrayList<String> = ArrayList()
            /*for(item in clientList)
                nameList.add(item.deviceName)*/

            val repeatMac: ArrayList<Int> = ArrayList()
            for(i in clientList.indices) {
                val macA = clientList[i].deviceMac
                nameList.add(clientList[i].deviceName)

                for(j in i+1 until clientList.size) {
                    val macB = clientList[j].deviceMac

                    if(CommonTool.checkIsTheSameDeviceMac(macA, macB)) {
                        LogUtil.d(TAG,"repeatMac:$macB")
                        repeatMac.add(j)
                        break
                    }
                }
            }

            if(repeatMac.isNotEmpty()) {
                for(item in repeatMac.sortedDescending()) {
                    nameList.removeAt(item)
                }
            }

            uiThread{
                val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, nameList)
                previous_settings_details_devices_list.adapter = adapter

                previous_settings_details_title_value_text.text = " '${siteInfo.siteName}'"
                previous_settings_details_wifi_network_name_value_text.text = siteInfo.wifiSSID
                previous_settings_details_wifi_password_value_text.text = siteInfo.wifiPWD
            }
        }
    }
}