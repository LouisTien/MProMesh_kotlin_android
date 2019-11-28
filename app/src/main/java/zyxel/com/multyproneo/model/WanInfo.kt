package zyxel.com.multyproneo.model

import java.io.Serializable

/**
 * Created by LouisTien on 2019/8/13.
 */
data class WanInfo
(
    val Object: WanInfoObject = WanInfoObject(),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
) : Serializable

data class WanInfoObject
(
    val DNSServer: String = "N/A",
    val IPAddress: String = "N/A",
    val MAC: String = "N/A",
    val PhyRate: Int = 0,
    val RxRate: Int = 0,
    val Status: String = "N/A",
    val TxRate: Int = 0
) : Serializable