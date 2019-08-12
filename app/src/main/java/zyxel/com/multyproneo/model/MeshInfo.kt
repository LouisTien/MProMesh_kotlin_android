package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/8/2.
 */
data class MeshInfo
(
    val Object: MeshInfoObject = MeshInfoObject(),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class MeshInfoObject
(
    val Enable: Boolean = false
)