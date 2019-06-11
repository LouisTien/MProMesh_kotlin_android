package zyxel.com.multyproneo.tool

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by LouisTien on 2019/6/11.
 */
class CommonTool
{
    companion object
    {
        fun hideKeyboard(activity: Activity)
        {
            var imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            view = View(activity)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun formatData(dataFormat: String, timeStamp: Long): String = if(timeStamp.toInt() == 0) "" else SimpleDateFormat(dataFormat).format(Date(timeStamp * 1000))
    }
}