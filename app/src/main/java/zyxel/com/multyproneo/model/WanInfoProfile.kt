package zyxel.com.multyproneo.model

import java.io.Serializable

/**
 * Created by LouisTien on 2019/6/4.
 */
data class WanInfoProfile
(
        var WanStatus: String = "N/A",
        var WanIP: String = "N/A",
        var WanMAC: String = "N/A",
        var WanDNS: String = "N/A",
        var WanPhyRate: Int = 0,
        var WanTX: Int = 0,
        var WanRX: Int = 0
) : Serializable
{}