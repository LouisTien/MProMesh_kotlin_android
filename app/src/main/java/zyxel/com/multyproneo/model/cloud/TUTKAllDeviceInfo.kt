package zyxel.com.multyproneo.model.cloud

data class TUTKAllDeviceInfo
(
    val data: ArrayList<AllDeviceInfo> = ArrayList()
)

data class AllDeviceInfo
(
    val credential: String = "N/A",
    val displayName: String = "N/A",
    val dmToken: String = "N/A",
    val fwVer: String = "N/A",
    val state: Int = 0,
    val udid: String = "N/A"
)