package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/5/30.
 */
data class GatewayProfile
(
        var modelName: String = "N/A",
        var aliasName:String = "N/A",
        var systemName: String = "N/A",
        var userDefineName: String = "N/A",
        var idInDB: String = "N/A",
        var IP: String = "N/A",
        var firmwareVersion: String = "N/A",
        var serial: String = "N/A",
        var password: String = "N/A",
        var userName: String = "N/A",
        var type: Int = 0,
        var autoConfigEnable: Int = 0,
        var initFlag: Int = 0,
        var multyFlag: Int = 0,
        var internetProtocol: String = "N/A",
        var limitLogin: Int = 0,
        var loginAccountFlag: Int = 0,
        var customer: String = "N/A",
        var theme: Int = 0,
        var agentVersion: String = "N/A"
){}