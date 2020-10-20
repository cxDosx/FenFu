package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.config.checkBlackList
import moe.cxdosx.fenfu.data.QueryLogs
import moe.cxdosx.fenfu.utils.DatabaseHelper
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At

fun Bot.userBind() {
    subscribeGroupMessages {
        /**
         * 绑定指定角色
         */
        Regex(FenFuText.regexMatch("bind", "绑定"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            val msg = it.replace(" +", " ").trim()//防止憨批打两个空格
            if (msg.contains(" ")) {
                val split = msg.split(" ")
                if (split.size == 2) {
                    val queryUser = QueryLogs().queryUser(split[1])
                    if (queryUser.size == 1 && queryUser[0].serverName == FenFuText.fenfuErrorMsg) {
                        reply(
                            At(sender) + "\n数据处理异常\n${queryUser[0].userName}"
                        )
                        return@regex
                    }
                    if (queryUser.isEmpty()) {
                        reply(
                            At(sender) + "\n" + FenFuText.logsUserQueryEmpty
                        )
                    } else if (queryUser.size > 1) {
                        reply(
                            At(sender) + "\n" + FenFuText.bindUserMultiPlayer(split[1])
                        )
                    } else {
                        val queryBindUser = DatabaseHelper.instance.queryBindUser(sender.id)
                        if (queryBindUser == null) {//未绑定
                            DatabaseHelper.instance.insertUserBindData(
                                queryUser[0].userName,
                                queryUser[0].serverName,
                                sender.id
                            )
                            reply(
                                At(sender) + "\n" + "你已成功绑定：${queryUser[0].userName}✿${queryUser[0].serverName}"
                            )
                        } else {
                            DatabaseHelper.instance.updateUserBindData(
                                queryUser[0].userName,
                                queryUser[0].serverName,
                                sender.id
                            )
                            reply(
                                At(sender) + "\n" + "你绑定的角色已成功变更：${queryBindUser!!.userName}✿${queryBindUser!!.serverName}" +
                                        " =〉${queryUser[0].userName}✿${queryUser[0].serverName}"
                            )
                        }
                    }
                } else if (split.size > 2) {
                    val serverName = DatabaseHelper.instance.queryIntactServerName(split[2])
                    if (serverName.isEmpty()) {
                        reply(
                            At(sender) + "\n" + FenFuText.unKnowServerName()
                        )
                    } else {
                        val queryBindUser = DatabaseHelper.instance.queryBindUser(sender.id)
                        if (queryBindUser == null) {//未绑定
                            DatabaseHelper.instance.insertUserBindData(
                                split[1],
                                serverName,
                                sender.id
                            )
                            reply(
                                At(sender) + "\n" + "你已成功绑定：${split[1]}✿$serverName"
                            )
                        } else {
                            DatabaseHelper.instance.updateUserBindData(
                                split[1],
                                serverName,
                                sender.id
                            )
                            reply(
                                At(sender) + "\n" + "你绑定的角色已成功变更：${queryBindUser!!.userName}✿${queryBindUser!!.serverName}" +
                                        " =〉${split[1]}✿$serverName"
                            )
                        }
                    }
                } else {
                    reply(
                        At(sender) + "\n" + FenFuText.bindHelp
                    )
                }
            } else {
                reply(
                    At(sender) + "\n" + FenFuText.bindHelp
                )
            }

        }

        /**
         * 解绑角色
         */
        Regex(FenFuText.regexMatch("unbind", "解绑"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            val queryBindUser = DatabaseHelper.instance.queryBindUser(sender.id)
            if (queryBindUser == null) {//未绑定
                reply(
                    At(sender) + "\n${FenFuText.notFoundBindUser}"
                )
            } else {
                DatabaseHelper.instance.deleteBindUser(sender.id)
                reply(
                    At(sender) + "\n" + "解绑成功..."
                )
            }
        }

        /**
         * 查询已绑定角色logs
         */
        Regex(FenFuText.regexMatch("me"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            val msg = it.replace(" +", " ").trim()//防止憨批打两个空格
            val queryBindUser = DatabaseHelper.instance.queryBindUser(sender.id)
            if (queryBindUser == null) {
                reply(
                    At(sender) + "\n" + FenFuText.notFoundBindUser
                )
                return@regex
            }
            if (msg.contains(" ")) {
                val split = msg.split(" ")
                val queryZone = DatabaseHelper.instance.bossNameQueryZone(split[1])
                if (queryZone == -1) {
                    reply(FenFuText.notFoundAreaId())
                } else {
                    reply(
                        At(sender) + "\n" +
                                QueryLogs().queryLogsData(
                                    queryBindUser!!.userName,
                                    queryBindUser!!.serverName,
                                    queryZone
                                )
                    )

                }
            } else {
                reply(
                    At(sender) + "\n" +
                            QueryLogs().queryLogsData(
                                queryBindUser!!.userName,
                                queryBindUser!!.serverName,
                                DatabaseHelper.instance.getDefaultQueryZone()
                            )
                )
            }
        }

        /**
         * 查询已绑定角色hps
         */
        Regex(FenFuText.regexMatch("mehps", "hpsme"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            val msg = it.replace(" +", " ").trim()//防止憨批打两个空格
            val queryBindUser = DatabaseHelper.instance.queryBindUser(sender.id)
            if (queryBindUser == null) {
                reply(
                    At(sender) + "\n" + FenFuText.notFoundBindUser
                )
                return@regex
            }
            if (msg.contains(" ")) {
                val split = msg.split(" ")
                val queryZone = DatabaseHelper.instance.bossNameQueryZone(split[1])
                if (queryZone == -1) {
                    reply(FenFuText.notFoundAreaId())
                } else {
                    reply(
                        At(sender) + "\n" +
                                QueryLogs().queryLogsData(
                                    queryBindUser!!.userName,
                                    queryBindUser!!.serverName,
                                    queryZone, true
                                )
                    )

                }
            } else {
                reply(
                    At(sender) + "\n" +
                            QueryLogs().queryLogsData(
                                queryBindUser!!.userName,
                                queryBindUser!!.serverName,
                                DatabaseHelper.instance.getDefaultQueryZone(), true
                            )
                )
            }
        }
    }
}