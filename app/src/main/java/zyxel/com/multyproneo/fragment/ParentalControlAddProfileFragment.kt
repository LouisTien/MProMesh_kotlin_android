package zyxel.com.multyproneo.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.theartofdev.edmodo.cropper.CropImage
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_parental_control_profile.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.BuildConfig
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.ParentalControlDeviceItemAdapter
import zyxel.com.multyproneo.adapter.ParentalControlScheduleItemAdapter
import zyxel.com.multyproneo.api.ApiHandler
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.ParentalControlApi
import zyxel.com.multyproneo.dialog.*
import zyxel.com.multyproneo.event.*
import zyxel.com.multyproneo.model.ParentalControlInfoSchedule
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import java.io.File
import java.net.URI

class ParentalControlAddProfileFragment : Fragment()
{
    private val TAG = "ParentalControlAddFragment"
    private lateinit var getParentalControlScheduleSetInfoDisposable: Disposable
    private lateinit var getParentalControlScheduleMenuInfoDisposable: Disposable
    private lateinit var getParentalControlScheduleActionInfoDisposable: Disposable
    private lateinit var getParentalControlDeviceActionInfoDisposable: Disposable
    private lateinit var getEndDeviceInfoCompleteDisposable: Disposable
    private lateinit var getParentalControlDeviceMenuInfoDisposable: Disposable
    private lateinit var selectParentalControlDeviceCompleteDisposable: Disposable
    private lateinit var handleParentalControlPhotoDisposable: Disposable
    private lateinit var msgDialogResponse: Disposable
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var setupScheduleSlideDialog: ParentalControlSetupScheduleSlideDialog
    private lateinit var setupScheduleActionMenuSlideDialog: ParentalControlSetupScheduleActionMenuSlideDialog
    private lateinit var setupDeviceActionMenuSlideDialog: ParentalControlSetupDeviceActionMenuSlideDialog
    private lateinit var setupDeviceSlideDialog: ParentalControlSetupDeviceSlideDialog
    private lateinit var addPhotoSlideDialog: ParentalControlAddPhotoSlideDialog
    private var keyboardListenersAttached = false
    private var scheduleList = mutableListOf<ParentalControlInfoSchedule>()
    private var profilePicURI = Uri.parse("N/A")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_parental_control_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        GlobalData.parentalControlSelectedDeviceList.clear()

        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        getParentalControlScheduleSetInfoDisposable = GlobalBus.listen(ParentalControlEvent.ScheduleSet::class.java).subscribe{
            when(it.action)
            {
                AppConfig.ScheduleAction.ACT_ADD -> scheduleList.add(it.scheduleInfo)
                AppConfig.ScheduleAction.ACT_EDIT -> scheduleList[it.index] = it.scheduleInfo
                else -> {}
            }
            updateScheduleAreaUI()
        }

        getParentalControlScheduleMenuInfoDisposable = GlobalBus.listen(ParentalControlEvent.ScheduleMenu::class.java).subscribe{
            setupScheduleActionMenuSlideDialog = ParentalControlSetupScheduleActionMenuSlideDialog(activity!!, it.index, it.scheduleInfo, scheduleList.size == 1)
            setupScheduleActionMenuSlideDialog.show()
        }

        getParentalControlScheduleActionInfoDisposable = GlobalBus.listen(ParentalControlEvent.ScheduleAct::class.java).subscribe{
            when(it.action)
            {
                AppConfig.ScheduleAction.ACT_DELETE -> scheduleList.removeAt(it.index)
                AppConfig.ScheduleAction.ACT_EDIT ->
                {
                    setupScheduleSlideDialog = ParentalControlSetupScheduleSlideDialog(activity!!,it.action, it.index, it.scheduleInfo)
                    setupScheduleSlideDialog.show()
                }
                else -> {}
            }
            updateScheduleAreaUI()
        }

        getEndDeviceInfoCompleteDisposable = GlobalBus.listen(ApiEvent.ApiExecuteComplete::class.java).subscribe{
            GlobalBus.publish(MainEvent.HideLoading())

            when(it.event)
            {
                ApiHandler.API_RES_EVENT.API_RES_EVENT_PARENTAL_CONTROL_ADD_SELECT_DEVICE ->
                {
                    runOnUiThread {
                        setupDeviceSlideDialog = ParentalControlSetupDeviceSlideDialog(activity!!)
                        setupDeviceSlideDialog.show()
                    }
                }

                else -> {}
            }
        }

        selectParentalControlDeviceCompleteDisposable = GlobalBus.listen(ParentalControlEvent.SelectParentalControlDeviceComplete::class.java).subscribe{
            GlobalData.parentalControlSelectedDeviceList.clear()

            GlobalData.homeEndDeviceList.forEach{
                if(it.ParentalControlSelect)
                    GlobalData.parentalControlSelectedDeviceList.add(it)
            }

            updateDeviceAreaUI()
        }

        getParentalControlDeviceActionInfoDisposable = GlobalBus.listen(ParentalControlEvent.DeviceDelete::class.java).subscribe{
            GlobalData.parentalControlSelectedDeviceList.remove(it.device)
            updateDeviceAreaUI()
        }

        getParentalControlDeviceMenuInfoDisposable = GlobalBus.listen(ParentalControlEvent.DeviceMenu::class.java).subscribe{
            setupDeviceActionMenuSlideDialog = ParentalControlSetupDeviceActionMenuSlideDialog(activity!!, it.device)
            setupDeviceActionMenuSlideDialog.show()
        }

        handleParentalControlPhotoDisposable = GlobalBus.listen(ParentalControlEvent.HandleProfilePhoto::class.java).subscribe{
            when(it.action)
            {
                AppConfig.ProfilePhotoAction.PRO_PHOTO_TAKE -> takePic()
                AppConfig.ProfilePhotoAction.PRO_PHOTO_SELECT -> selectPic()
                else -> {}
            }
        }

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_PC_DISACRD_CHANGE -> GlobalBus.publish(MainEvent.SwitchToFrag(ParentalControlFragment()))
                else -> {}
            }
        }

        setListener()

        //pop up keyboard and focus on name EditText
        parental_control_profile_name_edit.requestFocus()
        inputMethodManager.showSoftInput(parental_control_profile_name_edit, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onResume()
    {
        super.onResume()
        attachKeyboardListeners()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()

        if(!getParentalControlScheduleSetInfoDisposable.isDisposed) getParentalControlScheduleSetInfoDisposable.dispose()
        if(!getParentalControlScheduleMenuInfoDisposable.isDisposed) getParentalControlScheduleMenuInfoDisposable.dispose()
        if(!getParentalControlScheduleActionInfoDisposable.isDisposed) getParentalControlScheduleActionInfoDisposable.dispose()
        if(!getParentalControlDeviceActionInfoDisposable.isDisposed) getParentalControlDeviceActionInfoDisposable.dispose()
        if(!getParentalControlDeviceMenuInfoDisposable.isDisposed) getParentalControlDeviceMenuInfoDisposable.dispose()
        if(!getEndDeviceInfoCompleteDisposable.isDisposed) getEndDeviceInfoCompleteDisposable.dispose()
        if(!selectParentalControlDeviceCompleteDisposable.isDisposed) selectParentalControlDeviceCompleteDisposable.dispose()
        if(!handleParentalControlPhotoDisposable.isDisposed) handleParentalControlPhotoDisposable.dispose()
        if(!msgDialogResponse.isDisposed) msgDialogResponse.dispose()

        if(keyboardListenersAttached)
            view?.viewTreeObserver?.removeOnGlobalLayoutListener(keyboardLayoutListener)
    }

    private val keyboardLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener
    {
        override fun onGlobalLayout()
        {
//            val rect = Rect()
//            view?.getWindowVisibleDisplayFrame(rect)
//            val heightDiff = view?.rootView?.height!! - (rect.bottom - rect.top)
//            if(heightDiff > 500)
//            {
//                parental_control_profile_name_edit.requestFocus()
//                parental_control_profile_image.visibility = View.GONE
//                parental_control_profile_add_photo_image.visibility = View.GONE
//            }
//            else
//            {
//                parental_control_profile_name_edit.clearFocus()
//                parental_control_profile_image.visibility = View.VISIBLE
//                parental_control_profile_add_photo_image.visibility = View.VISIBLE
//            }
        }
    }

    private fun attachKeyboardListeners()
    {
        if(keyboardListenersAttached) return
        view?.viewTreeObserver?.addOnGlobalLayoutListener(keyboardLayoutListener)
        keyboardListenersAttached = true
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            parental_control_profile_cancel_text ->
            {
                inputMethodManager.hideSoftInputFromWindow(parental_control_profile_name_edit.applicationWindowToken, 0)
                MessageDialog(
                        activity!!,
                        getString(R.string.message_dialog_parental_control_discard_title),
                        getString(R.string.message_dialog_parental_control_discard_msg),
                        arrayOf(getString(R.string.parental_control_discard), getString(R.string.parental_control_keep_edit)),
                        AppConfig.DialogAction.ACT_PC_DISACRD_CHANGE,
                        AppConfig.DialogPosBtnColor.DPC_RED
                ).show()
            }

            parental_control_profile_add_text ->
            {
                inputMethodManager.hideSoftInputFromWindow(parental_control_profile_name_edit.applicationWindowToken, 0)
                addParentalControlProfile()
            }

            parental_control_profile_add_photo_image ->
            {
                runOnUiThread {
                    addPhotoSlideDialog = ParentalControlAddPhotoSlideDialog(activity!!)
                    addPhotoSlideDialog.show()
                }
            }

            parental_control_profile_add_schedule_linear ->
            {
                inputMethodManager.hideSoftInputFromWindow(parental_control_profile_name_edit.applicationWindowToken, 0)
                setupScheduleSlideDialog = ParentalControlSetupScheduleSlideDialog(activity!!)
                setupScheduleSlideDialog.show()
            }

            parental_control_profile_add_device_linear -> startGetEndDeviceInfoTask()

            parental_control_profile_name_clear_image -> parental_control_profile_name_edit.setText("")
        }
    }

    private fun setListener()
    {
        parental_control_profile_cancel_text.setOnClickListener(clickListener)
        parental_control_profile_add_text.setOnClickListener(clickListener)
        parental_control_profile_add_photo_image.setOnClickListener(clickListener)
        parental_control_profile_add_schedule_linear.setOnClickListener(clickListener)
        parental_control_profile_add_device_linear.setOnClickListener(clickListener)
        parental_control_profile_name_clear_image.setOnClickListener(clickListener)
        parental_control_profile_name_edit.textChangedListener{ afterTextChanged{ checkCreateStatus() } }
        parental_control_profile_name_edit.setOnFocusChangeListener{ _, hasFocus ->
            when(hasFocus)
            {
                true ->
                {
                    parental_control_profile_name_edit_line_image.visibility = View.VISIBLE
                    parental_control_profile_name_clear_image.visibility = View.VISIBLE
                }

                false ->
                {
                    parental_control_profile_name_edit_line_image.visibility = View.GONE
                    parental_control_profile_name_clear_image.visibility = View.GONE
                }
            }
        }
    }

    private fun updateScheduleAreaUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        runOnUiThread{
            if(scheduleList.isNotEmpty())
            {
                parental_control_profile_schedule_list.adapter = ParentalControlScheduleItemAdapter(scheduleList)
                parental_control_profile_schedule_list.visibility = View.VISIBLE
                CommonTool.setListViewHeight(parental_control_profile_schedule_list)

                //parental_control_profile_add_schedule_linear.backgroundResource = R.drawable.corner_down_helf_shape

                val params = parental_control_profile_add_schedule_linear.layoutParams as RelativeLayout.LayoutParams
                params.topMargin = 0
                parental_control_profile_add_schedule_linear.layoutParams = params
            }
            else
            {
                parental_control_profile_schedule_list.visibility = View.GONE

                //parental_control_profile_add_schedule_linear.backgroundResource = R.drawable.corner_shape

                val params = parental_control_profile_add_schedule_linear.layoutParams as RelativeLayout.LayoutParams
                params.topMargin = context?.resources?.getDimension(R.dimen.layout_size_10dp_in_1080)?.toInt() ?: 30
                parental_control_profile_add_schedule_linear.layoutParams = params
            }

            if(scheduleList.size >= AppConfig.PC_MAX_SCHEDULE)
            {
                parental_control_profile_add_schedule_text.alpha = 0.3f
                parental_control_profile_add_schedule_linear.isEnabled = false
            }
            else
            {
                parental_control_profile_add_schedule_text.alpha = 1f
                parental_control_profile_add_schedule_linear.isEnabled = true
            }

            checkCreateStatus()
        }
    }

    private fun updateDeviceAreaUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        runOnUiThread{
            if(GlobalData.parentalControlSelectedDeviceList.isNotEmpty())
            {
                parental_control_profile_device_list.adapter = ParentalControlDeviceItemAdapter(GlobalData.parentalControlSelectedDeviceList)
                parental_control_profile_device_list.visibility = View.VISIBLE
                CommonTool.setListViewHeight(parental_control_profile_device_list)

                //parental_control_profile_add_device_linear.backgroundResource = R.drawable.corner_down_helf_shape

                val params = parental_control_profile_add_device_linear.layoutParams as RelativeLayout.LayoutParams
                params.topMargin = 0
                parental_control_profile_add_device_linear.layoutParams = params
            }
            else
            {
                parental_control_profile_device_list.visibility = View.GONE

                //parental_control_profile_add_device_linear.backgroundResource = R.drawable.corner_shape

                val params = parental_control_profile_add_device_linear.layoutParams as RelativeLayout.LayoutParams
                params.topMargin = context?.resources?.getDimension(R.dimen.layout_size_10dp_in_1080)?.toInt() ?: 30
                parental_control_profile_add_device_linear.layoutParams = params
            }

            checkCreateStatus()
        }
    }

    private fun checkCreateStatus()
    {
        if(parental_control_profile_name_edit.text.isNotEmpty() &&
            scheduleList.isNotEmpty() &&
            GlobalData.parentalControlSelectedDeviceList.isNotEmpty())
        {
            parental_control_profile_add_text.alpha = 1f
            parental_control_profile_add_text.isEnabled = true
        }
        else
        {
            parental_control_profile_add_text.alpha = 0.3f
            parental_control_profile_add_text.isEnabled = false
        }
    }

    private fun takePic()
    {
        if(hasCameraPermission())
            takePicFromCamera()
        else
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(arrayOf(Manifest.permission.CAMERA), AppConfig.PERMISSION_CAMERA_REQUESTCODE)
        }
    }

    private fun selectPic()
    {
        if(hasReadStoragePermission())
            pickImageFromGallery()
        else
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES), AppConfig.PERMISSION_READ_STORAGE_REQUESTCODE)
            else
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), AppConfig.PERMISSION_READ_STORAGE_REQUESTCODE)

        }
    }

    private fun hasCameraPermission() =
            ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun hasReadStoragePermission() =
            ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        when(requestCode)
        {
            AppConfig.PERMISSION_READ_STORAGE_REQUESTCODE ->
            {
                if((grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    LogUtil.d(TAG, "Read storage permission granted!")
                    pickImageFromGallery()
                }
                else
                    LogUtil.e(TAG, "Read storage permission denied!")
            }

            AppConfig.PERMISSION_CAMERA_REQUESTCODE ->
            {
                if((grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    LogUtil.d(TAG, "Camera permission granted!")
                    takePicFromCamera()
                }
                else
                    LogUtil.e(TAG, "Camera permission denied!")
            }
        }
    }

    private fun pickImageFromGallery()
    {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, AppConfig.IMAGE_PICK_CODE)
    }

    private fun takePicFromCamera()
    {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try
        {
            val tmpPhotoFile = File("${GlobalData.parentalControlProfilePicDir.toString()}/tmp.jpg")
            profilePicURI = FileProvider.getUriForFile(activity!!, BuildConfig.APPLICATION_ID + ".fileprovider", tmpPhotoFile)
            LogUtil.d(TAG, "profilePicURI = $profilePicURI")

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, profilePicURI)
            else
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpPhotoFile))

            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
        startActivityForResult(intent, AppConfig.IMAGE_TAKE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        when(requestCode)
        {
            AppConfig.IMAGE_PICK_CODE, AppConfig.IMAGE_TAKE_CODE ->
            {
                if(resultCode == Activity.RESULT_OK)
                {
                    if(requestCode == AppConfig.IMAGE_PICK_CODE)
                        profilePicURI = data?.data ?: Uri.parse("N/A")

                    LogUtil.d(TAG,"Image URL:$profilePicURI")
                    val intent = context?.let { CropImage.activity(profilePicURI).getIntent(it) }
                    startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->
            {
                when(resultCode)
                {
                    Activity.RESULT_OK ->
                    {
                        val result = CropImage.getActivityResult(data)
                        profilePicURI = result.uri
                        LogUtil.d(TAG,"Cropper Image URL:$profilePicURI")
                        parental_control_profile_image.setImageURI(profilePicURI)
                        parental_control_profile_add_photo_image.setImageResource(R.drawable.icon_edit)
                    }

                    else -> profilePicURI = Uri.parse("N/A")
                }
            }
        }
    }

    private fun saveProfilePic()
    {
        if(profilePicURI.isAbsolute)
        {
            val desFile = File("${GlobalData.parentalControlProfilePicDir.toString()}/${GlobalData.getCurrentGatewayInfo().MAC}-${GlobalData.parentalControlProfileFirstEmptyIndex}.jpg")
            try
            {
                val srcfile = File(URI(profilePicURI.toString()))
                if(srcfile.exists())
                {
                    CommonTool.copyFile(srcfile, desFile)
                    LogUtil.d(TAG,"save pic path:${desFile.path}")
                }
                else
                    LogUtil.d(TAG,"source pic not exists:${srcfile.path}")
            }
            catch(e: Exception)
            {
                e.printStackTrace();
                LogUtil.e(TAG,"save pic fail:${desFile.path}")
            }
        }
        else
            LogUtil.e(TAG,"profile pic path not available : ${profilePicURI}, maybe not select the profile pic.")
    }

    private fun addParentalControlProfile()
    {
        LogUtil.d(TAG, "addParentalControlProfile()")

        GlobalBus.publish(MainEvent.ShowLoading())

        var macList = ""
        GlobalData.parentalControlSelectedDeviceList.forEach{
            macList += "${it.PhysAddress},"
        }

        if(macList.endsWith(","))
            macList = macList.substring(0, macList.length - 1)

        val params = JSONObject()
        params.put("Enable", true)
        params.put("Name", parental_control_profile_name_edit.text.toString())
        params.put("MACAddressList", macList)
        LogUtil.d(TAG, "addParentalControlProfile param:$params")

        ParentalControlApi.AddProfile()
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

                            if(scheduleList.isNotEmpty())
                            {
                                val schInfo = scheduleList.first()
                                addParentalControlSchedule(GlobalData.parentalControlProfileFirstEmptyIndex, schInfo)
                                scheduleList.remove(schInfo)
                            }
                            else
                                GlobalBus.publish(MainEvent.HideLoading())
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    private fun addParentalControlSchedule(profileIndex: Int, schInfo: ParentalControlInfoSchedule)
    {
        LogUtil.d(TAG, "addParentalControlSchedule()")

        val sch_params = JSONObject()
        sch_params.put("Days", schInfo.Days)
        sch_params.put("TimeStartHour", schInfo.TimeStartHour)
        sch_params.put("TimeStartMin", schInfo.TimeStartMin)
        sch_params.put("TimeStopHour", schInfo.TimeStopHour)
        sch_params.put("TimeStopMin", schInfo.TimeStopMin)
        LogUtil.d(TAG, "addParentalControlSchedule param:$sch_params")

        ParentalControlApi.AddSchedule(profileIndex)
                .setRequestPageName(TAG)
                .setParams(sch_params)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.loginInfo.sessionkey = sessionkey

                            if(scheduleList.isNotEmpty())
                            {
                                val sch = scheduleList.first()
                                addParentalControlSchedule(GlobalData.parentalControlProfileFirstEmptyIndex, sch)
                                scheduleList.remove(sch)
                            }
                            else
                            {
                                saveProfilePic()
                                GlobalBus.publish(MainEvent.HideLoading())
                                GlobalBus.publish(MainEvent.SwitchToFrag(ParentalControlFragment()))
                            }
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    private fun startGetEndDeviceInfoTask()
    {
        GlobalBus.publish(MainEvent.ShowLoading())
        ApiHandler().execute(
                ApiHandler.API_RES_EVENT.API_RES_EVENT_PARENTAL_CONTROL_ADD_SELECT_DEVICE,
                arrayListOf
                (
                        ApiHandler.API_REF.API_GET_CHANGE_ICON_NAME,
                        ApiHandler.API_REF.API_GET_DEVICE_INFO,
                        ApiHandler.API_REF.API_GET_PARENTAL_CONTROL_INFO,
                        ApiHandler.API_REF.API_GET_GATEWAY_SYSTEM_DATE,
                        ApiHandler.API_REF.API_CHECK_IN_USE_SELECT_DEVICE
                )
        )
    }
}