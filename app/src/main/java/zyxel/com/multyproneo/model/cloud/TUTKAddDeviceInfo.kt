package zyxel.com.multyproneo.model.cloud

data class TUTKAddDeviceInfo
(
    val data: AddDeviceInfo
)

data class AddDeviceInfo
(
    val credential: String = "N/A",
    val dmToken: String = "N/A",
    val state: Int = 0
)