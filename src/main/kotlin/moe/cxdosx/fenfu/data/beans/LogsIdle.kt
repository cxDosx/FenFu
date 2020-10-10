package moe.cxdosx.fenfu.data.beans

import net.mamoe.mirai.message.GroupMessageEvent

data class LogsIdle(
    /*val sender: Long,
    val sendTime: Long,*/
    val groupMessage: GroupMessageEvent,
    val users: ArrayList<LogsUser>
)