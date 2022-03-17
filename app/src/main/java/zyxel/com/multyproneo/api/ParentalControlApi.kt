package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData

object ParentalControlApi
{
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    class GetInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}?first_level_only=false"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getInfoURL)
                    .build()
        }
    }

    class GetProfileInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getProfileInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getProfileInfoURL)
                    .build()
        }
    }

    class GetProfileInfoByIndex(val index: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val getProfileInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile.$index."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getProfileInfoURL)
                    .build()
        }
    }

    class EditProfileInfo(val index: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val editProfileInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile.$index.?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(editProfileInfoURL)
                    .put(requestParam)
                    .build()
        }
    }

    class AddProfile : Commander()
    {
        override fun composeRequest(): Request
        {
            val addProfileURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile.?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(addProfileURL)
                    .post(requestParam)
                    .build()
        }
    }

    class DeleteProfile(val index: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val deleteProfileURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile.$index.?sessionkey=${GlobalData.loginInfo.sessionkey}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(deleteProfileURL)
                    .delete()
                    .build()
        }
    }

    class GetScheduleInfo(val profileIndex: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val getScheduleInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile.$profileIndex.Schedule."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getScheduleInfoURL)
                    .build()
        }
    }

    class GetScheduleInfoByIndex(val profileIndex: Int = 0, val scheduleIndex: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val getScheduleInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile.$profileIndex.Schedule.$scheduleIndex."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getScheduleInfoURL)
                    .build()
        }
    }

    class AddSchedule(val profileIndex: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val addScheduleURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile.$profileIndex.Schedule.?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(addScheduleURL)
                    .post(requestParam)
                    .build()
        }
    }

    class EditScheduleInfo(val profileIndex: Int = 0, val scheduleIndex: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val editScheduleInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile.$profileIndex.Schedule.$scheduleIndex.?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(editScheduleInfoURL)
                    .put(requestParam)
                    .build()
        }
    }

    class DeleteScheduleInfo(val profileIndex: Int = 0, val scheduleIndex: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val deleteScheduleInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}Profile.$profileIndex.Schedule.$scheduleIndex.?sessionkey=${GlobalData.loginInfo.sessionkey}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(deleteScheduleInfoURL)
                    .delete()
                    .build()
        }
    }

    class SetParentalControl() : Commander()
    {
        override fun composeRequest(): Request
        {
            val setParentalControlURL = "${GlobalData.getAPIPath()}${AppConfig.API_PARENTAL_CONTROL}?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setParentalControlURL)
                    .put(requestParam)
                    .build()
        }
    }
}