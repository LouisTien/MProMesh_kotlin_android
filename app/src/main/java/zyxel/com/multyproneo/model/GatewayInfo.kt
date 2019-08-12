package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/8/5.
 */
data class GatewayInfo
(
    var IP: String = "N/A",
    val ApiName: String = "N/A",
    val DeviceMode: String = "N/A",
    val ModelName: String = "N/A",
    val SoftwareVersion: String = "N/A",
    val SupportedApiVersion: List<SupportedApiVersion> = listOf(SupportedApiVersion())
)

data class SupportedApiVersion
(
    val ApiVersion: String = "N/A",
    val HttpsPort: Int = 0,
    val LoginURI: String = "N/A",
    val Protocol: String = "N/A"
)