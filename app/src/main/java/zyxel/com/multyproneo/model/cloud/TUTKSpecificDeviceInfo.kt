package zyxel.com.multyproneo.model.cloud

data class TUTKSpecificDeviceInfo
(
    val data: SpecificDeviceInfo
)

data class SpecificDeviceInfo
(
    val credential: String = "N/A",
    val displayName: String = "N/A",
    val dmToken: String = "N/A",
    val fwVer: String = "N/A",
    val state: Int = 0,
    val udid: String = "N/A"
)