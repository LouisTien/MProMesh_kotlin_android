package zyxel.com.multyproneo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import android.view.View
import android.view.WindowManager
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_welcome.*
import org.jetbrains.anko.startActivity
import zyxel.com.multyproneo.adapter.WelcomeAdapter
import zyxel.com.multyproneo.tool.OnClearFromRecentService
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.SharedPreferencesUtil

/**
 * Created by LouisTien on 2019/5/21.
 */
class WelcomeActivity : AppCompatActivity()
{
    private val welcomeAdapter = WelcomeAdapter(this)
    private var firstTimeUse by SharedPreferencesUtil(this, AppConfig.SHAREDPREF_FIRST_TIME_KEY, true)

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        startService(Intent(baseContext, OnClearFromRecentService::class.java))

        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(AppConfig.NoUploadFabric).build())
                .build()

        Fabric.with(this, crashlyticsKit)

        setContentView(R.layout.activity_welcome)

        welcome_view_pager.adapter = welcomeAdapter
        welcome_view_pager.offscreenPageLimit = 3
        welcome_view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int)
            {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
            {
            }

            override fun onPageSelected(position: Int)
            {
                when(position)
                {
                    0 ->
                    {
                        welcome_left_item_linear.visibility = View.INVISIBLE
                        welcome_right_item_relative.visibility = View.VISIBLE
                        welcome_arrow_image.visibility = View.VISIBLE
                        welcome_right_start_text.visibility = View.INVISIBLE
                    }

                    1 ->
                    {
                        welcome_left_item_linear.visibility = View.VISIBLE
                        welcome_right_item_relative.visibility = View.VISIBLE
                        welcome_arrow_image.visibility = View.VISIBLE
                        welcome_right_start_text.visibility = View.INVISIBLE
                    }

                    2 ->
                    {
                        welcome_left_item_linear.visibility = View.INVISIBLE
                        welcome_right_item_relative.visibility = View.VISIBLE
                        welcome_arrow_image.visibility = View.INVISIBLE
                        welcome_right_start_text.visibility = View.VISIBLE
                    }

                    else ->
                    {
                    }
                }
            }
        })

        welcome_arrow_image.setOnClickListener{
            when(welcome_view_pager.currentItem)
            {
                0 -> welcome_view_pager.currentItem = 1
                1 -> welcome_view_pager.currentItem = 2
                else ->{}
            }}
        welcome_right_start_text.setOnClickListener{ launch() }
        welcome_left_start_text.setOnClickListener{ launch() }

        Handler().postDelayed({ startMainActivity() }, AppConfig.WELCOME_DISPLAY_TIME_IN_MILLISECONDS)

    }

    override fun onResume()
    {
        super.onResume()
        window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun startMainActivity()
    {
        if(firstTimeUse)
        {
            launcher_linear.visibility = View.INVISIBLE
            welcome_view_pager_linear.visibility = View.VISIBLE
            welcome_view_pager.adapter = welcomeAdapter
            welcome_view_pager_indicator.attachTo(welcome_view_pager)
            welcome_left_item_linear.visibility = View.INVISIBLE
            welcome_right_item_relative.visibility = View.VISIBLE
            welcome_arrow_image.visibility = View.VISIBLE
            welcome_right_start_text.visibility = View.INVISIBLE
        }
        else
            launch()
    }

    private fun launch()
    {
        firstTimeUse = false
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        startActivity<MainActivity>()
        finish()
    }
}