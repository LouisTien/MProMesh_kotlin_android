package zyxel.com.multyproneo.api.cloud

import com.tutk.IOTC.IOTCAPIs
import com.tutk.IOTC.RDTAPIs
import com.tutk.IOTC.St_RDT_Status
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.LogUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

object TUTKP2PBaseApi
{
    private val TAG = javaClass.simpleName
    private var ms_nIOTCInit = IOTCAPIs.IOTC_ER_TIMEOUT
    private var m_strUID = ""
    private var m_bHasClientConn = false
    private var mSID = -1
    private var mRDT_ID = -1
    private lateinit var responseCallback: TUTKP2PResponseCallback


    fun initIOTCRDT(): Int
    {
        LogUtil.d(TAG, "RDT Version[${RDTAPIs.RDT_GetRDTApiVer()}]")

        ms_nIOTCInit = IOTCAPIs.IOTC_Initialize2(0)
        LogUtil.d(TAG, "IOTC_Initialize2(.)=$ms_nIOTCInit")

        if(ms_nIOTCInit != IOTCAPIs.IOTC_ER_NoERROR)
        {
            LogUtil.e(TAG, "IOTC_Initialize error!!")
            return ms_nIOTCInit
        }

        var ret = RDTAPIs.RDT_Initialize()
        LogUtil.d(TAG, "RDT_Initialize(.)=$ret")

        if(ret < 0)
        {
            LogUtil.e(TAG, "RDT_Initialize error!!")
            return ret
        }

        return 0
    }

    fun unInitIOTCRDT()
    {
        if(ms_nIOTCInit == IOTCAPIs.IOTC_ER_NoERROR)
        {
            RDTAPIs.RDT_DeInitialize()
            LogUtil.d(TAG, "RDT_DeInitialize()")
            IOTCAPIs.IOTC_DeInitialize()
            LogUtil.d(TAG, "IOTC_DeInitialize()")
            ms_nIOTCInit = IOTCAPIs.IOTC_ER_TIMEOUT
        }
    }

    fun startSession(strUID: String): Int
    {
        m_bHasClientConn = false
        m_strUID = strUID

        var nSID = -1
        nSID = clientConnectDev()

        if(nSID < 0)
            return -1
        else
        {
            val nRDT_ID = RDTAPIs.RDT_Create(nSID, 3000, 0)
            if(nRDT_ID < 0)
            {
                IOTCAPIs.IOTC_Session_Close(nSID)
                LogUtil.e(TAG, "RDT_Create() error:$nRDT_ID")
                return -1
            }
            else
            {
                mRDT_ID = nRDT_ID
                mSID = nSID
                m_bHasClientConn = true

                val status = St_RDT_Status()
                RDTAPIs.RDT_Status_Check(nRDT_ID, status)
                LogUtil.d(TAG, "doClient(), BufSizeInRecvQueue=${status.BufSizeInRecvQueue}, BufSizeInSendQueue=${status.BufSizeInSendQueue}")
            }
        }

        return 0
    }

    private fun clientConnectDev(): Int
    {
        val ret: Int
        var nSID = -1

        nSID = IOTCAPIs.IOTC_Get_SessionID()
        LogUtil.d(TAG, "IOTC_Get_SessionID(.)=$nSID")

        if(nSID < 0)
        {
            LogUtil.e(TAG, "IOTC_Get_SessionID error!!")
            return nSID
        }

        ret = IOTCAPIs.IOTC_Connect_ByUID_Parallel(m_strUID, nSID)
        LogUtil.d(TAG, "IOTC_Connect_ByUID_Parallel(.)=$ret")

        if(ret < 0)
        {
            when(ret)
            {
                IOTCAPIs.IOTC_ER_NOT_INITIALIZED -> LogUtil.e(TAG, "Don't call IOTC_Initialize2() when connecting.(${AppConfig.TUTK_STATUS_INIT_SEARCH_DEV})")
                IOTCAPIs.IOTC_ER_CONNECT_IS_CALLING -> LogUtil.e(TAG, "IOTC_Connect_ByXX() is calling when connecting.($ret)")
                IOTCAPIs.IOTC_ER_FAIL_RESOLVE_HOSTNAME -> LogUtil.e(TAG, "Can't resolved server's Domain name when connecting.($ret)")
                IOTCAPIs.IOTC_ER_SERVER_NOT_RESPONSE -> LogUtil.e(TAG, "Server not response when connecting.($ret)")
                IOTCAPIs.IOTC_ER_FAIL_GET_LOCAL_IP -> LogUtil.e(TAG, "Can't Get local IP when connecting.($ret)")
                IOTCAPIs.IOTC_ER_UNKNOWN_DEVICE -> LogUtil.e(TAG, "Wrong UID when connecting.($ret)")
                IOTCAPIs.IOTC_ER_UNLICENSE -> LogUtil.e(TAG, "UID is not registered when connecting.($ret)")
                IOTCAPIs.IOTC_ER_CAN_NOT_FIND_DEVICE -> LogUtil.e(TAG, "Device is NOT online when connecting.($ret)")
                IOTCAPIs.IOTC_ER_EXCEED_MAX_SESSION -> LogUtil.e(TAG, "Exceed the max session number when connecting.($ret)")
                IOTCAPIs.IOTC_ER_TIMEOUT -> LogUtil.e(TAG, "Timeout when connecting.($ret)")
                IOTCAPIs.IOTC_ER_DEVICE_NOT_LISTENING -> LogUtil.e(TAG, "The device is not on listening when connecting.($ret)")
                else -> LogUtil.e(TAG, "Failed to connect device when connecting.($ret)")
            }

            return ret
        }

        return nSID
    }

    fun sendData(method: AppConfig.TUTKP2PMethod, command: String)
    {
        var cmdLength = command.toByteArray().size

        /*
        c code header structure which FW received

        struct cloud_req_header {
            uint8_t method;
            int16_t length;
        };

        size : 4bytes (because of OS alignment)
         */

        var sendBuf = ByteArray(AppConfig.TUTK_MAXSIZE_RECVBUF)
        sendBuf[0] = method.value.toByte()
        sendBuf[1] = 0x0
        sendBuf[2] = (cmdLength shr 8).toByte()
        sendBuf[3] = cmdLength.toByte()

        for(i in 0 until command.toByteArray().size)
        {
            sendBuf[4+i] = command.toByteArray()[i]
            //LogUtil.d(TAG,"sendBuf[${4+i}]:${sendBuf[4+i].toChar()}")
        }

        var nWrite = -1

        nWrite = RDTAPIs.RDT_Write(mRDT_ID, sendBuf, (AppConfig.TUTK_RECV_HEADER_LENGTH + cmdLength))

        LogUtil.d(TAG, "RDT_Write command:$command")

        if(nWrite > 0)
        {
            LogUtil.d(TAG, "RDT_Write success:$nWrite")
        }
        else if(nWrite < 0)
        {
            LogUtil.e(TAG, "RDT_Write error:$nWrite")
            GlobalBus.publish(MainEvent.HideLoading())
            destroyRDT_ID()
        }
    }

    fun receiveData()
    {
        var headerBuf = ByteArray(AppConfig.TUTK_RECV_HEADER_LENGTH)
        var cmdBuf = ByteArray(AppConfig.TUTK_MAXSIZE_RECVBUF)
        var count = 0
        var nRead = -1
        var errorCode = 0
        var payloadLength = 0
        var resultStr = ""

        nRead = RDTAPIs.RDT_Read(mRDT_ID, headerBuf, AppConfig.TUTK_RECV_HEADER_LENGTH, AppConfig.TUTK_RDT_WAIT_TIMEMS)
        LogUtil.d(TAG, "RDT_Read header, nRead:$nRead")

        if(nRead < 0)
        {
            LogUtil.e(TAG, "RDT_Read header error:$nRead")
            GlobalBus.publish(MainEvent.HideLoading())
            destroyRDT_ID()
            return
        }

        /*
        c code header structure which FW received

        struct cloud_resp_header {
            uint8_t error;
            int16_t length;
        };

        size : 4bytes (because of OS alignment)
        */
        errorCode = headerBuf[0].toInt()
        payloadLength = (headerBuf[3].toInt() and 0xFF) + (headerBuf[2].toInt() and 0xFF shl 8)
        LogUtil.d(TAG, "Receive header errorCode:$errorCode")
        LogUtil.d(TAG, "Receive header payloadLength:$payloadLength")

        var remainLength = payloadLength
        while(count < AppConfig.TUTK_RDT_RECV_TIMEOUT_TIMES)
        {
            for(i in 0 until cmdBuf.size)
            {
                cmdBuf[i] = 0
            }

            nRead = RDTAPIs.RDT_Read(mRDT_ID, cmdBuf, remainLength, AppConfig.TUTK_RDT_WAIT_TIMEMS)
            LogUtil.d(TAG, "RDT_Read, nRead:$nRead")

            if(nRead < 0)
            {
                LogUtil.e(TAG, "RDT_Read error:$nRead")
                GlobalBus.publish(MainEvent.HideLoading())
                destroyRDT_ID()
                return
            }

            remainLength -= nRead

            var tmpBuf = ByteArray(nRead)
            for(i in 0 until nRead)
            {
                tmpBuf[i] = cmdBuf[i]
            }

            var tmpStr = String(tmpBuf, StandardCharsets.UTF_8)
            //LogUtil.d(TAG, "RDT_Read receive data:$tmpStr")

            resultStr += tmpStr

            if(remainLength == 0 || remainLength < 0)
            {
                LogUtil.d(TAG, "receiveData End")

                responseCallback.onSuccess(resultStr)

                if(resultStr.length > 4000)
                {
                    for(i in resultStr.indices step 4000)
                    {
                        if(i + 4000 < resultStr.length)
                            LogUtil.d(TAG, "RDT_Read, result: (count) = ${resultStr.substring(i, i + 4000)}")
                        else
                            LogUtil.d(TAG, "RDT_Read, result: (end) = ${resultStr.substring(i, resultStr.length)}")
                    }
                }
                else
                    LogUtil.d(TAG, "RDT_Read, result: $resultStr")

                return
            }

            count++

            if(count >= AppConfig.TUTK_RDT_RECV_TIMEOUT_TIMES)
                responseCallback.onSuccess("")
        }
    }

    private fun destroyRDT_ID()
    {
        if(mRDT_ID > -1)
        {
            RDTAPIs.RDT_Destroy(mRDT_ID)
            mRDT_ID = -1
        }

        if(mSID > -1)
        {
            IOTCAPIs.IOTC_Session_Close(mSID)
            mSID = -1
        }
    }

    fun stopSession()
    {
        LogUtil.d(TAG,"stopSession")
        IOTCAPIs.IOTC_Connect_Stop()
        destroyRDT_ID()
        unInitIOTCRDT()
        m_bHasClientConn = false
    }

    fun setCallback(callback: TUTKP2PResponseCallback)
    {
        responseCallback = callback
    }
}