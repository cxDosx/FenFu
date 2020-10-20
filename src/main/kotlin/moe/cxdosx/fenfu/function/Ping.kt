package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.config.checkBlackList
import moe.cxdosx.fenfu.data.Ping
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At

fun Bot.ping() {
    subscribeGroupMessages {
        Regex(FenFuText.regexMatch("ping"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            val preMessage = reply(
                At(sender) +
                        "\n正在检查身体..."
            )
            Ping(this, preMessage).ping()
        }
    }
}