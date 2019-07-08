package zyxel.com.multyproneo.socketconnect

/**
 * Created by LouisTien on 2019/7/8.
 */
class Packet
{
    var version: Int = 0
    var type: Int = 0
    var length: Int = 0
    var payload: ByteArray? = null
    var receivedIP: String? = null
    var receivedPort: Int = 0
    private var errorCode: IntArray? = null

    constructor(rawdata: ByteArray)
    {
        version = rawdata[0].toInt()
        type = rawdata[1].toInt()
        length = (rawdata[2].toInt() and 0xFF shl 8) + (rawdata[3].toInt() and 0xFF)
        payload = ByteArray(length)
        for(i in 0 until length)
            payload!![i] = rawdata[i + 4]
        errorCode = IntArray(20)
    }

    constructor(_version: Int, _type: Int, _payload: ByteArray)
    {
        version = _version
        type = _type
        length = _payload.size
        payload = _payload
    }

    fun getBytes(): ByteArray
    {
        val data = ByteArray(payload!!.size + 4)
        data[0] = version.toByte()
        data[1] = type.toByte()
        data[2] = (length shr 8).toByte()
        data[3] = length.toByte()
        for(i in payload!!.indices)
            data[i + 4] = payload!![i]
        /* converter payload length from big endian to lillte endian */
        val tmp1 = data[2]
        val tmp2 = data[3]
        data[2] = tmp2
        data[3] = tmp1
        /* end */
        return data
    }
}