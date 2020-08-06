package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.dialog_qrcode.*
import zyxel.com.multyproneo.R

/**
 * Created by LouisTien on 2019/6/12.
 */
class QRCodeDialog(context: Context, private var msg: String, private var QRCodeBitmap: Bitmap) : Dialog(context)
{
    private lateinit var QRCodeBitmap5g: Bitmap
    private var supportTwoSSID = false

    constructor(context: Context, msg: String, QRCodeBitmap: Bitmap, QRCodeBitmap5g: Bitmap) : this(context, msg, QRCodeBitmap)
    {
        this.QRCodeBitmap5g = QRCodeBitmap5g
        this.supportTwoSSID = true
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_qrcode)
        setCancelable(true)
        initUI()
    }

    override fun show()
    {
        super.show()
        QRCode_dialog_QRCode_image.setImageBitmap(QRCodeBitmap)
        QRCode_dialog_msg_text.text = msg
    }

    private fun initUI()
    {
        if(supportTwoSSID)
            QRCode_dialog_title_linear.visibility = View.VISIBLE

        setClickListener()
    }

    private fun setClickListener()
    {
        QRCode_dialog_positive_text.setOnClickListener(clickListener)
        QRCode_dialog_title_24g_linear.setOnClickListener(clickListener)
        QRCode_dialog_title_5g_linear.setOnClickListener(clickListener)
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            QRCode_dialog_positive_text -> dismiss()

            QRCode_dialog_title_24g_linear ->
            {
                QRCode_dialog_title_24g_bottom_line_image.setBackgroundResource(R.color.color_ffc800)
                QRCode_dialog_title_5g_bottom_line_image.setBackgroundResource(R.color.color_888888)
                QRCode_dialog_QRCode_image.setImageBitmap(QRCodeBitmap)
            }

            QRCode_dialog_title_5g_linear ->
            {
                QRCode_dialog_title_24g_bottom_line_image.setBackgroundResource(R.color.color_888888)
                QRCode_dialog_title_5g_bottom_line_image.setBackgroundResource(R.color.color_ffc800)
                QRCode_dialog_QRCode_image.setImageBitmap(QRCodeBitmap5g)
            }
        }
    }
}