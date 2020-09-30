package moe.cxdosx.fenfu

import moe.cxdosx.fenfu.config.BotConfig
import net.mamoe.mirai.*
import moe.cxdosx.fenfu.function.*

suspend fun main() {
    val bot = Bot(
        BotConfig.botQQ,
        BotConfig.botPwd
    ) {
        fileBasedDeviceInfo("device.json")
    }.alsoLogin()

    bot.apply {
        help()
        csm()
        queryLogs()
        queryHpsLogs()
        userBind()
        atBotLogs()
    }

    bot.join()
}