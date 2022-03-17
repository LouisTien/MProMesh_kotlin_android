package zyxel.com.multyproneo.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_parental_control_setup_schedule.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.ParentalControlEvent
import zyxel.com.multyproneo.model.ParentalControlInfoSchedule
import zyxel.com.multyproneo.util.AppConfig

class ParentalControlSetupScheduleSlideDialog
(
        context: Context,
        private val action: AppConfig.ScheduleAction = AppConfig.ScheduleAction.ACT_ADD,
        private val index: Int = 0,
        private val scheduleInfo: ParentalControlInfoSchedule = ParentalControlInfoSchedule(Days = "127")
) : BottomSheetDialog(context)
{
    var startHour = 0
    var startMin = 0
    var endHour = 0
    var endMin = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_parental_control_setup_schedule)
        setCancelable(true)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        updateUI()
        setListener()
    }

    private fun updateUI()
    {
        val daysInfo = scheduleInfo.GetDaysInfo()

        with(parental_control_setup_schedule_mon_select_image)
        {
            tag = if(daysInfo.mon) 1 else 0
            imageResource = if(daysInfo.mon) R.drawable.selected_icon else R.drawable.select_icon
        }

        with(parental_control_setup_schedule_tue_select_image)
        {
            tag = if(daysInfo.tue) 1 else 0
            imageResource = if(daysInfo.tue) R.drawable.selected_icon else R.drawable.select_icon
        }

        with(parental_control_setup_schedule_wed_select_image)
        {
            tag = if(daysInfo.wed) 1 else 0
            imageResource = if(daysInfo.wed) R.drawable.selected_icon else R.drawable.select_icon
        }

        with(parental_control_setup_schedule_thu_select_image)
        {
            tag = if(daysInfo.thu) 1 else 0
            imageResource = if(daysInfo.thu) R.drawable.selected_icon else R.drawable.select_icon
        }

        with(parental_control_setup_schedule_fri_select_image)
        {
            tag = if(daysInfo.fri) 1 else 0
            imageResource = if(daysInfo.fri) R.drawable.selected_icon else R.drawable.select_icon
        }

        with(parental_control_setup_schedule_sat_select_image)
        {
            tag = if(daysInfo.sat) 1 else 0
            imageResource = if(daysInfo.sat) R.drawable.selected_icon else R.drawable.select_icon
        }

        with(parental_control_setup_schedule_sun_select_image)
        {
            tag = if(daysInfo.sun) 1 else 0
            imageResource = if(daysInfo.sun) R.drawable.selected_icon else R.drawable.select_icon
        }

        parental_control_setup_schedule_start_block_time_hour_edit.setText(if(scheduleInfo.TimeStartHour == 0) "00" else scheduleInfo.TimeStartHour.toString())
        parental_control_setup_schedule_start_block_time_min_edit.setText(if(scheduleInfo.TimeStartMin == 0) "00" else scheduleInfo.TimeStartMin.toString())
        parental_control_setup_schedule_end_block_time_hour_edit.setText(if(scheduleInfo.TimeStopHour == 0) "00" else scheduleInfo.TimeStopHour.toString())
        parental_control_setup_schedule_end_block_time_min_edit.setText(if(scheduleInfo.TimeStopMin == 0) "00" else scheduleInfo.TimeStopMin.toString())

        checkSaveStatus()
    }

    private val clickListener = View.OnClickListener { view ->
        when(view)
        {
            parental_control_schedule_cancel_text -> dismiss()

            parental_control_schedule_save_text ->
            {
                dismiss()

                val scheduleInfo = ParentalControlInfoSchedule()
                val days = (if(parental_control_setup_schedule_sun_select_image.tag == 1) 1 else 0) +
                        (if(parental_control_setup_schedule_mon_select_image.tag == 1) 2 else 0) +
                        (if(parental_control_setup_schedule_tue_select_image.tag == 1) 4 else 0) +
                        (if(parental_control_setup_schedule_wed_select_image.tag == 1) 8 else 0) +
                        (if(parental_control_setup_schedule_thu_select_image.tag == 1) 16 else 0) +
                        (if(parental_control_setup_schedule_fri_select_image.tag == 1) 32 else 0) +
                        (if(parental_control_setup_schedule_sat_select_image.tag == 1) 64 else 0)

                scheduleInfo.Days = days.toString()
                scheduleInfo.TimeStartHour = startHour
                scheduleInfo.TimeStartMin = startMin
                scheduleInfo.TimeStopHour = endHour
                scheduleInfo.TimeStopMin = endMin

                GlobalBus.publish(ParentalControlEvent.ScheduleSet(action, index, scheduleInfo))
            }

            parental_control_setup_schedule_mon_select_image ->
            {
                with(parental_control_setup_schedule_mon_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                clearTimeEditFocus()
            }

            parental_control_setup_schedule_tue_select_image ->
            {
                with(parental_control_setup_schedule_tue_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                clearTimeEditFocus()
            }

            parental_control_setup_schedule_wed_select_image ->
            {
                with(parental_control_setup_schedule_wed_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                clearTimeEditFocus()
            }

            parental_control_setup_schedule_thu_select_image ->
            {
                with(parental_control_setup_schedule_thu_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                clearTimeEditFocus()
            }

            parental_control_setup_schedule_fri_select_image ->
            {
                with(parental_control_setup_schedule_fri_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
            }

            parental_control_setup_schedule_sat_select_image ->
            {
                with(parental_control_setup_schedule_sat_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                clearTimeEditFocus()
            }

            parental_control_setup_schedule_sun_select_image ->
            {
                with(parental_control_setup_schedule_sun_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                clearTimeEditFocus()
            }
        }
    }

    private val focusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
        when(view)
        {
            parental_control_setup_schedule_start_block_time_hour_edit,
            parental_control_setup_schedule_start_block_time_min_edit ->
            {
                if(hasFocus)
                {
                    //parental_control_schedule_start_time_linear.backgroundResource = R.drawable.corner_shape_blue_background_stroke
                    //parental_control_schedule_end_time_linear.backgroundResource = R.drawable.corner_shape_gray_background
                }
            }

            parental_control_setup_schedule_end_block_time_hour_edit,
            parental_control_setup_schedule_end_block_time_min_edit ->
            {
                if(hasFocus)
                {
                    //parental_control_schedule_start_time_linear.backgroundResource = R.drawable.corner_shape_gray_background
                    //parental_control_schedule_end_time_linear.backgroundResource = R.drawable.corner_shape_blue_background_stroke
                }
            }
        }
    }

    private fun setListener()
    {
        parental_control_schedule_cancel_text.setOnClickListener(clickListener)
        parental_control_schedule_save_text.setOnClickListener(clickListener)
        parental_control_setup_schedule_mon_select_image.setOnClickListener(clickListener)
        parental_control_setup_schedule_tue_select_image.setOnClickListener(clickListener)
        parental_control_setup_schedule_wed_select_image.setOnClickListener(clickListener)
        parental_control_setup_schedule_thu_select_image.setOnClickListener(clickListener)
        parental_control_setup_schedule_fri_select_image.setOnClickListener(clickListener)
        parental_control_setup_schedule_sat_select_image.setOnClickListener(clickListener)
        parental_control_setup_schedule_sun_select_image.setOnClickListener(clickListener)

        parental_control_setup_schedule_start_block_time_hour_edit.onFocusChangeListener = focusChangeListener
        parental_control_setup_schedule_start_block_time_min_edit.onFocusChangeListener = focusChangeListener
        parental_control_setup_schedule_end_block_time_hour_edit.onFocusChangeListener = focusChangeListener
        parental_control_setup_schedule_end_block_time_min_edit.onFocusChangeListener = focusChangeListener

        parental_control_setup_schedule_start_block_time_hour_edit.textChangedListener {
            afterTextChanged {
                if(parental_control_setup_schedule_start_block_time_hour_edit.text.isNotEmpty())
                {
                    if(parental_control_setup_schedule_start_block_time_hour_edit.text.toString().toInt() > 23)
                        parental_control_setup_schedule_start_block_time_hour_edit.setText("23")
                }
                checkSaveStatus()
            }
        }

        parental_control_setup_schedule_start_block_time_min_edit.textChangedListener {
            afterTextChanged {
                if(parental_control_setup_schedule_start_block_time_min_edit.text.isNotEmpty())
                {
                    if(parental_control_setup_schedule_start_block_time_min_edit.text.toString().toInt() > 59)
                        parental_control_setup_schedule_start_block_time_min_edit.setText("59")
                }
                checkSaveStatus()
            }
        }

        parental_control_setup_schedule_end_block_time_hour_edit.textChangedListener {
            afterTextChanged {
                if(parental_control_setup_schedule_end_block_time_hour_edit.text.isNotEmpty())
                {
                    if(parental_control_setup_schedule_end_block_time_hour_edit.text.toString().toInt() > 23)
                        parental_control_setup_schedule_end_block_time_hour_edit.setText("23")
                }
                checkSaveStatus()
            }
        }

        parental_control_setup_schedule_end_block_time_min_edit.textChangedListener {
            afterTextChanged {
                if(parental_control_setup_schedule_end_block_time_min_edit.text.isNotEmpty())
                {
                    if(parental_control_setup_schedule_end_block_time_min_edit.text.toString().toInt() > 59)
                        parental_control_setup_schedule_end_block_time_min_edit.setText("59")
                }
                checkSaveStatus()
            }
        }
    }

    private fun checkSaveStatus()
    {
        startHour =
                if(parental_control_setup_schedule_start_block_time_hour_edit.text.isEmpty())
                    0
                else
                    parental_control_setup_schedule_start_block_time_hour_edit.text.toString().toInt()

        startMin =
                if(parental_control_setup_schedule_start_block_time_min_edit.text.isEmpty())
                    0
                else
                    parental_control_setup_schedule_start_block_time_min_edit.text.toString().toInt()

        endHour =
                if(parental_control_setup_schedule_end_block_time_hour_edit.text.isEmpty())
                    0
                else
                    parental_control_setup_schedule_end_block_time_hour_edit.text.toString().toInt()

        endMin =
                if(parental_control_setup_schedule_end_block_time_min_edit.text.isEmpty())
                    0
                else
                    parental_control_setup_schedule_end_block_time_min_edit.text.toString().toInt()

        if(endHour < startHour)
        {
            parental_control_schedule_save_text.alpha = 0.3f
            parental_control_schedule_save_text.isEnabled = false
        }
        else if(endHour > startHour)
        {
            parental_control_schedule_save_text.alpha = 1f
            parental_control_schedule_save_text.isEnabled = true
        }
        else //endHour = startHour
        {
            if(endMin > startMin)
            {
                parental_control_schedule_save_text.alpha = 1f
                parental_control_schedule_save_text.isEnabled = true
            }
            else
            {
                parental_control_schedule_save_text.alpha = 0.3f
                parental_control_schedule_save_text.isEnabled = false
            }
        }
    }

    private fun clearTimeEditFocus()
    {
        parental_control_setup_schedule_start_block_time_hour_edit.clearFocus()
        parental_control_setup_schedule_start_block_time_min_edit.clearFocus()
        parental_control_setup_schedule_end_block_time_hour_edit.clearFocus()
        parental_control_setup_schedule_end_block_time_min_edit.clearFocus()
        //parental_control_schedule_start_time_linear.backgroundResource = R.drawable.corner_shape_gray_background
        //parental_control_schedule_end_time_linear.backgroundResource = R.drawable.corner_shape_gray_background
    }
}