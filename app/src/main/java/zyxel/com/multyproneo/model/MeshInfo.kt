package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/8/2.
 */
data class MeshInfo
(
    val Object: MeshInfoObject,
    val oper_status: String,
    val requested_path: String
)

data class MeshInfoObject
(
    val Enable: Boolean
)