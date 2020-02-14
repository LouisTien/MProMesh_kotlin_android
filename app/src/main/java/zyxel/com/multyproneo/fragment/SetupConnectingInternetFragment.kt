package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_setup_connecting_internet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.LogUtil
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class SetupConnectingInternetFragment : Fragment()
{
    private val TAG = javaClass.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_connecting_internet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        setup_connecting_internet_next_button.onClick{

        }

        startInternetCheckTask()
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

    private fun startInternetCheckTask()
    {
        doAsync{
            var result = false
            try
            {
                val urlc = URL(AppConfig.VERFY_INTERNET_DOMAIN_URL).openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "Test")
                urlc.setRequestProperty("Connection", "close")
                urlc.connectTimeout = 1500
                urlc.connect()
                if(urlc.responseCode == 200)
                {
                    result = true
                }
            }
            catch(e: IOException)
            {
                LogUtil.d(TAG, "Error checking internet connection = $e")
                result = false
            }

            try
            {
                Thread.sleep(1000)
            }
            catch(e: InterruptedException)
            {
                e.printStackTrace()
            }

            uiThread{
                when(result)
                {
                    true -> {
                        setup_connecting_internet_title_text.text = getString(R.string.setup_connecting_internet_success_title)
                        setup_connecting_internet_description_text.visibility = View.INVISIBLE
                        setup_connecting_internet_content_image.setImageResource(R.drawable.gif_connectiongtotheinternet_02)
                        setup_connecting_internet_next_button.visibility = View.VISIBLE
                    }

                    false ->
                    {
                        val bundle = Bundle().apply{
                            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_NO_INTERNET)
                        }

                        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
                    }
                }
            }
        }
    }
}