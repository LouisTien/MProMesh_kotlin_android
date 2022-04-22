package zyxel.com.multyproneo.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_parental_control_setup_schedule.*
import org.jetbrains.anko.imageResource
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
    private var startHour = 0
    private var startMin = 0
    private var endHour = 0
    private var endMin = 0
    private val numValues = 60 / AppConfig.NUM_PIC_INTERVAL
    private var displayedValues = arrayOfNulls<String>(numValues)


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_parental_control_setup_schedule)
        setCancelable(true)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        for(i in 0 until numValues)
            displayedValues[i] = String.format("%02d", (i * AppConfig.NUM_PIC_INTERVAL))

        updateUI()
        setListener()
    }

    private fun updateUI()
    {
        start_hour.maxValue = 23
        start_hour.minValue = 0
        start_min.maxValue = 3
        start_min.minValue = 0
        end_hour.maxValue = 24
        end_hour.minValue = 0
        end_min.maxValue = 3
        end_min.minValue = 0

        start_hour.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        start_hour.setFormatter { i -> String.format("%02d", i) }
        start_min.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        start_min.displayedValues = displayedValues
        end_hour.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        end_hour.setFormatter { i -> String.format("%02d", i) }
        end_min.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        end_min.displayedValues = displayedValues

        start_hour.value = scheduleInfo.TimeStartHour
        startHour = scheduleInfo.TimeStartHour
        start_min.value = scheduleInfo.TimeStartMin / AppConfig.NUM_PIC_INTERVAL
        startMin = (scheduleInfo.TimeStartMin / AppConfig.NUM_PIC_INTERVAL) * AppConfig.NUM_PIC_INTERVAL //for other value handle, to make the value in displayedValues array range
        end_hour.value = scheduleInfo.TimeStopHour
        endHour = scheduleInfo.TimeStopHour
        end_min.value = scheduleInfo.TimeStopMin / AppConfig.NUM_PIC_INTERVAL
        endMin = (scheduleInfo.TimeStopMin / AppConfig.NUM_PIC_INTERVAL) * AppConfig.NUM_PIC_INTERVAL

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
                checkSaveStatus()
            }

            parental_control_setup_schedule_tue_select_image ->
            {
                with(parental_control_setup_schedule_tue_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                checkSaveStatus()
            }

            parental_control_setup_schedule_wed_select_image ->
            {
                with(parental_control_setup_schedule_wed_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                checkSaveStatus()
            }

            parental_control_setup_schedule_thu_select_image ->
            {
                with(parental_control_setup_schedule_thu_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                checkSaveStatus()
            }

            parental_control_setup_schedule_fri_select_image ->
            {
                with(parental_control_setup_schedule_fri_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                checkSaveStatus()
            }

            parental_control_setup_schedule_sat_select_image ->
            {
                with(parental_control_setup_schedule_sat_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                checkSaveStatus()
            }

            parental_control_setup_schedule_sun_select_image ->
            {
                with(parental_control_setup_schedule_sun_select_image)
                {
                    tag = if(tag == 0) 1 else 0
                    imageResource = if(tag == 0) R.drawable.select_icon else R.drawable.selected_icon
                }
                checkSaveStatus()
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

        start_hour.setOnValueChangedListener { _, _, newVal ->
            startHour = newVal
            checkSaveStatus()
        }

        start_min.setOnValueChangedListener { _, _, newVal ->
            startMin = newVal * AppConfig.NUM_PIC_INTERVAL
            checkSaveStatus()
        }

        end_hour.setOnValueChangedListener { _, _, newVal ->
            endHour = newVal
            if(endHour == 24)
            {
                end_min.value = 0
                endMin = 0
            }
            checkSaveStatus()
        }

        end_min.setOnValueChangedListener { _, _, newVal ->
            endMin = newVal * AppConfig.NUM_PIC_INTERVAL
            if(endHour == 24)
            {
                end_min.value = 0
                endMin = 0
            }
            checkSaveStatus()
        }
    }

    private fun checkSaveStatus()
    {
        if(endHour < startHour)
            disableSaveButton()
        else if(endHour > startHour)
            enableSaveButton()
        else //endHour = startHour
        {
            if(endMin > startMin)
                enableSaveButton()
            else
                disableSaveButton()
        }

        if(parental_control_setup_schedule_sun_select_image.tag == 0
            && parental_control_setup_schedule_mon_select_image.tag == 0
            && parental_control_setup_schedule_tue_select_image.tag == 0
            && parental_control_setup_schedule_wed_select_image.tag == 0
            && parental_control_setup_schedule_thu_select_image.tag == 0
            && parental_control_setup_schedule_fri_select_image.tag == 0
            && parental_control_setup_schedule_sat_select_image.tag == 0)
            disableSaveButton()
    }

    private fun enableSaveButton()
    {
        parental_control_schedule_save_text.alpha = 1f
        parental_control_schedule_save_text.isEnabled = true
    }

    private fun disableSaveButton()
    {
        parental_control_schedule_save_text.alpha = 0.3f
        parental_control_schedule_save_text.isEnabled = false
    }
}