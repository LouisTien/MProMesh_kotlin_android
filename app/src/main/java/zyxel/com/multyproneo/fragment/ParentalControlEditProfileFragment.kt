package zyxel.com.multyproneo.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
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
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.model.ParentalControlInfoProfile
import zyxel.com.multyproneo.model.ParentalControlInfoSchedule
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import java.io.File
import java.net.URI

class ParentalControlEditProfileFragment : Fragment()
{
    private val TAG = "ParentalControlEditProfileFragment"
    private lateinit var getParentalControlScheduleSetInfoDisposable: Disposable
    private lateinit var getParentalControlScheduleMenuInfoDisposable: Disposable
    private lateinit var getParentalControlScheduleActionInfoDisposable: Disposable
    private lateinit var getParentalControlDeviceMenuInfoDisposable: Disposable
    private lateinit var getParentalControlDeviceActionInfoDisposable: Disposable
    private lateinit var getEndDeviceInfoCompleteDisposable: Disposable
    private lateinit var selectParentalControlDeviceCompleteDisposable: Disposable
    private lateinit var handleParentalControlPhotoDisposable: Disposable
    private lateinit var msgDialogResponse: Disposable
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var setupScheduleSlideDialog: ParentalControlSetupScheduleSlideDialog
    private lateinit var setupScheduleActionMenuSlideDialog: ParentalControlSetupScheduleActionMenuSlideDialog
    private lateinit var setupDeviceActionMenuSlideDialog: ParentalControlSetupDeviceActionMenuSlideDialog
    private lateinit var setupDeviceSlideDialog: ParentalControlSetupDeviceSlideDialog
    private lateinit var editPhotoSlideDialog: ParentalControlEditPhotoSlideDialog
    private var profilePicURI = Uri.parse("N/A")
    private var profileInfo = ParentalControlInfoProfile()
    private var isEditNamePicMode = false
    private var keyboardListenersAttached = false
    private var scheduleList = mutableListOf<ParentalControlInfoSchedule>()
    private var deviceList = mutableListOf<DevicesInfoObject>()
    private var delScheduleIndex = 0
    private var delDeviceMac = ""
    private var delProfilePic = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_parental_control_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        with(arguments){ this?.getSerializable("ProfileInfo")?.let{ profileInfo = it as ParentalControlInfoProfile} }

        getParentalControlScheduleSetInfoDisposable = GlobalBus.listen(ParentalControlEvent.ScheduleSet::class.java).subscribe{
            when(it.action)
            {
                AppConfig.ScheduleAction.ACT_ADD -> addParentalControlSchedule(profileInfo.index, it.scheduleInfo)
                AppConfig.ScheduleAction.ACT_EDIT -> editParentalControlScheduleInfo(profileInfo.index, it.index, it.scheduleInfo)
                else -> {}
            }
            updateScheduleAreaUI()
        }

        getParentalControlScheduleMenuInfoDisposable = GlobalBus.listen(ParentalControlEvent.ScheduleMenu::class.java).subscribe{
            if(profileInfo.Enable)
            {
                setupScheduleActionMenuSlideDialog = ParentalControlSetupScheduleActionMenuSlideDialog(activity!!, it.index, it.scheduleInfo, scheduleList.size == 1)
                setupScheduleActionMenuSlideDialog.show()
            }
        }

        getParentalControlScheduleActionInfoDisposable = GlobalBus.listen(ParentalControlEvent.ScheduleAct::class.java).subscribe{
            when(it.action)
            {
                AppConfig.ScheduleAction.ACT_DELETE ->
                {
                    delScheduleIndex = it.index

                    val formatStr = "%02d"
                    val timeStr = String.format(formatStr, it.scheduleInfo.TimeStartHour) + ":" +
                            String.format(formatStr, it.scheduleInfo.TimeStartMin) + " - " +
                            String.format(formatStr, it.scheduleInfo.TimeStopHour) + ":" +
                            String.format(formatStr, it.scheduleInfo.TimeStopMin)

                    val weekStr = CommonTool.getSelectDays(it.scheduleInfo)
                    val schStr = " \"$timeStr $weekStr\"?"

                    MessageDialog(
                            activity!!,
                            getString(R.string.parental_control_delete_schedule) + schStr,
                            getString(R.string.parental_control_delete_schedule_msg),
                            arrayOf(getString(R.string.parental_control_delete), getString(R.string.common_cancel)),
                            AppConfig.DialogAction.ACT_PC_SCHEDULE_DELETE,
                            AppConfig.DialogPosBtnColor.DPC_RED
                    ).show()
                }

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
                ApiHandler.API_RES_EVENT.API_RES_EVENT_PARENTAL_CONTROL_EDIT_SELECT_DEVICE ->
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

            GlobalData.homeEndDeviceList.forEach{
                if(it.ParentalControlSelect)
                    deviceList.add(it)
            }

            var macList = ""
            deviceList.forEach{
                macList += "${it.PhysAddress},"
            }

            if(macList.endsWith(","))
                macList = macList.substring(0, macList.length - 1)

            editParentalControlProfileDevice(macList)
        }

        getParentalControlDeviceMenuInfoDisposable = GlobalBus.listen(ParentalControlEvent.DeviceMenu::class.java).subscribe{
            setupDeviceActionMenuSlideDialog = ParentalControlSetupDeviceActionMenuSlideDialog(activity!!, it.device)
            setupDeviceActionMenuSlideDialog.show()
        }

        getParentalControlDeviceActionInfoDisposable = GlobalBus.listen(ParentalControlEvent.DeviceDelete::class.java).subscribe{
            delDeviceMac = it.device.PhysAddress

            MessageDialog(
                    activity!!,
                    String.format(getString(R.string.parental_control_remove_device_detail), it.device.getName()),
                    getString(R.string.parental_control_delete_device_msg),
                    arrayOf(getString(R.string.remove_site_dialog_remove), getString(R.string.common_cancel)),
                    AppConfig.DialogAction.ACT_PC_DEVICE_DELETE,
                    AppConfig.DialogPosBtnColor.DPC_RED
            ).show()
        }

        handleParentalControlPhotoDisposable = GlobalBus.listen(ParentalControlEvent.HandleProfilePhoto::class.java).subscribe{
            delProfilePic = false
            when(it.action)
            {
                AppConfig.ProfilePhotoAction.PRO_PHOTO_TAKE -> takePic()
                AppConfig.ProfilePhotoAction.PRO_PHOTO_SELECT -> selectPic()
                AppConfig.ProfilePhotoAction.PRO_PHOTO_DELETE -> deletePic()
            }
        }

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_PC_SCHEDULE_DELETE -> deleteParentalControlScheduleInfo(profileInfo.index, delScheduleIndex)

                AppConfig.DialogAction.ACT_PC_DEVICE_DELETE ->
                {
                    var macList = ""
                    profileInfo.GetMACAddressList().forEach{
                        if(it != delDeviceMac) macList += "$it,"
                    }

                    if(macList.endsWith(","))
                        macList = macList.substring(0, macList.length - 1)

                    editParentalControlProfileDevice(macList)
                }

                AppConfig.DialogAction.ACT_PC_PROFILE_DELETE -> deleteParentalControlProfile(profileInfo.index)

                else -> {}
            }
        }

        setListener()

        updateUI()
    }

    override fun onResume()
    {
        super.onResume()
        attachKeyboardListeners()
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
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
        if(!getParentalControlDeviceMenuInfoDisposable.isDisposed) getParentalControlDeviceMenuInfoDisposable.dispose()
        if(!getParentalControlDeviceActionInfoDisposable.isDisposed) getParentalControlDeviceActionInfoDisposable.dispose()
        if(!getEndDeviceInfoCompleteDisposable.isDisposed) getEndDeviceInfoCompleteDisposable.dispose()
        if(!selectParentalControlDeviceCompleteDisposable.isDisposed) selectParentalControlDeviceCompleteDisposable.dispose()
        if(!handleParentalControlPhotoDisposable.isDisposed) handleParentalControlPhotoDisposable.dispose()
        if(!msgDialogResponse.isDisposed) msgDialogResponse.dispose()

        if(keyboardListenersAttached)
            view?.viewTreeObserver?.removeGlobalOnLayoutListener(keyboardLayoutListener)
    }

    private val keyboardLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener
    {
        override fun onGlobalLayout()
        {
            if(isEditNamePicMode)
            {
                val rect = Rect()
                view?.getWindowVisibleDisplayFrame(rect)
                val heightDiff = view?.rootView?.height!! - (rect.bottom - rect.top)
                if(heightDiff > 500)
                {
                    parental_control_profile_name_edit.requestFocus()
                    parental_control_profile_image.visibility = View.GONE
                    parental_control_profile_add_photo_image.visibility = View.GONE
                }
                else
                {
                    parental_control_profile_name_edit.clearFocus()
                    parental_control_profile_image.visibility = View.VISIBLE
                    parental_control_profile_add_photo_image.visibility = View.VISIBLE
                }
            }
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
            parental_control_profile_add_text ->
            {
                if(!isEditNamePicMode)
                {
                    isEditNamePicMode = true
                    updateUI()
                }
                else
                {
                    inputMethodManager.hideSoftInputFromWindow(parental_control_profile_name_edit.applicationWindowToken, 0)
                    editParentalControlProfileName(parental_control_profile_name_edit.text.toString())
                }
            }

            parental_control_profile_cancel_text ->
            {
                inputMethodManager.hideSoftInputFromWindow(parental_control_profile_name_edit.applicationWindowToken, 0)
                isEditNamePicMode = false
                delProfilePic = false
                updateUI()
            }

            parental_control_profile_back_image -> GlobalBus.publish(MainEvent.SwitchToFrag(ParentalControlFragment()))

            parental_control_profile_add_photo_image ->
            {
                runOnUiThread {
                    editPhotoSlideDialog = ParentalControlEditPhotoSlideDialog(activity!!)
                    editPhotoSlideDialog.show()
                }
            }

            parental_control_profile_schedule_switch -> editParentalControlProfileEnable(!profileInfo.Enable)

            parental_control_profile_add_schedule_linear ->
            {
                setupScheduleSlideDialog = ParentalControlSetupScheduleSlideDialog(activity!!)
                setupScheduleSlideDialog.show()
            }

            parental_control_profile_add_device_linear ->
            {
                GlobalData.parentalControlSelectedDeviceList.clear()
                startGetEndDeviceInfoTask()
            }

            parental_control_profile_delete_profile_frame ->
            {
                MessageDialog(
                        activity!!,
                        getString(R.string.parental_control_delete_profile) + " \"${profileInfo.Name}\"?",
                        getString(R.string.parental_control_delete_profile_msg),
                        arrayOf(getString(R.string.parental_control_delete), getString(R.string.common_cancel)),
                        AppConfig.DialogAction.ACT_PC_PROFILE_DELETE,
                        AppConfig.DialogPosBtnColor.DPC_RED
                ).show()
            }

            parental_control_profile_name_clear_image -> parental_control_profile_name_edit.setText("")
        }
    }

    private fun setListener()
    {
        parental_control_profile_add_text.setOnClickListener(clickListener)
        parental_control_profile_cancel_text.setOnClickListener(clickListener)
        parental_control_profile_back_image.setOnClickListener(clickListener)
        parental_control_profile_add_photo_image.setOnClickListener(clickListener)
        parental_control_profile_schedule_switch.setOnClickListener(clickListener)
        parental_control_profile_add_schedule_linear.setOnClickListener(clickListener)
        parental_control_profile_add_device_linear.setOnClickListener(clickListener)
        parental_control_profile_delete_profile_frame.setOnClickListener(clickListener)
        parental_control_profile_name_clear_image.setOnClickListener(clickListener)
        parental_control_profile_name_edit.textChangedListener{ afterTextChanged{ checkSaveStatus() } }
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

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        runOnUiThread{
            //open UI
            parental_control_profile_title_area_relative.visibility = View.VISIBLE
            parental_control_profile_area_linear.visibility = View.VISIBLE

            //title
            parental_control_profile_title_text.text = profileInfo.Name

            //profile picture
            val picFile = File("${GlobalData.parentalControlProfilePicDir.toString()}/${GlobalData.getCurrentGatewayInfo().MAC}-${profileInfo.index}.jpg")
            if(picFile.exists())
            {
                val picBitmap = BitmapFactory.decodeFile(picFile.absolutePath)
                parental_control_profile_image.imageBitmap = picBitmap
            }
            else
                parental_control_profile_image.setImageResource(R.drawable.default_profile_image)

            //profile name
            parental_control_profile_name_edit.setText(profileInfo.Name)

            //schedule switch
            parental_control_profile_schedule_switch.setImageResource( if(profileInfo.Enable) R.drawable.toggle_switch_on else R.drawable.toggle_switch_off )

            //schedule list
            scheduleList.clear()
            profileInfo.Schedule.forEach{ scheduleList.add(it) }
            updateScheduleAreaUI()

            //device list
            deviceList.clear()
            var isFoundInHomeDevList = false
            for(mac in profileInfo.GetMACAddressList())
            {
                isFoundInHomeDevList = false

                for(endDev in GlobalData.homeEndDeviceList)
                {
                    if(mac == endDev.PhysAddress)
                    {
                        isFoundInHomeDevList = true
                        deviceList.add(endDev)
                        break
                    }
                }

                if(!isFoundInHomeDevList)
                {
                    for(item in GlobalData.changeIconNameList)
                    {
                        if(mac == item.MacAddress)
                        {
                            isFoundInHomeDevList = true
                            deviceList.add(
                                    DevicesInfoObject
                                    (
                                            HostName = item.HostName,
                                            PhysAddress = item.MacAddress,
                                            Internet_Blocking_Enable = item.Internet_Blocking_Enable
                                    ))
                            break
                        }
                    }

                    if(!isFoundInHomeDevList)
                        deviceList.add(DevicesInfoObject(HostName = mac, PhysAddress = mac))
                }
            }
            updateDeviceAreaUI()

            //decide which page
            if(isEditNamePicMode)
            {
                //title
                parental_control_profile_cancel_text.visibility = View.VISIBLE
                parental_control_profile_back_image.visibility = View.GONE
                parental_control_profile_add_text.text = getString(R.string.wifi_settings_edit_save)
                parental_control_profile_add_text.textColor = ContextCompat.getColor(context!!, R.color.color_575757)
                parental_control_profile_add_text.alpha = 1f
                parental_control_profile_title_text.text = getString(R.string.parental_control_edit_profile)

                //pic
                parental_control_profile_image.visibility = View.VISIBLE
                parental_control_profile_add_photo_image.visibility = View.VISIBLE
                parental_control_profile_add_photo_image.setImageResource(R.drawable.icon_edit)

                //name
                parental_control_profile_name_relative.visibility = View.VISIBLE
                parental_control_profile_name_line_image.visibility = View.GONE

                //schedule
                parental_control_profile_schedule_switch_relative.visibility = View.GONE
                parental_control_profile_schedule_relative.visibility = View.GONE

                //device
                parental_control_profile_device_relative.visibility = View.GONE

                //delete profile
                parental_control_profile_delete_profile_frame.visibility = View.GONE
            }
            else
            {
                //title
                parental_control_profile_cancel_text.visibility = View.GONE
                parental_control_profile_back_image.visibility = View.VISIBLE
                parental_control_profile_add_text.text = getString(R.string.settings_cloud_account_router_action_edit)
                parental_control_profile_add_text.textColor = ContextCompat.getColor(context!!, R.color.color_606060)
                parental_control_profile_add_text.alpha = 1f
                parental_control_profile_title_text.text = profileInfo.Name

                //pic
                parental_control_profile_image.visibility = View.VISIBLE
                parental_control_profile_add_photo_image.visibility = View.GONE

                //name
                parental_control_profile_name_relative.visibility = View.GONE

                //schedule
                parental_control_profile_schedule_switch_relative.visibility = View.VISIBLE
                parental_control_profile_schedule_relative.visibility = View.VISIBLE

                //device
                parental_control_profile_device_relative.visibility = View.VISIBLE

                //delete profile
                parental_control_profile_delete_profile_frame.visibility = View.VISIBLE
            }
        }
    }

    private fun updateScheduleAreaUI()
    {
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

        if( (scheduleList.size >= AppConfig.PC_MAX_SCHEDULE) || !profileInfo.Enable )
        {
            parental_control_profile_add_schedule_text.alpha = 0.3f
            parental_control_profile_add_schedule_linear.isEnabled = false
        }
        else
        {
            parental_control_profile_add_schedule_text.alpha = 1f
            parental_control_profile_add_schedule_linear.isEnabled = true
        }

        parental_control_profile_schedule_list.alpha = if(profileInfo.Enable) 1f else 0.3f
    }

    private fun updateDeviceAreaUI()
    {
        if(deviceList.isNotEmpty())
        {
            parental_control_profile_device_list.adapter = ParentalControlDeviceItemAdapter(deviceList)
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
    }

    private fun checkSaveStatus()
    {
        if(parental_control_profile_name_edit.text.isNotEmpty())
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
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), AppConfig.PERMISSION_READ_STORAGE_REQUESTCODE)
        }
    }

    private fun deletePic()
    {
        delProfilePic = true
        profilePicURI = Uri.parse("N/A")
        parental_control_profile_image.imageResource = R.drawable.default_profile_image
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
                    }

                    else -> profilePicURI = Uri.parse("N/A")
                }
            }
        }
    }

    private fun saveProfilePic()
    {
        val desFile = File("${GlobalData.parentalControlProfilePicDir.toString()}/${GlobalData.getCurrentGatewayInfo().MAC}-${profileInfo.index}.jpg")

        if(profilePicURI.isAbsolute)
        {
            val srcfile = File(URI(profilePicURI.toString()))
            try
            {
                if(srcfile.exists())
                {
                    CommonTool.copyFile(srcfile, desFile)
                    LogUtil.d(TAG,"save pic path:${desFile.path}")
                }
                else
                    LogUtil.e(TAG,"source pic not exists:${srcfile.path}")
            }
            catch(e: Exception)
            {
                e.printStackTrace();
                LogUtil.e(TAG,"save pic fail:${desFile.path}")
            }
        }
        else
            LogUtil.e(TAG, "profile pic path not available : ${profilePicURI}, maybe not select the profile pic.")

        if(delProfilePic)
        {
            try
            {
                if(desFile.exists())
                {
                    desFile.delete()
                    LogUtil.d(TAG,"delete pic path:${desFile.path}")
                }
                else
                    LogUtil.e(TAG,"delete pic not exists:${desFile.path}")
            }
            catch(e: Exception)
            {
                e.printStackTrace();
                LogUtil.e(TAG,"delete pic fail:${desFile.path}")
            }
        }
    }

    private fun editParentalControlProfileName(name: String)
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        val params = JSONObject()
        params.put("Name", name)

        editParentalControlProfile(AppConfig.EditProfileItem.ED_PRO_NAME, params)
    }

    private fun editParentalControlProfileEnable(enable: Boolean)
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        val params = JSONObject()
        params.put("Enable", enable)

        editParentalControlProfile(AppConfig.EditProfileItem.ED_PRO_ENABLE, params)
    }

    private fun editParentalControlProfileDevice(macList: String)
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        val params = JSONObject()
        params.put("MACAddressList", macList)

        editParentalControlProfile(AppConfig.EditProfileItem.ED_PRO_DEVICE, params)
    }

    private fun editParentalControlProfile(item: AppConfig.EditProfileItem, params: JSONObject)
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        LogUtil.d(TAG,"editParentalControlProfileInfo param:$params")

        ParentalControlApi.EditProfileInfo(profileInfo.index)
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

                            when(item)
                            {
                                AppConfig.EditProfileItem.ED_PRO_NAME ->
                                {
                                    profileInfo.Name = params.getString("Name").toString()
                                    saveProfilePic()
                                    isEditNamePicMode = false
                                }

                                AppConfig.EditProfileItem.ED_PRO_ENABLE -> profileInfo.Enable = params.getBoolean("Enable")

                                AppConfig.EditProfileItem.ED_PRO_DEVICE -> profileInfo.MACAddressList = params.getString("MACAddressList").toString()

                                else -> {}
                            }

                            updateUI()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        GlobalBus.publish(MainEvent.HideLoading())
                    }
                }).execute()
    }

    private fun addParentalControlSchedule(profileIndex: Int, schInfo: ParentalControlInfoSchedule)
    {
        GlobalBus.publish(MainEvent.ShowLoading())

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

                            profileInfo.Schedule.add(schInfo)
                            updateUI()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        GlobalBus.publish(MainEvent.HideLoading())
                    }
                }).execute()
    }

    private fun editParentalControlScheduleInfo(profileIndex: Int, scheduleIndex: Int, schInfo: ParentalControlInfoSchedule)
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        LogUtil.d(TAG, "editParentalControlScheduleInfo()")

        val sch_params = JSONObject()
        sch_params.put("Days", schInfo.Days)
        sch_params.put("TimeStartHour", schInfo.TimeStartHour)
        sch_params.put("TimeStartMin", schInfo.TimeStartMin)
        sch_params.put("TimeStopHour", schInfo.TimeStopHour)
        sch_params.put("TimeStopMin", schInfo.TimeStopMin)
        LogUtil.d(TAG, "editParentalControlScheduleInfo param:$sch_params")

        ParentalControlApi.EditScheduleInfo(profileIndex, (scheduleIndex + 1))
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

                            profileInfo.Schedule[scheduleIndex] = schInfo
                            updateUI()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        GlobalBus.publish(MainEvent.HideLoading())
                    }
                }).execute()
    }

    private fun deleteParentalControlScheduleInfo(profileIndex: Int, scheduleIndex: Int)
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        LogUtil.d(TAG, "deleteParentalControlScheduleInfo()")

        ParentalControlApi.DeleteScheduleInfo(profileIndex, (scheduleIndex + 1))
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.loginInfo.sessionkey = sessionkey

                            profileInfo.Schedule.removeAt(scheduleIndex)
                            updateUI()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        GlobalBus.publish(MainEvent.HideLoading())
                    }
                }).execute()
    }

    private fun deleteParentalControlProfile(index: Int)
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        LogUtil.d(TAG, "deleteParentalControlProfile()")

        ParentalControlApi.DeleteProfile(index)
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        GlobalBus.publish(MainEvent.HideLoading())

                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.loginInfo.sessionkey = sessionkey

                            val picFile = File("${GlobalData.parentalControlProfilePicDir.toString()}/${GlobalData.getCurrentGatewayInfo().MAC}-${profileInfo.index}.jpg")
                            if(picFile.exists())
                            {
                                picFile.delete()
                                LogUtil.d(TAG, "delete pic:${picFile.path}")
                            }

                            GlobalBus.publish(MainEvent.SwitchToFrag(ParentalControlFragment().apply{ arguments = Bundle().apply{ putSerializable("DelProfileInfo", profileInfo) }}))
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
                ApiHandler.API_RES_EVENT.API_RES_EVENT_PARENTAL_CONTROL_EDIT_SELECT_DEVICE,
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