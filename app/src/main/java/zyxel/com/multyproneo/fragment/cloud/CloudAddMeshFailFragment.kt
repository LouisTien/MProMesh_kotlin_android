package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_cloud_add_mesh_fail.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.GlobalData

class CloudAddMeshFailFragment : Fragment()
{
    private val TAG = "CloudAddMeshFailFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_add_mesh_fail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        setClickListener()
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

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            cloud_mesh_fail_quit_text -> GlobalBus.publish(MainEvent.EnterCloudHomePage())

            cloud_mesh_fail_retry_image ->
            {
                val bundle = Bundle().apply{
                    putBoolean("fromAddMeshFailPage", true)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshWPSFragment().apply{ arguments = bundle }))
            }

            cloud_mesh_fail_help_text -> {}
        }
    }

    private fun setClickListener()
    {
        cloud_mesh_fail_quit_text.setOnClickListener(clickListener)
        cloud_mesh_fail_retry_image.setOnClickListener(clickListener)
        cloud_mesh_fail_help_text.setOnClickListener(clickListener)
    }
}