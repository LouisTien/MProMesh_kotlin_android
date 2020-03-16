package zyxel.com.multyproneo.model

data class IPInterfaceInfo
(
    val Object: List<IPInterfaceInfoObject> = listOf(IPInterfaceInfoObject()),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class IPInterfaceInfoObject
(
    val Alias: String = "N/A",
    val Enable: Boolean = false,
    val IPv4Address: List<IPv4AddressObject> = listOf(IPv4AddressObject()),
    val IPv4Enable: Boolean = false,
    val IPv6Address: List<Any> = listOf(Any()),
    val IPv6Enable: Boolean = false,
    val Name: String = "N/A",
    val Status: String = "N/A",
    val X_ZYXEL_IfName: String = "N/A"
)

data class IPv4AddressObject
(
    val AddressingType: String = "N/A",
    val Alias: String = "N/A",
    val Enable: Boolean = false,
    val IPAddress: String = "N/A",
    val Status: String = "N/A",
    val SubnetMask: String = "N/A",
    val X_ZYXEL_Alias: Boolean = false,
    val X_ZYXEL_Dhcp4Subnet_Ref: String = "N/A",
    val X_ZYXEL_IfName: String = "N/A"
)