package zyxel.com.multyproneo.tool

import android.util.DisplayMetrics

/**
 * Created by LouisTien on 2019/6/24.
 */
class DensityPixelConverter
{
    fun convertDpToPixel(dp: Float, displayMetrics: DisplayMetrics): Float = dp * displayMetrics.density
    fun convertPixelToDp(px: Float, displayMetrics: DisplayMetrics): Float = px / displayMetrics.density
}