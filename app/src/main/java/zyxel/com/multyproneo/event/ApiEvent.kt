package zyxel.com.multyproneo.event

import zyxel.com.multyproneo.api.ApiHandler

class ApiEvent
{
    class ApiExecuteComplete(var event: ApiHandler.API_RES_EVENT)
    class StopRegularTask()
}