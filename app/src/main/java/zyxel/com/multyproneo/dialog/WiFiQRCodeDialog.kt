package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.dialog_wifi_qrcode.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.WiFiQRCodeAdapter

class WiFiQRCodeDialog(context: Context, private var bmpArrayList: ArrayList<Bitmap>) : Dialog(context)
{
    private val qrcodeAdapter = WiFiQRCodeAdapter(context, bmpArrayList)

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_wifi_qrcode)
        setCancelable(true)
        wifi_qrcode_pager.adapter = qrcodeAdapter
        wifi_qrcode_pager.offscreenPageLimit = bmpArrayList.size
        wifi_qrcode_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int)
            {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
            {
            }

            override fun onPageSelected(position: Int)
            {
            }

        })
        if(bmpArrayList.size == 1) wifi_qrcode_pager_indicator.visibility = View.INVISIBLE
        wifi_qrcode_pager_indicator.attachTo(wifi_qrcode_pager)
        wifi_qrcode_positive_text.setOnClickListener{ dismiss() }
    }
}