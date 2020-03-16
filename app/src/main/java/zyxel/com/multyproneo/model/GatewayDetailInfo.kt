package zyxel.com.multyproneo.model

data class GatewayDetailInfo
(
    val Object: GatewayDetailInfoObject = GatewayDetailInfoObject(),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class GatewayDetailInfoObject
(
    val Description: String = "N/A",
    val FirstUseDate: String = "N/A",
    val HardwareVersion: String = "N/A",
    val Manufacturer: String = "N/A",
    val ManufacturerOUI: String = "N/A",
    val ModelName: String = "N/A",
    val ProductClass: String = "N/A",
    val SerialNumber: String = "N/A",
    val SoftwareVersion: String = "N/A",
    val UpTime: Long = 0
)