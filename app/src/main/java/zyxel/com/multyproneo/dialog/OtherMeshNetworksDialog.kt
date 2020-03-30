package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_other_mesh_netowrks.*
import org.jetbrains.anko.doAsync
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudOtherMeshNetworksItemAdapter
import zyxel.com.multyproneo.api.cloud.TUTKP2PBaseApi
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.cloud.CloudHomeFragment
import zyxel.com.multyproneo.fragment.cloud.SetupConnectTroubleshootingFragment
import zyxel.com.multyproneo.fragment.cloud.SetupControllerReadyFragment
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.util.AppConfig

class OtherMeshNetworksDialog(context: Context, private var siteName: String, private var gatewayListInfo: TUTKAllDeviceInfo) : Dialog(context)
{
    private lateinit var siteSelectedDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_other_mesh_netowrks)
        setCancelable(false)

        Glide.with(context).load(R.drawable.img_locationdefault).apply(RequestOptions.circleCropTransform()).into(other_mesh_main_mesh_image)

        other_mesh_main_mesh_text.text = siteName

        other_mesh_content_list.adapter = CloudOtherMeshNetworksItemAdapter(gatewayListInfo)

        other_mesh_close_image.setOnClickListener{ dismiss() }

        other_mesh_add_image.setOnClickListener{
            dismiss()
            GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))
        }

        siteSelectedDisposable = GlobalBus.listen(DialogEvent.OnOtherSiteSelect::class.java).subscribe{
            doAsync{
                GlobalBus.publish(MainEvent.ShowLoading())

                TUTKP2PBaseApi.stopSession()
                if(TUTKP2PBaseApi.initIOTCRDT() >= 0)
                {
                    if(TUTKP2PBaseApi.startSession(it.uid) >= 0)
                    {
                        dismiss()
                        GlobalBus.publish(MainEvent.SwitchToFrag(CloudHomeFragment()))
                    }
                    else
                    {
                        dismiss()
                        gotoTroubleShooting()
                    }
                }
                else
                {
                    dismiss()
                    gotoTroubleShooting()
                }
            }
        }
    }

    override fun dismiss()
    {
        super.dismiss()
        if(!siteSelectedDisposable.isDisposed) siteSelectedDisposable.dispose()
    }

    private fun gotoTroubleShooting()
    {
        GlobalBus.publish(MainEvent.HideLoading())

        val bundle = Bundle().apply{
            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CLOUD_API_ERROR)
        }

        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
    }
}