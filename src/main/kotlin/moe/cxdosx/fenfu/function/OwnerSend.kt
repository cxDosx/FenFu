package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.BotConfig
import moe.cxdosx.fenfu.config.FenFuText
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeFriendMessages

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
    }
}