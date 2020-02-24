package zyxel.com.multyproneo.model.cloud

data class TUTKUpdateDeviceInfo
(
    val data: UpdateDeviceInfo
)

data class UpdateDeviceInfo
(
    val _id: String = "N/A",
    val account_id: Long = 0,
    val created: String = "N/A",
    val credential: String = "N/A",
    val displayName: String = "N/A",
    val dmtoken: String = "N/A",
    val fwVer: String = "N/A",
    val pk: String = "N/A",
    val udid: String = "N/A",
    val updated: String = "N/A"
)