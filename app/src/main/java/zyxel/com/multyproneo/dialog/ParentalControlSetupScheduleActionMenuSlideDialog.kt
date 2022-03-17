package zyxel.com.multyproneo.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_parental_control_schedule_action_menu.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.ParentalControlEvent
import zyxel.com.multyproneo.model.ParentalControlInfoSchedule
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.util.AppConfig

class ParentalControlSetupScheduleActionMenuSlideDialog
(
        context: Context,
        var index: Int,
        var scheduleInfo: ParentalControlInfoSchedule,
        var onlyEdit: Boolean
) : BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_parental_control_schedule_action_menu)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setCancelable(true)

        val formatStr = "%02d"
        val timeStr = String.format(formatStr, scheduleInfo.TimeStartHour) + ":" +
                String.format(formatStr, scheduleInfo.TimeStartMin) + " - " +
                String.format(formatStr, scheduleInfo.TimeStopHour) + ":" +
                String.format(formatStr, scheduleInfo.TimeStopMin)

        val weekStr = CommonTool.getSelectDays(scheduleInfo)
        parental_control_schedule_action_name_text.text = "$timeStr $weekStr"

        if(onlyEdit)
        {
            parental_control_schedule_action_delete_relative.visibility = View.GONE
            parental_control_schedule_action_delete_line_image.visibility = View.GONE
        }

        parental_control_schedule_action_edit_relative.onClick{
            dismiss()
            GlobalBus.publish(ParentalControlEvent.ScheduleAct(AppConfig.ScheduleAction.ACT_EDIT, index, scheduleInfo))
        }

        parental_control_schedule_action_delete_relative.onClick{
            dismiss()
            GlobalBus.publish(ParentalControlEvent.ScheduleAct(AppConfig.ScheduleAction.ACT_DELETE, index, scheduleInfo))
        }
    }
}