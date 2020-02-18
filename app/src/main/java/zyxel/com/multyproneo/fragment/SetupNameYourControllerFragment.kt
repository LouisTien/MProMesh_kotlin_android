package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_setup_name_your_controller.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.LocationNamesItemAdapter
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.LocationNamesListEvent
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.GlobalData

class SetupNameYourControllerFragment : Fragment()
{
    private lateinit var getItemClickDisposable: Disposable
    private var adapter = LocationNamesItemAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_name_your_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        //setup_name_your_controller_location_edit.setText(GlobalData.getCurrentGatewayInfo().getName())

        GlobalData.locationNamesSelectIndex = -1

        setup_name_your_controller_location_name_list.adapter = adapter

        getItemClickDisposable = GlobalBus.listen(LocationNamesListEvent.OnDeviceSelected::class.java).subscribe{
            setup_name_your_controller_location_edit.text = null
            setup_name_your_controller_location_edit.setText(it.name)
            adapter.notifyDataSetChanged()
        }

        setup_name_your_controller_next_image.onClick{  }
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
        if(!getItemClickDisposable.isDisposed) getItemClickDisposable.dispose()
    }
}