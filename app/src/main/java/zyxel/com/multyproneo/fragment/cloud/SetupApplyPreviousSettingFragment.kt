package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_apply_previous_setting.*
import org.jetbrains.anko.doAsync
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.dialog.SlidePreviousSettingNameDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.LogUtil

class SetupApplyPreviousSettingFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var db: DatabaseCloudUtil
    private lateinit var dbList: List<DatabaseSiteInfoEntity>
    private lateinit var msgSlideDialogResponse: Disposable
    private lateinit var nameSlideDialog: SlidePreviousSettingNameDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_apply_previous_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
        db = DatabaseCloudUtil.getInstance(context!!)!!

        doAsync{
            dbList = db.getSiteInfoDao().getAll()
        }

        msgSlideDialogResponse = GlobalBus.listen(DialogEvent.OnSlideListSelect::class.java).subscribe{
            LogUtil.d(TAG, "RESPONSE:${it.data}")
            nameSlideDialog.dismiss()
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
            setup_previous_setting_check_image -> {}

            setup_previous_setting_details_text -> {}

            setup_previous_setting_skip_image -> {}

            setup_previous_setting_apply_image -> {}

            setup_previous_setting_detail_image -> {
                nameSlideDialog = SlidePreviousSettingNameDialog(activity!!, dbList, 0)
                nameSlideDialog.show()
            }
        }
    }

    private fun setClickListener()
    {
        setup_previous_setting_check_image.setOnClickListener(clickListener)
        setup_previous_setting_details_text.setOnClickListener(clickListener)
        setup_previous_setting_skip_image.setOnClickListener(clickListener)
        setup_previous_setting_apply_image.setOnClickListener(clickListener)
        setup_previous_setting_detail_image.setOnClickListener(clickListener)
    }
}