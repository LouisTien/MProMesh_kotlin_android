package zyxel.com.multyproneo.api.cloud

import com.tutk.IOTC.IOTCAPIs
import com.tutk.IOTC.RDTAPIs
import com.tutk.IOTC.St_RDT_Status
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

        if(ms_nIOTCInit != IOTCAPIs.IOTC_ER_NoERROR)
        {
            ms_nIOTCInit = IOTCAPIs.IOTC_Initialize2(0)
            LogUtil.d(TAG, "IOTC_Initialize2(.)=$ms_nIOTCInit")

            if(ms_nIOTCInit >= 0)
            {
                val ret = RDTAPIs.RDT_Initialize()
                LogUtil.d(TAG, "RDT_Initialize(.)=$ret")
            }

            return ms_nIOTCInit
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
            return nSID

        ret = IOTCAPIs.IOTC_Connect_ByUID_Parallel(m_strUID, nSID)
        LogUtil.d(TAG, "IOTC_Connect_ByUID_Parallel(.)=$ret")

        if(ret < 0)
        {
            when(ret)
            {
                IOTCAPIs.IOTC_ER_NOT_INITIALIZED -> LogUtil.d(TAG, "Don't call IOTC_Initialize2() when connecting.(${AppConfig.TUTK_STATUS_INIT_SEARCH_DEV})")
                IOTCAPIs.IOTC_ER_CONNECT_IS_CALLING -> LogUtil.d(TAG, "IOTC_Connect_ByXX() is calling when connecting.($ret)")
                IOTCAPIs.IOTC_ER_FAIL_RESOLVE_HOSTNAME -> LogUtil.d(TAG, "Can't resolved server's Domain name when connecting.($ret)")
                IOTCAPIs.IOTC_ER_SERVER_NOT_RESPONSE -> LogUtil.d(TAG, "Server not response when connecting.($ret)")
                IOTCAPIs.IOTC_ER_FAIL_GET_LOCAL_IP -> LogUtil.d(TAG, "Can't Get local IP when connecting.($ret)")
                IOTCAPIs.IOTC_ER_UNKNOWN_DEVICE -> LogUtil.d(TAG, "Wrong UID when connecting.($ret)")
                IOTCAPIs.IOTC_ER_UNLICENSE -> LogUtil.d(TAG, "UID is not registered when connecting.($ret)")
                IOTCAPIs.IOTC_ER_CAN_NOT_FIND_DEVICE -> LogUtil.d(TAG, "Device is NOT online when connecting.($ret)")
                IOTCAPIs.IOTC_ER_EXCEED_MAX_SESSION -> LogUtil.d(TAG, "Exceed the max session number when connecting.($ret)")
                IOTCAPIs.IOTC_ER_TIMEOUT -> LogUtil.d(TAG, "Timeout when connecting.($ret)")
                IOTCAPIs.IOTC_ER_DEVICE_NOT_LISTENING -> LogUtil.d(TAG, "The device is not on listening when connecting.($ret)")
                else -> LogUtil.d(TAG, "Failed to connect device when connecting.($ret)")
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

        nWrite = RDTAPIs.RDT_Write(mRDT_ID, sendBuf, (4 + cmdLength))

        LogUtil.d(TAG, "RDT_Write command:$command")

        if(nWrite > 0)
        {
            LogUtil.d(TAG, "RDT_Write success:$nWrite")
        }
        else if(nWrite < 0)
        {
            LogUtil.e(TAG, "RDT_Write error:$nWrite")
            destroyRDT_ID()
        }
    }

    fun receiveData()
    {
        var recvBuf = ByteArray(AppConfig.TUTK_MAXSIZE_RECVBUF)
        var nRead = -1
        var error = 0
        var length = 0
        lateinit var result: String
        lateinit var cmdBuf: ByteArray


        for(i in 0 until recvBuf.size)
            recvBuf[i] = 0

        nRead = RDTAPIs.RDT_Read(mRDT_ID, recvBuf, AppConfig.TUTK_MAXSIZE_RECVBUF, 1000)
        LogUtil.d(TAG, "RDT_Read, nRead:$nRead")

        if(nRead > 0)
        {
            /*
            c code header structure which FW received

            struct cloud_resp_header {
                uint8_t error;
                int16_t length;
            };

            size : 4bytes (because of OS alignment)
            */

            error = recvBuf[0].toInt()
            length = (recvBuf[3].toInt() and 0xFF) + (recvBuf[2].toInt() and 0xFF shl 8)

            if(length > 0 && length < AppConfig.TUTK_MAXSIZE_RECVBUF)
                cmdBuf = ByteArray(length)
            else
                cmdBuf = ByteArray(0)

            for(i in 0 until length)
            {
                cmdBuf[i] = recvBuf[4 + i]
                //LogUtil.d(TAG,"cmdBuf[$i]:${cmdBuf[i].toChar()}")
            }

            result = String(cmdBuf, StandardCharsets.UTF_8)

            LogUtil.d(TAG, "RDT_Read header, error:$error")
            LogUtil.d(TAG, "RDT_Read header, length:$length")

            if(result.length > 4000)
            {
                for(i in result.indices step 4000)
                {
                    if(i + 4000 < result.length)
                        LogUtil.d(TAG, "RDT_Read, result: (count) = ${result.substring(i, i + 4000)}")
                    else
                        LogUtil.d(TAG, "RDT_Read, result: (end) = ${result.substring(i, result.length)}")
                }
            }
            else
                LogUtil.d(TAG, "RDT_Read, result: $result")

            responseCallback.onSuccess(result)
        }
        else if(nRead == RDTAPIs.RDT_ER_TIMEOUT)
        {
            LogUtil.d(TAG, "RDTAPIs.RDT_ER_TIMEOUT")
        }
        else if(nRead < 0)
        {
            LogUtil.e(TAG, "RDT_Read error:$nRead")
            destroyRDT_ID()
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
        m_bHasClientConn = false
    }

    fun setCallback(callback: TUTKP2PResponseCallback)
    {
        responseCallback = callback
    }
}