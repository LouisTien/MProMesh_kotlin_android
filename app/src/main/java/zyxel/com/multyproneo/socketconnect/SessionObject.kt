package zyxel.com.multyproneo.socketconnect

/**
 * Created by LouisTien on 2019/7/8.
 */
class SessionObject
{
    var data: String? = null
    var version: Int = 0
    var type: Int = 0
    var dest_ip: String? = null
    var dest_port: Int = 0
    var src_ip: String? = null
    var src_port: Int = 0
    var encryptkey: String? = null
    var length: Int = 0
    var errorCode: IntArray? = null
}