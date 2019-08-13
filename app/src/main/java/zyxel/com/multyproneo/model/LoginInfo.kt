package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/7/17.
 */
data class LoginInfo
(
    val MethodList: List<String> = listOf(String()),
    val oper_status: String = "N/A",
    val sessionkey: String = "N/A"
)