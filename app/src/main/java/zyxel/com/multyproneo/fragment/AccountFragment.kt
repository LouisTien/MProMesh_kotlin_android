package zyxel.com.multyproneo.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_account.*
import org.json.JSONObject
import zyxel.com.multyproneo.BuildConfig
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.DatabaseUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/6/13.
 */
class AccountFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var msgDialogResponse: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        account_app_version_text.text = BuildConfig.VERSION_NAME

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_LOGOUT -> setLogoutTask()
            }
        }

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!msgDialogResponse.isDisposed) msgDialogResponse.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            account_privacy_policy_relative -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zyxel.com/privacy_policy.shtml")))

            account_logout_button ->
            {
                MessageDialog(
                        activity!!,
                        getString(R.string.account_logout),
                        getString(R.string.message_dialog_check_lougot),
                        arrayOf(getString(R.string.message_dialog_yes), getString(R.string.message_dialog_no)),
                        AppConfig.DialogAction.ACT_LOGOUT
                ).show()
            }
        }
    }

    private fun setClickListener()
    {
        account_privacy_policy_relative.setOnClickListener(clickListener)
        account_logout_button.setOnClickListener(clickListener)
    }

    private fun setLogoutTask()
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        val params = JSONObject()
        AccountApi.Logout()
                .setRequestPageName(TAG)
                .setParams(params)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        DatabaseUtil.getInstance(activity!!)?.deleteInformationToDB(GlobalData.getCurrentGatewayInfo())
                        GlobalBus.publish(MainEvent.HideLoading())
                        GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
                    }
                }).execute()
    }
}