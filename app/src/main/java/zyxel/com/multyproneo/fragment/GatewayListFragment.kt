package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_gateway_list.*
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.GatewayItemAdapter
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.event.GatewayListEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.model.LoginInfo
import zyxel.com.multyproneo.util.DatabaseUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/5/30.
 */
class GatewayListFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var deviceSelectedDisposable: Disposable
    private lateinit var gatewayInfoMutableList: MutableList<GatewayInfo>
    private lateinit var loginInfo: LoginInfo
    private var autoLogin = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_gateway_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("AutoLogin")?.let{ autoLogin = it }
        }

        gateway_retry_image.setOnClickListener{
            GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
        }
    }

    override fun onResume()
    {
        super.onResume()

        GlobalBus.publish(MainEvent.HideBottomToolbar())

        deviceSelectedDisposable = GlobalBus.listen(GatewayListEvent.OnDeviceSelected::class.java).subscribe{
            OnDeviceSelected(it.index)
        }

        gatewayInfoMutableList = GlobalData.gatewayList

        with(gateway_list_view)
        {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            adapter = GatewayItemAdapter(gatewayInfoMutableList)
        }

        if(autoLogin && gatewayInfoMutableList.size == 1)
        {
            GlobalBus.publish(MainEvent.ShowLoading())
            OnDeviceSelected(0)
        }
    }

    override fun onPause()
    {
        super.onPause()
        if(!deviceSelectedDisposable.isDisposed) deviceSelectedDisposable.dispose()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private fun OnDeviceSelected(index: Int)
    {
        GlobalData.currentGatewayIndex = index
        val userName = DatabaseUtil.getInstance(activity!!)?.getDeviceUserNameFromDB(gatewayInfoMutableList[index].MAC)
        val password = DatabaseUtil.getInstance(activity!!)?.getDevicePasswordFromDB(gatewayInfoMutableList[index].MAC)
        LogUtil.d(TAG, "OnDeviceSelected userName from DB:$userName")
        LogUtil.d(TAG, "OnDeviceSelected password from DB:$password")

        if(password == "" || userName == "")
        {
            LogUtil.d(TAG, "password or user name from DB is empty")
            GlobalBus.publish(MainEvent.HideLoading())
            GlobalBus.publish(MainEvent.SwitchToFrag(LoginFragment()))
        }
        else
        {
            LogUtil.d(TAG, "get password or user name from DB, start login")
            val params = JSONObject()
            params.put("username", userName)
            params.put("password", password)
            LogUtil.d(TAG,"login param:${params}")
            AccountApi.Login()
                    .setRequestPageName(TAG)
                    .setParams(params)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                loginInfo = Gson().fromJson(responseStr, LoginInfo::class.javaObjectType)
                                LogUtil.d(TAG,"loginInfo:$loginInfo")
                                GlobalData.sessionKey = loginInfo.sessionkey
                                gatewayInfoMutableList[index].Password = password!!
                                gatewayInfoMutableList[index].UserName = userName!!
                                DatabaseUtil.getInstance(activity!!)?.updateInformationToDB(gatewayInfoMutableList[index])
                                GlobalBus.publish(MainEvent.HideLoading())
                                GlobalBus.publish(MainEvent.EnterHomePage())
                            }
                            catch(e: JSONException)
                            {
                                GlobalBus.publish(MainEvent.HideLoading())
                                GlobalBus.publish(MainEvent.SwitchToFrag(LoginFragment()))
                                e.printStackTrace()
                            }
                        }

                        override fun onFail(code: Int, msg: String, ctxName: String)
                        {
                            LogUtil.e(TAG, "[onFail] code = $code")
                            LogUtil.e(TAG, "[onFail] msg = $msg")
                            LogUtil.e(TAG, "[onFail] ctxName = $ctxName")
                            GlobalBus.publish(MainEvent.HideLoading())
                            GlobalBus.publish(MainEvent.SwitchToFrag(LoginFragment().apply{ arguments = Bundle().apply{ putString("Error", msg) } }))
                        }
                    }).execute()
        }
    }
}