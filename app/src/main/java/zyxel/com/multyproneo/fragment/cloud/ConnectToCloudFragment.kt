package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_connect_to_cloud.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent

class ConnectToCloudFragment : Fragment()
{
    private var isInSetupFlow = true
    private var needLoginWhenFinal = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_connect_to_cloud, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("isInSetupFlow", true)?.let{ isInSetupFlow = it }
            this?.getBoolean("needLoginWhenFinal", false)?.let{ needLoginWhenFinal = it }
        }

        connect_to_cloud_continue_image.onClick{
            val bundle = Bundle().apply{
                putBoolean("isInSetupFlow", isInSetupFlow)
                putBoolean("needLoginWhenFinal", needLoginWhenFinal)
            }
            GlobalBus.publish(MainEvent.SwitchToFrag(CloudLoginFragment().apply{ arguments = bundle }))
        }

        runOnUiThread{
            connect_to_cloud_content_animation_view.setAnimation("ConnectToTheCloud_oldJson.json")
            connect_to_cloud_content_animation_view.playAnimation()
        }
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }
}