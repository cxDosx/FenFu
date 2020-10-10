package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.data.QueryLogs
import moe.cxdosx.fenfu.data.beans.LogsIdle
import moe.cxdosx.fenfu.data.beans.LogsIdleQueue
import moe.cxdosx.fenfu.utils.DatabaseHelper
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.content

/**
 * AtBot处理Logs
 */
fun Bot.atBotLogs() {
    subscribeGroupMessages {
        has<At> {
            if (it.target == bot.id) {
                val drop = message.drop(message.size - 1)
                val rawMessage = drop.asMessageChain().content.trim()
                queryIdleQueue(this, rawMessage)
            }
        }

        /**
         * 处理响应只回复数字的内容
         */
        Regex(FenFuText.matchNumber) matching regex@{
            queryIdleQueue(this, it)
        }
    }
}

/**
 * 处理查询待定状态的LOGS请求
 */
private suspend fun queryIdleQueue(messageEvent: GroupMessageEvent, content: String) {
    if (LogsIdleQueue.idleQueue.isEmpty()) {
        return
    } else {
        var userName = ""
        var serverName = ""
        synchronized(LogsIdleQueue.idleQueue) {
            val removeList = ArrayList<LogsIdle>()
            for (element in LogsIdleQueue.idleQueue) {
                val diffTime = kotlin.math.abs(messageEvent.time - element.groupMessage.time)
                if (diffTime >= 60) {
                    removeList.add(element)
                    continue
                }
                if (element.groupMessage.sender.id == messageEvent.sender.id) {
                    val index = try {
                        content.trim().toInt() - 1
                    } catch (e: Exception) {
                        for (el in removeList) {
                            LogsIdleQueue.idleQueue.remove(el)
                        }
                        return
                    }
                    if (index >= element.users.size) {
                        return
                    } else {
                        userName = element.users[index].userName
                        serverName = element.users[index].serverName
                        LogsIdleQueue.idleQueue.remove(element)
                    }
                    break
                }
            }
            for (element in removeList) {
                LogsIdleQueue.idleQueue.remove(element)
            }
        }
        if (userName.isNotEmpty() && serverName.isNotEmpty()) {
            messageEvent.reply(
                QueryLogs().queryLogsData(
                    userName,
                    serverName,
                    DatabaseHelper.instance.getDefaultQueryZone()
                )
            )
        }
    }
}