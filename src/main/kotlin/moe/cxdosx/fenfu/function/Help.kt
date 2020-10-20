package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.config.checkBlackList
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At

fun Bot.help() {
    subscribeGroupMessages {
        Regex(FenFuText.regexMatch("help", "帮助"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            reply(
                At(sender) + "\n" + FenFuText.help
            )
        }
    }
}