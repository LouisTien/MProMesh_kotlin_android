package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_apply_previous_settings.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.dialog.SetupPreviousSettingsDetailsDialog
import zyxel.com.multyproneo.dialog.SetupSlidePreviousSettingsNameDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class SetupApplyPreviousSettingsFragment : Fragment()
{
    private val TAG = "SetupApplyPreviousSettingsFragment"
    private lateinit var db: DatabaseCloudUtil
    private lateinit var currentEntity: DatabaseSiteInfoEntity
    private lateinit var siteInfoList: List<DatabaseSiteInfoEntity>
    private lateinit var msgSlideDialogResponse: Disposable
    private lateinit var nameSlideDialog: SetupSlidePreviousSettingsNameDialog
    private lateinit var detailsDialog: SetupPreviousSettingsDetailsDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_apply_previous_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        setClickListener()
        db = DatabaseCloudUtil.getInstance(context!!)!!

        msgSlideDialogResponse = GlobalBus.listen(DialogEvent.OnSlideListSelect::class.java).subscribe{
            LogUtil.d(TAG, "RESPONSE:${it.data}")
            currentEntity = it.data
            updateUI()
            nameSlideDialog.dismiss()
        }

        doAsync{
            GlobalBus.publish(MainEvent.ShowLoading())

            siteInfoList = db.getSiteInfoDao().queryByBackup(true)

            if(siteInfoList.isNotEmpty())
                currentEntity = siteInfoList[0]
            else
                currentEntity = DatabaseSiteInfoEntity()

            uiThread{
                updateUI()
                GlobalBus.publish(MainEvent.HideLoading())
            }
        }
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
        if(!msgSlideDialogResponse.isDisposed) msgSlideDialogResponse.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            setup_previous_settings_details_text ->
            {
                detailsDialog = SetupPreviousSettingsDetailsDialog(activity!!, currentEntity.mac)
                detailsDialog.show()
            }

            setup_previous_settings_skip_image -> { GlobalBus.publish(MainEvent.SwitchToFrag(CloudLoginFragment())) }

            setup_previous_settings_apply_image ->
            {
                val bundle = Bundle().apply{
                    putString("MAC", currentEntity.mac)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(SetupApplyingPreviousSettingsFragment().apply{ arguments = bundle }))
            }

            setup_previous_settings_detail_image ->
            {
                nameSlideDialog = SetupSlidePreviousSettingsNameDialog(activity!!, siteInfoList, getIndexInSiteInfoList())
                nameSlideDialog.show()
            }
        }
    }

    private fun setClickListener()
    {
        setup_previous_settings_details_text.setOnClickListener(clickListener)
        setup_previous_settings_skip_image.setOnClickListener(clickListener)
        setup_previous_settings_apply_image.setOnClickListener(clickListener)
        setup_previous_settings_detail_image.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        setup_previous_settings_name_text.text = currentEntity.siteName
    }

    private fun getIndexInSiteInfoList(): Int
    {
        var ret = 0

        if(siteInfoList.isNotEmpty())
        {
            for(i in 0 until siteInfoList.size)
            {
                if(currentEntity.mac == siteInfoList[i].mac)
                {
                    ret = i
                    break
                }
            }
        }

        return ret
    }
}