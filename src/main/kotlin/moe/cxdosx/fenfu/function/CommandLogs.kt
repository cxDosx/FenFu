package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.data.QueryLogs
import moe.cxdosx.fenfu.utils.DatabaseHelper
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At

/**
 * 查询logs相关
 */
fun Bot.queryLogs(){
    subscribeGroupMessages {
        Regex(FenFuText.regexMatch("logs", "log"), RegexOption.IGNORE_CASE) matching regex@{
            val msg = it.replace(" +", " ").trim()//防止憨批打两个空格
            if (msg.contains(" ")) {
                val split = msg.split(" ")
                if (split.size == 2) { // 只有用户名
                    /**
                     * 该方法已在内部reply
                     */
                    QueryLogs().queryUser(split[1], this)
                } else if (split.size == 3) { //不光有用户名还有 服务器名或者副本名
                    val serverName = DatabaseHelper.instance.queryIntactServerName(split[2])
                    /**
                     * 假设第三位是服务器名
                     */
                    if (serverName.isNotEmpty()) {
                        reply(
                            At(sender) + "\n" +
                                    QueryLogs().queryLogsData(
                                        split[1],
                                        split[2],
                                        DatabaseHelper.instance.getDefaultQueryZone()
                                    )
                        )
                        return@regex
                    } else {
                        /**
                         * 假设第三位是副本名
                         */
                        val queryUser = QueryLogs().queryUser(split[1])
                        if (queryUser.isEmpty()) { //没找到User
                            reply(
                                At(sender) + "\n" + FenFuText.logsUserQueryEmpty
                            )
                            return@regex
                        } else if (queryUser.size == 1) { //找到单个user
                            val bossNameQueryZone = DatabaseHelper.instance.bossNameQueryZone(split[2])
                            if (bossNameQueryZone == -1) { //没有匹配到areaId
                                reply(
                                    At(sender) + "\n" + FenFuText.notFoundAreaId(split[2])
                                )
                                return@regex
                            } else { //匹配到了areaId
                                reply(
                                    At(sender) + "\n" + QueryLogs().queryLogsData(
                                        queryUser[0].userName,
                                        queryUser[0].serverName,
                                        bossNameQueryZone
                                    )
                                )
                            }
                        } else { //多个匹配user
                            reply(
                                At(sender) + "\n" + FenFuText.queryLogsMultiPlayer(split[1])
                            )
                        }
                    }

                } else if (split.size == 4) {//用户名 服务器名 还有副本名
                    val queryZone = DatabaseHelper.instance.bossNameQueryZone(split[3])
                    if (queryZone == -1) {
                        reply(At(sender) + "\n" + FenFuText.notFoundAreaId(split[3]))
                    } else {
                        reply(
                            At(sender) + "\n" +
                                    QueryLogs().queryLogsData(
                                        split[1],
                                        split[2],
                                        queryZone
                                    )
                        )
                    }
                }
            } else {
                reply(At(sender) + "\n" + FenFuText.logsHelp())
            }
        }
    }
}