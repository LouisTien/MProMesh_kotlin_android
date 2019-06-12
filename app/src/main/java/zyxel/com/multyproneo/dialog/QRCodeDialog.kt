package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_qrcode.*
import zyxel.com.multyproneo.R

/**
 * Created by LouisTien on 2019/6/12.
 */
class QRCodeDialog(context: Context, private var msg: String, private var QRCodeBitmap: Bitmap) : Dialog(context)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_qrcode)
        setCancelable(true)
        QRCode_dialog_positive_text.setOnClickListener{ dismiss() }
    }

    override fun show()
    {
        super.show()
        QRCode_dialog_QRCode_image.setImageBitmap(QRCodeBitmap)
        QRCode_dialog_msg_text.text = msg
    }
}