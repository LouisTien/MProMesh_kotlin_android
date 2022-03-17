package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_parental_control.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.ParentalControlProfileItemAdapter
import zyxel.com.multyproneo.api.ApiHandler
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.ParentalControlApi
import zyxel.com.multyproneo.event.ApiEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.ParentalControlInfoProfile
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class ParentalControlFragment : Fragment()
{
    private val TAG = "ParentalControlFragment"
    private lateinit var getParentalControlInfoCompleteDisposable: Disposable
    private var delProfileInfo = ParentalControlInfoProfile()
    private var showDelProMsg = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_parental_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        with(arguments){ this?.getSerializable("DelProfileInfo")?.let{
            delProfileInfo = it as ParentalControlInfoProfile
            showDelProMsg = true
        }}

        getParentalControlInfoCompleteDisposable = GlobalBus.listen(ApiEvent.ApiExecuteComplete::class.java).subscribe{
            GlobalBus.publish(MainEvent.HideLoading())

            when(it.event)
            {
                ApiHandler.API_RES_EVENT.API_RES_EVENT_PARENTAL_CONTROL -> updateUI()
                else -> {}
            }
        }

        setActionListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
        startGetParentalControlInfo()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!getParentalControlInfoCompleteDisposable.isDisposed) getParentalControlInfoCompleteDisposable.dispose()
    }

    private fun setActionListener()
    {
        parental_control_profile_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            startGetParentalControlInfo()
        }

        parental_control_add_image.onClick{ GlobalBus.publish(MainEvent.SwitchToFrag(ParentalControlAddProfileFragment())) }

        parental_control_add_profile_button.onClick{ GlobalBus.publish(MainEvent.SwitchToFrag(ParentalControlAddProfileFragment())) }

        parental_control_switch_image.onClick{ setParentalControl() }
    }

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        runOnUiThread{

            parental_control_switch_image.imageResource =
                    if(GlobalData.parentalControlMasterSwitch) R.drawable.toggle_switch_on else R.drawable.toggle_switch_off

            parental_control_profile_list_swipe.setRefreshing(false)

            if(GlobalData.parentalControlInfoProfileList.size < AppConfig.PC_MAX_PROFILE)
            {
                parental_control_add_image.alpha = 1f
                parental_control_add_image.isEnabled = true
            }
            else
            {
                parental_control_add_image.alpha = 0.3f
                parental_control_add_image.isEnabled = false
            }

            if(GlobalData.parentalControlInfoProfileList.isEmpty())
            {
                parental_control_no_profile_area_relative.visibility = View.VISIBLE
                parental_control_profile_list_swipe.visibility = View.GONE
                parental_control_switch_relative.visibility = View.GONE
            }
            else
            {
                parental_control_no_profile_area_relative.visibility = View.GONE
                parental_control_profile_list_swipe.visibility = View.VISIBLE
                parental_control_switch_relative.visibility = View.VISIBLE
            }

            parental_control_profile_list.adapter = ParentalControlProfileItemAdapter(activity!!, GlobalData.parentalControlInfoProfileList)
            val height = CommonTool.setListViewHeight(parental_control_profile_list)
            val paramsS: ViewGroup.LayoutParams = parental_control_profile_list_swipe.layoutParams
            paramsS.height = height
            parental_control_profile_list_swipe.layoutParams = paramsS

            if(showDelProMsg)
            {
                showDelProMsg = false
                GlobalBus.publish(MainEvent.ShowToast(String.format(getString(R.string.parental_control_delete_profile_toast), delProfileInfo.Name), TAG))
            }
        }
    }

    private fun startGetParentalControlInfo()
    {
        GlobalBus.publish(MainEvent.ShowLoading())
        ApiHandler().execute(
                ApiHandler.API_RES_EVENT.API_RES_EVENT_PARENTAL_CONTROL,
                arrayListOf
                (
                        ApiHandler.API_REF.API_GET_PARENTAL_CONTROL_INFO,
                        ApiHandler.API_REF.API_GET_GATEWAY_SYSTEM_DATE
                )
        )
    }

    private fun setParentalControl()
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        LogUtil.d(TAG, "setParentalControl()")

        val params = JSONObject()
        params.put("Enable", !GlobalData.parentalControlMasterSwitch)
        LogUtil.d(TAG, "setParentalControl param:$params")

        ParentalControlApi.SetParentalControl()
                .setRequestPageName(TAG)
                .setParams(params)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.loginInfo.sessionkey = sessionkey
                            startGetParentalControlInfo()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }
}