package zyxel.com.multyproneo.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.adapter_wifi_qrcode_item.view.*
import zyxel.com.multyproneo.R

class WiFiQRCodeAdapter(private var context: Context, private var bmpArrayList: ArrayList<Bitmap>) : PagerAdapter()
{
    private lateinit var inflator: LayoutInflater

    override fun instantiateItem(container: ViewGroup, position: Int): Any
    {
        inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView: View = inflator.inflate(R.layout.adapter_wifi_qrcode_item, container,false)
        itemView.wifi_qrcode_content_image.setImageBitmap(bmpArrayList[position])
        when(position)
        {
            0 -> itemView.wifi_qrcode_title_text.text = context.getString(if(bmpArrayList.size >= 3) R.string.wifi_settings_wifi_name_24g_qrcode else R.string.wifi_settings_wifi_name_qrcode)
            1 -> itemView.wifi_qrcode_title_text.text = context.getString(if(bmpArrayList.size >= 3) R.string.wifi_settings_wifi_name_5g_qrcode else R.string.wifi_settings_guest_wifi_qrcode)
            2 -> itemView.wifi_qrcode_title_text.text = context.getString(R.string.wifi_settings_guest_wifi_qrcode)
            else -> {}
        }
        container.addView(itemView)
        return itemView
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) = container.removeView(`object` as View)

    override fun getCount(): Int = bmpArrayList.size
}