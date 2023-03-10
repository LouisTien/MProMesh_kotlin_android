package zyxel.com.multyproneo.tool

import android.content.Context

/**
 * Created by LouisTien on 2019/7/31.
 */
object DensityTool
{
    fun dip2px(context: Context, dpValue: Float): Int
    {
        val scale = context.getResources().getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int
    {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }
}