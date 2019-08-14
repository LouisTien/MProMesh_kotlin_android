package zyxel.com.multyproneo.model

import java.io.Serializable

/**
 * Created by LouisTien on 2019/8/5.
 */
data class GatewayInfo
(
    var IdInDB: String = "N/A",
    var UserName: String = "N/A",
    var Password: String = "N/A",
    var UserDefineName: String = "N/A",
    var IP: String = "N/A",
    var MAC: String = "00:00:00:00:00",
    var ApiName: String = "N/A",
    var DeviceMode: String = "N/A",
    var ModelName: String = "N/A",
    var SoftwareVersion: String = "N/A",
    val SupportedApiVersion: List<SupportedApiVersion> = listOf(SupportedApiVersion())
) : Serializable
{
    fun getName(): String
    {
        if(UserDefineName == null || UserDefineName == "N/A" || UserDefineName == " " || UserDefineName == "")
            return ModelName
        else
            return UserDefineName
    }
}

data class SupportedApiVersion
(
    val ApiVersion: String = "N/A",
    val HttpsPort: Int = 0,
    val LoginURI: String = "N/A",
    val Protocol: String = "N/A"
)