package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.BotConfig
import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.utils.DatabaseHelper
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages

fun Bot.ownerSend() {
    subscribeFriendMessages {
        Regex(FenFuText.regexMatch("sendall"), RegexOption.IGNORE_CASE) matching regex@{
            if (sender.id == BotConfig.ownerQQ) {
                val msg = it.substring(it.indexOf(" ")).trim()
                groups.forEach { group ->
                    group.sendMessage(msg)
                }
            }
        }

        contains("reloadTimedTask", true) {
            if (sender.id == BotConfig.ownerQQ) {
                TimedTask.initTimedTask(bot)
                reply(
                    "已重载，共${TimedTask.taskSize()}个事件"
                )
            }
        }

        Regex(FenFuText.regexMatch("setu"), RegexOption.IGNORE_CASE) matching regex@{
            if (sender.id == BotConfig.ownerQQ) {
                val msg = it.substring(it.indexOf(" ")).trim()
                val toInt = msg.toInt()
                DatabaseHelper.instance.switchSeTuEnable(toInt)
                reply(
                    "当前setu有效性：${DatabaseHelper.instance.checkSeTuEnable()}"
                )
            }
        }
    }

    subscribeGroupMessages {
        Regex(FenFuText.regexMatch("setu"), RegexOption.IGNORE_CASE) matching regex@{
            if (sender.id == BotConfig.ownerQQ) {
                val msg = it.substring(it.indexOf(" ")).trim()
                val toInt = msg.toInt()
                DatabaseHelper.instance.switchSeTuEnable(toInt)
                reply(
                    "当前setu有效性：${DatabaseHelper.instance.checkSeTuEnable()}"
                )
            }
        }
    }
}