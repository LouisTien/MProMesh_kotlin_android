package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import kotlinx.android.synthetic.main.dialog_slide_previous_setting_name.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.SlideDialogPreviousSettingNameItemAdapter
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity

class SlidePreviousSettingNameDialog(context: Context, private var contentList: List<DatabaseSiteInfoEntity>, private var focusIndex: Int) : Dialog(context, R.style.slideDialogStyle)
{
    private lateinit var adapter: SlideDialogPreviousSettingNameItemAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_slide_previous_setting_name)
        setCancelable(true)

        this.window!!.attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        this.window!!.attributes.gravity = Gravity.BOTTOM
        this.window!!.attributes.windowAnimations = R.style.slideDialogAnimation

        adapter = SlideDialogPreviousSettingNameItemAdapter(contentList, focusIndex)

        slide_dialog_previous_setting_name_list.adapter = adapter

        slide_dialog_previous_setting_name_done_text.onClick{
            dismiss()
        }
    }
}