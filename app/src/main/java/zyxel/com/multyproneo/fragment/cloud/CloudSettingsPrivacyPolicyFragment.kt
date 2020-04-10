package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import android.webkit.WebSettings
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_settings_privacy_policy.*
import zyxel.com.multyproneo.R


class CloudSettingsPrivacyPolicyFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_settings_privacy_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        settings_privacy_policy_webView.settings.javaScriptEnabled = true
        settings_privacy_policy_webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings_privacy_policy_webView.loadUrl("https://www.zyxel.com/privacy_policy.shtml")

        settings_privacy_policy_back_image.setOnClickListener{
            GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsFragment()))
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