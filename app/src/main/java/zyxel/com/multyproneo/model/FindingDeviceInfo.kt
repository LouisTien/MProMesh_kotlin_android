package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/8/5.
 */
data class FindingDeviceInfo
(
    var IP: String,
    val ApiName: String,
    val DeviceMode: String,
    val ModelName: String,
    val SoftwareVersion: String,
    val SupportedApiVersion: List<SupportedApiVersion>
)

data class SupportedApiVersion
(
    val ApiVersion: String,
    val HttpsPort: Int,
    val LoginURI: String,
    val Protocol: String
)